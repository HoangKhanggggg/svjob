package vn.vlu.svjob.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.AuthDtos.LoginRequest;
import vn.vlu.svjob.dto.AuthDtos.LoginResponse;
import vn.vlu.svjob.dto.AuthDtos.RegisterRequest;
import vn.vlu.svjob.dto.AuthDtos.UserResponse;
import vn.vlu.svjob.entity.EmployerProfile;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.StudentProfile;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.entity.UserStatus;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.EmployerProfileRepository;
import vn.vlu.svjob.repository.StudentProfileRepository;
import vn.vlu.svjob.repository.UserRepository;
import vn.vlu.svjob.security.JwtUtil;

/** Đăng ký, đăng nhập (FR-01, FR-02). */
@Service
public class AuthService {

    static final String WRONG_LOGIN = "Email hoặc mật khẩu không đúng";

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, StudentProfileRepository studentProfileRepository,
                       EmployerProfileRepository employerProfileRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (req.role() == Role.ADMIN) {
            throw ApiException.badRequest("Chỉ được đăng ký vai trò Sinh viên hoặc Nhà tuyển dụng");
        }
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {                       // BR-01
            throw ApiException.conflict("Email đã được sử dụng");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));    // NFR-02: lưu dạng băm BCrypt
        user.setFullName(req.fullName().trim());
        user.setPhone(blankToNull(req.phone()));
        user.setRole(req.role());
        userRepository.save(user);

        // Tạo sẵn hồ sơ rỗng theo vai trò để người dùng vào sửa sau
        if (req.role() == Role.STUDENT) {
            studentProfileRepository.save(new StudentProfile(user));
        } else {
            String company = blankToNull(req.companyName());
            employerProfileRepository.save(new EmployerProfile(user, company != null ? company : user.getFullName()));
        }
        return UserResponse.from(user);
    }

    public LoginResponse login(LoginRequest req) {
        // Sai email hay sai mật khẩu đều báo cùng 1 câu để không lộ email nào đã tồn tại (BR-02)
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, WRONG_LOGIN));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, WRONG_LOGIN);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw ApiException.forbidden("Tài khoản đã bị khóa");
        }
        return new LoginResponse(jwtUtil.generateToken(user), UserResponse.from(user));
    }

    public UserResponse me(Long userId) {
        return UserResponse.from(userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy tài khoản")));
    }

    static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
