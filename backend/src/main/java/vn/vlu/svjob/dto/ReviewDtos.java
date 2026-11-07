package vn.vlu.svjob.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** Đánh giá hai chiều (FR-18, FR-19). */
public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record ReviewRequest(
            @NotNull(message = "Thiếu người được đánh giá") Long revieweeId,
            @NotNull(message = "Vui lòng chọn số sao")
            @Min(value = 1, message = "Điểm từ 1 đến 5") @Max(value = 5, message = "Điểm từ 1 đến 5") Integer rating,
            @Size(max = 500, message = "Nhận xét tối đa 500 ký tự") String comment) {
    }

    public record ReviewItem(Long id, Long jobId, String jobTitle, String reviewerName, Integer rating,
                             String comment, LocalDateTime createdAt) {
    }

    public record UserReviews(Long userId, String fullName, Double avgRating, long reviewCount, List<ReviewItem> items) {
    }
}
