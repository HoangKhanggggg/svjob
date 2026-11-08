package vn.vlu.svjob.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.vlu.svjob.entity.*;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.ApplicationRepository;
import vn.vlu.svjob.repository.JobPostRepository;
import vn.vlu.svjob.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static vn.vlu.svjob.service.TestData.*;

/** UT-04 → UT-07. */
@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobPostRepository jobPostRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock UserRepository userRepository;
    @Mock CategoryService categoryService;
    @Mock ProfileService profileService;
    @Mock NotificationService notificationService;
    @InjectMocks JobService jobService;

    private final User owner = user(2, Role.EMPLOYER);

    @Test
    void UT04_luongTuLonHonLuongDen_bao400() {
        assertThatThrownBy(() -> jobService.create(2L, jobRequest(30000, 25000)))
                .hasMessage("Lương đến phải lớn hơn hoặc bằng lương từ")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(jobPostRepository, never()).save(any());
    }

    @Test
    void taoTinCoCaLamVaTuyenGap_luuDungGiaTri() {
        when(categoryService.findActive(1)).thenReturn(new JobCategory());
        when(userRepository.getReferenceById(2L)).thenReturn(owner);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(jobPostRepository.save(any(JobPost.class))).thenAnswer(i -> i.getArgument(0));
        var req = new vn.vlu.svjob.dto.JobDtos.JobRequest("Phục vụ", 1, "Mô tả", 25000, 30000, SalaryUnit.HOUR,
                "Bình Thạnh", "Ca tối", 2, java.time.LocalDate.now().plusDays(10), WorkShift.EVENING, true);

        jobService.create(2L, req);

        var cap = org.mockito.ArgumentCaptor.forClass(JobPost.class);
        verify(jobPostRepository).save(cap.capture());
        assertThat(cap.getValue().getShift()).isEqualTo(WorkShift.EVENING);
        assertThat(cap.getValue().isUrgent()).isTrue();
    }

    @Test
    void taoTinKhongChonCa_macDinhLinhHoatVaKhongGap() {
        when(categoryService.findActive(1)).thenReturn(new JobCategory());
        when(userRepository.getReferenceById(2L)).thenReturn(owner);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(jobPostRepository.save(any(JobPost.class))).thenAnswer(i -> i.getArgument(0));

        jobService.create(2L, jobRequest(25000, 30000));

        var cap = org.mockito.ArgumentCaptor.forClass(JobPost.class);
        verify(jobPostRepository).save(cap.capture());
        assertThat(cap.getValue().getShift()).isEqualTo(WorkShift.FLEXIBLE);
        assertThat(cap.getValue().isUrgent()).isFalse();
    }

    @Test
    void taoTinGioKetThucTruocGioBatDau_bao400() {
        var req = new vn.vlu.svjob.dto.JobDtos.JobRequest("Phục vụ", 1, "Mô tả", 25000, 30000, SalaryUnit.HOUR,
                "Bình Thạnh", "18h-12h", 2, java.time.LocalDate.now().plusDays(10), WorkShift.EVENING, false, 18, 12);
        assertThatThrownBy(() -> jobService.create(2L, req))
                .hasMessage("Giờ kết thúc phải sau giờ bắt đầu")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(jobPostRepository, never()).save(any());
    }

    @Test
    void taoTinChiNhapMotGio_bao400() {
        var req = new vn.vlu.svjob.dto.JobDtos.JobRequest("Phục vụ", 1, "Mô tả", 25000, 30000, SalaryUnit.HOUR,
                "Bình Thạnh", "từ 18h", 2, java.time.LocalDate.now().plusDays(10), WorkShift.EVENING, false, 18, null);
        assertThatThrownBy(() -> jobService.create(2L, req)).hasMessage("Cần nhập cả giờ bắt đầu và giờ kết thúc");
    }

    @Test
    void timKiemKhungGioNguocNhau_bao400() {
        assertThatThrownBy(() -> jobService.search(null, null, null, null, null, null, null, 22, 17, 0, 10))
                .hasMessage("Giờ bắt đầu phải nhỏ hơn giờ kết thúc");
    }

    @Test
    void UT05_ntdKhacSuaTin_bao403() {
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(job(10, owner, JobStatus.OPEN, 2)));

        assertThatThrownBy(() -> jobService.update(99L, 10L, jobRequest(25000, 30000)))
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void UT06_xoaTinDaCoUngVienDuocNhan_bao409() {
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(job(10, owner, JobStatus.OPEN, 2)));
        when(applicationRepository.countByJobIdAndStatus(10L, ApplicationStatus.ACCEPTED)).thenReturn(1L);

        assertThatThrownBy(() -> jobService.delete(2L, 10L))
                .hasMessage("Tin đã có ứng viên được nhận, bạn chỉ có thể đóng tin")
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verify(jobPostRepository, never()).delete(any(JobPost.class));
    }

    @Test
    void UT07_chuyenDangTuyenSangHoanThanh_boQuaBuoc_bao409VaGiuNguyen() {
        JobPost job = job(10, owner, JobStatus.OPEN, 2);
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.changeStatus(2L, 10L, JobStatus.COMPLETED))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        assertThat(job.getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    void chuyenSangDaNhanNguoi_cacDonConChoDuyetTuBiTuChoi() {
        JobPost job = job(10, owner, JobStatus.OPEN, 2);
        Application pending = application(1, job, user(3, Role.STUDENT), ApplicationStatus.PENDING);
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.countByJobIdAndStatus(10L, ApplicationStatus.ACCEPTED)).thenReturn(1L);
        when(applicationRepository.findByJobIdAndStatus(10L, ApplicationStatus.PENDING)).thenReturn(List.of(pending));

        jobService.changeStatus(2L, 10L, JobStatus.FILLED);

        assertThat(job.getStatus()).isEqualTo(JobStatus.FILLED);
        assertThat(pending.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(notificationService).notify(any(), any(), any());
    }

    @Test
    void chuyenSangDaNhanNguoiKhiChuaNhanAi_bao409() {
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(job(10, owner, JobStatus.OPEN, 2)));
        when(applicationRepository.countByJobIdAndStatus(10L, ApplicationStatus.ACCEPTED)).thenReturn(0L);

        assertThatThrownBy(() -> jobService.changeStatus(2L, 10L, JobStatus.FILLED))
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void adminAnTinKhongCoLyDo_bao400() {
        when(jobPostRepository.findById(10L)).thenReturn(Optional.of(job(10, owner, JobStatus.OPEN, 2)));
        assertThatThrownBy(() -> jobService.hide(10L, true, " "))
                .hasMessage("Vui lòng nhập lý do ẩn tin");
    }
}
