package vn.vlu.svjob.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.vlu.svjob.entity.EmployerProfile;
import vn.vlu.svjob.entity.StudentProfile;
import vn.vlu.svjob.entity.User;

/** Hồ sơ sinh viên (FR-04) và nhà tuyển dụng (FR-05). */
public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record StudentProfileRequest(
            @NotBlank(message = "Họ tên không được để trống") @Size(max = 100) String fullName,
            @Pattern(regexp = "^$|^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
            @Size(max = 150) String school,
            @Size(max = 100) String major,
            @Min(value = 1, message = "Năm học từ 1 đến 6") @Max(value = 6, message = "Năm học từ 1 đến 6") Integer yearOfStudy,
            @Size(max = 500, message = "Kỹ năng tối đa 500 ký tự") String skills,
            @Size(max = 2000, message = "Giới thiệu tối đa 2000 ký tự") String bio) {
    }

    public record StudentProfileResponse(Long userId, String email, String fullName, String phone, String school,
                                         String major, Integer yearOfStudy, String skills, String bio, boolean hasCv,
                                         Double avgRating, long reviewCount) {
        public static StudentProfileResponse from(User u, StudentProfile p, Double avg, long count) {
            return new StudentProfileResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), p.getSchool(),
                    p.getMajor(), p.getYearOfStudy(), p.getSkills(), p.getBio(), p.getCvUrl() != null, avg, count);
        }
    }

    public record EmployerProfileRequest(
            @NotBlank(message = "Họ tên người đại diện không được để trống") @Size(max = 100) String fullName,
            @NotBlank(message = "Tên đơn vị không được để trống") @Size(max = 150) String companyName,
            @Size(max = 255) String address,
            @Size(max = 2000, message = "Giới thiệu tối đa 2000 ký tự") String description,
            @Pattern(regexp = "^$|^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String contactPhone) {
    }

    public record EmployerProfileResponse(Long userId, String email, String fullName, String companyName,
                                          String address, String description, String contactPhone,
                                          Double avgRating, long reviewCount) {
        public static EmployerProfileResponse from(User u, EmployerProfile p, Double avg, long count) {
            return new EmployerProfileResponse(u.getId(), u.getEmail(), u.getFullName(), p.getCompanyName(),
                    p.getAddress(), p.getDescription(), p.getContactPhone(), avg, count);
        }
    }
}
