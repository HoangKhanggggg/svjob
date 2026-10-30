package vn.vlu.svjob.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.User;

/** Dữ liệu vào/ra cho nhóm API Auth (FR-01, FR-02). */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không đúng định dạng")
            @Size(max = 100, message = "Email tối đa 100 ký tự")
            String email,

            @NotBlank(message = "Mật khẩu không được để trống")
            @Size(min = 8, max = 64, message = "Mật khẩu tối thiểu 8 ký tự")
            String password,

            @NotBlank(message = "Họ tên không được để trống")
            @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
            String fullName,

            @Pattern(regexp = "^$|^0\\d{9,10}$", message = "Số điện thoại không hợp lệ")
            String phone,

            @NotNull(message = "Vui lòng chọn vai trò")
            Role role,

            /** Chỉ dùng khi role = EMPLOYER; bỏ trống thì lấy họ tên. */
            @Size(max = 150, message = "Tên đơn vị tối đa 150 ký tự")
            String companyName) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email không được để trống") String email,
            @NotBlank(message = "Mật khẩu không được để trống") String password) {
    }

    public record UserResponse(Long id, String email, String fullName, String phone, String role, String status,
                               java.time.LocalDateTime createdAt) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(),
                    u.getRole().name(), u.getStatus().name(), u.getCreatedAt());
        }
    }

    public record LoginResponse(String token, UserResponse user) {
    }
}
