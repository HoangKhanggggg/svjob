package vn.vlu.svjob.dto;

import jakarta.validation.constraints.NotNull;
import vn.vlu.svjob.entity.ApplicationStatus;
import vn.vlu.svjob.entity.JobStatus;

import java.time.LocalDateTime;

/** Ứng tuyển và duyệt (FR-12 → FR-15). */
public final class ApplicationDtos {

    private ApplicationDtos() {
    }

    public record DecisionRequest(@NotNull(message = "Vui lòng chọn ACCEPTED hoặc REJECTED") ApplicationStatus status) {
    }

    /** Đơn của tôi (phía SV). canReview = được đánh giá NTD; reviewed = đã đánh giá rồi. */
    public record MyApplication(Long id, Long jobId, String jobTitle, String companyName, Long employerId,
                                JobStatus jobStatus, ApplicationStatus status, LocalDateTime appliedAt,
                                LocalDateTime processedAt, boolean hasCv, boolean canReview, boolean reviewed) {
    }

    /** Ứng viên của một tin (phía NTD). */
    public record Applicant(Long id, Long studentId, String fullName, String email, String phone, String school,
                            String major, Integer yearOfStudy, String skills, String bio, String coverLetter,
                            boolean hasCv, ApplicationStatus status, LocalDateTime appliedAt,
                            LocalDateTime processedAt, Double avgRating, boolean reviewed) {
    }

    public record ApplicationResult(Long id, Long jobId, String studentName, ApplicationStatus status,
                                    LocalDateTime processedAt) {
    }
}
