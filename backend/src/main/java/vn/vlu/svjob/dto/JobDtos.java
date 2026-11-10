package vn.vlu.svjob.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.SalaryUnit;
import vn.vlu.svjob.entity.WorkShift;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Tin tuyển dụng (FR-06 → FR-11, FR-16). */
public final class JobDtos {

    private JobDtos() {
    }

    public record JobRequest(
            @NotBlank(message = "Không được để trống") @Size(max = 200, message = "Tối đa 200 ký tự") String title,
            @NotNull(message = "Vui lòng chọn ngành nghề") Integer categoryId,
            @NotBlank(message = "Không được để trống") String description,
            @NotNull(message = "Không được để trống") @Min(value = 0, message = "Lương không được âm") Integer salaryMin,
            @Min(value = 0, message = "Lương không được âm") Integer salaryMax,
            @NotNull(message = "Vui lòng chọn đơn vị lương") SalaryUnit salaryUnit,
            @NotBlank(message = "Không được để trống") @Size(max = 100) String location,
            @NotBlank(message = "Không được để trống") @Size(max = 200) String workTime,
            @NotNull(message = "Không được để trống") @Min(value = 1, message = "Cần tuyển ít nhất 1 người")
            @Max(value = 100, message = "Tối đa 100 người") Integer quantity,
            @NotNull(message = "Không được để trống") @Future(message = "Hạn nộp phải sau ngày hôm nay") LocalDate deadline,
            WorkShift shift,
            Boolean urgent,
            @Min(value = 0, message = "Giờ từ 0 đến 24") @Max(value = 24, message = "Giờ từ 0 đến 24") Integer startHour,
            @Min(value = 0, message = "Giờ từ 0 đến 24") @Max(value = 24, message = "Giờ từ 0 đến 24") Integer endHour) {

        /** Chữ ký có ca + gấp, chưa có giờ. */
        public JobRequest(String title, Integer categoryId, String description, Integer salaryMin, Integer salaryMax,
                          SalaryUnit salaryUnit, String location, String workTime, Integer quantity,
                          LocalDate deadline, WorkShift shift, Boolean urgent) {
            this(title, categoryId, description, salaryMin, salaryMax, salaryUnit, location, workTime, quantity,
                    deadline, shift, urgent, null, null);
        }

        /** Giữ chữ ký cũ (không ca, không gấp) cho code/test đã có. */
        public JobRequest(String title, Integer categoryId, String description, Integer salaryMin, Integer salaryMax,
                          SalaryUnit salaryUnit, String location, String workTime, Integer quantity,
                          LocalDate deadline) {
            this(title, categoryId, description, salaryMin, salaryMax, salaryUnit, location, workTime, quantity,
                    deadline, null, null, null, null);
        }
    }

    public record StatusRequest(@NotNull(message = "Vui lòng chọn trạng thái") JobStatus status) {
    }

    public record HideRequest(boolean hidden, @Size(max = 255) String reason) {
    }

    /** Dùng cho danh sách (trang chủ, tin của tôi, admin). */
    public record JobSummary(Long id, String title, String categoryName, String companyName, Integer salaryMin,
                             Integer salaryMax, SalaryUnit salaryUnit, String location, String workTime,
                             Integer quantity, LocalDate deadline, JobStatus status, String hiddenReason,
                             LocalDateTime createdAt, Long employerId, long pendingCount, long acceptedCount,
                             WorkShift shift, boolean urgent, Integer startHour, Integer endHour) {
        public static JobSummary from(JobPost j, String companyName, long pending, long accepted) {
            return new JobSummary(j.getId(), j.getTitle(), j.getCategory().getName(), companyName, j.getSalaryMin(),
                    j.getSalaryMax(), j.getSalaryUnit(), j.getLocation(), j.getWorkTime(), j.getQuantity(),
                    j.getDeadline(), j.getStatus(), j.getHiddenReason(), j.getCreatedAt(), j.getEmployer().getId(),
                    pending, accepted, j.getShift(), j.isUrgent(), j.getStartHour(), j.getEndHour());
        }
    }

    /** Chi tiết tin + thông tin NTD (FR-11). */
    public record JobDetail(Long id, String title, Integer categoryId, String categoryName, String description,
                            Integer salaryMin, Integer salaryMax, SalaryUnit salaryUnit, String location,
                            String workTime, Integer quantity, long acceptedCount, LocalDate deadline,
                            JobStatus status, LocalDateTime createdAt, ProfileDtos.EmployerProfileResponse employer,
                            WorkShift shift, boolean urgent, Integer startHour, Integer endHour) {
        public static JobDetail from(JobPost j, long accepted, ProfileDtos.EmployerProfileResponse employer) {
            return new JobDetail(j.getId(), j.getTitle(), j.getCategory().getId(), j.getCategory().getName(),
                    j.getDescription(), j.getSalaryMin(), j.getSalaryMax(), j.getSalaryUnit(), j.getLocation(),
                    j.getWorkTime(), j.getQuantity(), accepted, j.getDeadline(), j.getStatus(), j.getCreatedAt(),
                    employer, j.getShift(), j.isUrgent(), j.getStartHour(), j.getEndHour());
        }
    }
}
