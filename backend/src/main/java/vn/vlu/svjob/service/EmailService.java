package vn.vlu.svjob.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gửi email cho sinh viên khi đơn ứng tuyển được nhận (góp ý của giảng viên).
 * - app.mail.enabled=false (mặc định): chỉ ghi log, không gửi, để demo/test không cần SMTP.
 * - app.mail.enabled=true: gửi thật qua SMTP khai báo trong db.properties (không đưa lên Git).
 * Gửi ở luồng riêng và nuốt lỗi, nên SMTP hỏng cũng không làm hỏng thao tác duyệt đơn.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean enabled;
    private final String from;
    private final String frontendUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "svjob-mail");
        t.setDaemon(true);
        return t;
    });

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${app.mail.enabled:false}") boolean enabled,
                        @Value("${app.mail.from:no-reply@svjob.vn}") String from,
                        @Value("${app.cors-origin:http://localhost:5173}") String frontendUrl) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    /** Nội dung thư "đơn được nhận". Tách riêng để unit test kiểm tra được. */
    String acceptedBody(String studentName, String jobTitle, String companyName) {
        return "Chào " + studentName + ",\n\n"
                + "Chúc mừng! Đơn ứng tuyển vị trí “" + jobTitle + "” tại " + companyName + " của bạn đã được nhà tuyển dụng nhận.\n"
                + "Hãy đăng nhập SVJob để xem chi tiết và liên hệ nhà tuyển dụng: " + frontendUrl + "/student/applications\n\n"
                + "Chúc bạn có một công việc thật tốt.\nSVJob";
    }

    public void sendApplicationAccepted(String toEmail, String studentName, String jobTitle, String companyName) {
        String subject = "[SVJob] Đơn ứng tuyển “" + jobTitle + "” đã được nhận";
        String body = acceptedBody(studentName, jobTitle, companyName);
        if (!enabled) {
            log.info("[MAIL tắt] Sẽ gửi tới {}: {}", toEmail, subject);
            return;
        }
        executor.submit(() -> {
            try {
                JavaMailSender sender = mailSender.getIfAvailable();
                if (sender == null) {
                    log.warn("Chưa cấu hình SMTP (spring.mail.host), bỏ qua email tới {}", toEmail);
                    return;
                }
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(from);
                msg.setTo(toEmail);
                msg.setSubject(subject);
                msg.setText(body);
                sender.send(msg);
                log.info("Đã gửi email tới {}", toEmail);
            } catch (Exception e) {
                log.error("Gửi email tới {} thất bại: {}", toEmail, e.getMessage());
            }
        });
    }
}
