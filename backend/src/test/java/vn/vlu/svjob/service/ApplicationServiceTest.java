package vn.vlu.svjob.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.vlu.svjob.entity.*;
import vn.vlu.svjob.repository.ApplicationRepository;
import vn.vlu.svjob.repository.ReviewRepository;
import vn.vlu.svjob.repository.StudentProfileRepository;
import vn.vlu.svjob.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static vn.vlu.svjob.service.TestData.*;

/** UT-08 → UT-11. */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock UserRepository userRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock JobService jobService;
    @Mock ProfileService profileService;
    @Mock FileStorageService fileStorageService;
    @Mock NotificationService notificationService;
    @InjectMocks ApplicationService applicationService;

    private final User employer = user(2, Role.EMPLOYER);
    private final User student = user(3, Role.STUDENT);

    @Test
    void UT08_ungTuyenTinDaDong_bao409() {
        when(jobService.find(10L)).thenReturn(job(10, employer, JobStatus.CLOSED, 2));

        assertThatThrownBy(() -> applicationService.apply(3L, 10L, null, "Em muốn ứng tuyển", false))
                .hasMessage("Tin đã ngừng nhận hồ sơ")
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void UT09_ungTuyenLan2CungTin_bao409() {
        when(jobService.find(10L)).thenReturn(job(10, employer, JobStatus.OPEN, 2));
        when(applicationRepository.existsByJobIdAndStudentId(10L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply(3L, 10L, null, "Lần 2", false))
                .hasMessage("Bạn đã ứng tuyển công việc này");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void ungTuyenKhongCvKhongLoiGioiThieu_bao400() {
        when(jobService.find(10L)).thenReturn(job(10, employer, JobStatus.OPEN, 2));

        assertThatThrownBy(() -> applicationService.apply(3L, 10L, null, "  ", false))
                .hasMessage("Cần đính kèm CV hoặc viết lời giới thiệu")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void UT10_duyetKhiDaDuSoLuong_bao409() {
        JobPost job = job(10, employer, JobStatus.OPEN, 1);
        Application a = application(5, job, student, ApplicationStatus.PENDING);
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(a));
        when(jobService.findOwned(10L, 2L)).thenReturn(job);
        when(applicationRepository.countByJobIdAndStatus(10L, ApplicationStatus.ACCEPTED)).thenReturn(1L);

        assertThatThrownBy(() -> applicationService.decide(2L, 5L, ApplicationStatus.ACCEPTED))
                .hasMessage("Tin đã nhận đủ số lượng");
        assertThat(a.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void duyetDonHopLe_ghiThoiDiemXuLyVaGuiThongBao() {
        JobPost job = job(10, employer, JobStatus.OPEN, 2);
        Application a = application(5, job, student, ApplicationStatus.PENDING);
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(a));
        when(jobService.findOwned(10L, 2L)).thenReturn(job);
        when(applicationRepository.countByJobIdAndStatus(10L, ApplicationStatus.ACCEPTED)).thenReturn(0L);

        applicationService.decide(2L, 5L, ApplicationStatus.ACCEPTED);

        assertThat(a.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(a.getProcessedAt()).isNotNull();
        verify(notificationService).notify(eq(student), any(), any());
    }

    @Test
    void UT11_rutDonDaDuocNhan_bao409() {
        Application a = application(5, job(10, employer, JobStatus.OPEN, 2), student, ApplicationStatus.ACCEPTED);
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> applicationService.withdraw(3L, 5L))
                .hasMessage("Chỉ rút được đơn đang chờ duyệt");
        assertThat(a.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    }
}
