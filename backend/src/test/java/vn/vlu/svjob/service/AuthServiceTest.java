package vn.vlu.svjob.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.vlu.svjob.dto.AuthDtos.LoginRequest;
import vn.vlu.svjob.dto.AuthDtos.RegisterRequest;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.entity.UserStatus;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.EmployerProfileRepository;
import vn.vlu.svjob.repository.StudentProfileRepository;
import vn.vlu.svjob.repository.UserRepository;
import vn.vlu.svjob.security.JwtUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UT-01 → UT-03 (sheet Unit test trong Kiem_thu.xlsx). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock EmployerProfileRepository employerProfileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    private RegisterRequest student(String email) {
        return new RegisterRequest(email, "Abc12345", "Nguyễn Văn An", "", Role.STUDENT, null);
    }

    @Test
    void UT01_dangKyEmailDaTonTai_bao409VaKhongLuu() {
        when(userRepository.existsByEmail("an@vlu.vn")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(student("an@vlu.vn")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Email đã được sử dụng")
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void UT02_dangNhapTaiKhoanBiKhoa_bao403() {
        User u = new User();
        u.setEmail("an@vlu.vn");
        u.setPasswordHash("hash");
        u.setRole(Role.STUDENT);
        u.setStatus(UserStatus.LOCKED);
        when(userRepository.findByEmail("an@vlu.vn")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Abc12345", "hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("an@vlu.vn", "Abc12345")))
                .hasMessage("Tài khoản đã bị khóa")
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void UT03_dangKyThanhCong_matKhauDuocMaHoaTruocKhiLuu() {
        when(userRepository.existsByEmail("an@vlu.vn")).thenReturn(false);
        when(passwordEncoder.encode("Abc12345")).thenReturn("$2a$10$hash");

        authService.register(student("An@VLU.vn "));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("$2a$10$hash").isNotEqualTo("Abc12345");
        assertThat(saved.getValue().getEmail()).isEqualTo("an@vlu.vn");
        verify(studentProfileRepository).save(any());
    }

    @Test
    void dangNhapSaiEmailHaySaiMatKhau_cungMotThongBao() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("khongco@vlu.vn", "x")))
                .hasMessage(AuthService.WRONG_LOGIN);
    }
}
