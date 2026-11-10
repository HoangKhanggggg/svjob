package vn.vlu.svjob.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import vn.vlu.svjob.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public final class CommonDtos {

    private CommonDtos() {
    }

    /** Kết quả phân trang gọn cho frontend (page bắt đầu từ 0). */
    public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> of(Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        }
    }

    public record MessageResponse(String message) {
    }

    public record CategoryRequest(
            @NotBlank(message = "Tên ngành nghề không được để trống") @Size(max = 100) String name,
            @Size(max = 255) String description,
            Boolean active) {
    }

    public record CategoryResponse(Integer id, String name, String description, boolean active) {
    }

    public record NotificationItem(Long id, String content, String link, boolean read, LocalDateTime createdAt) {
    }

    public record NotificationList(long unreadCount, List<NotificationItem> items) {
    }

    public record UserStatusRequest(@NotNull(message = "Vui lòng chọn trạng thái") UserStatus status) {
    }

    public record CategoryCount(Integer id, String name, long openJobs) {
    }

    public record Stats(long openJobs, long completedJobs, long employers, long students, List<CategoryCount> categories) {
    }
}
