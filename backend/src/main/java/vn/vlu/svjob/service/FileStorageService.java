package vn.vlu.svjob.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.vlu.svjob.exception.ApiException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Lưu file CV lên ổ đĩa (thư mục app.upload-dir/cv). Phụ trách: Khang.
 * Chỉ nhận PDF ≤ 5 MB (BR-06). Tên file lưu là UUID để không bị trùng và không đoán được.
 */
@Service
public class FileStorageService {

    static final long MAX_SIZE = 5L * 1024 * 1024;

    private final Path cvDir;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.cvDir = Path.of(uploadDir, "cv").toAbsolutePath().normalize();
        try {
            Files.createDirectories(cvDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Không tạo được thư mục lưu file", e);
        }
    }

    /** Kiểm tra và lưu CV, trả về tên file đã lưu (ghi vào cột cv_url). */
    public String storeCv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Vui lòng chọn file CV");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".pdf") || !isPdf(file)) {
            throw ApiException.badRequest("Chỉ chấp nhận file PDF");
        }
        if (file.getSize() > MAX_SIZE) {
            throw ApiException.badRequest("File tối đa 5 MB");
        }
        String stored = UUID.randomUUID() + ".pdf";
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, cvDir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Lưu file thất bại", e);
        }
        return stored;
    }

    public Resource loadCv(String storedName) {
        Path path = cvDir.resolve(storedName).normalize();
        if (!path.startsWith(cvDir) || !Files.exists(path)) {
            throw ApiException.notFound("Không tìm thấy file CV");
        }
        return new PathResource(path);
    }

    /** File PDF thật luôn bắt đầu bằng "%PDF" – chặn trường hợp đổi đuôi file .docx thành .pdf. */
    private static boolean isPdf(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(4);
            return head.length == 4 && head[0] == '%' && head[1] == 'P' && head[2] == 'D' && head[3] == 'F';
        } catch (IOException e) {
            return false;
        }
    }
}
