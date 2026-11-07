package vn.vlu.svjob.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.vlu.svjob.dto.ReviewDtos.ReviewRequest;
import vn.vlu.svjob.entity.*;
import vn.vlu.svjob.repository.ApplicationRepository;
import vn.vlu.svjob.repository.ReviewRepository;
import vn.vlu.svjob.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static vn.vlu.svjob.service.TestData.*;

/** UT-12, UT-13 (UT-14 rating = 6 được chặn bởi @Max ở ReviewRequest, kiểm tra bằng Postman). */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock UserRepository userRepository;
    @Mock JobService jobService;
    @Mock NotificationService notificationService;
    @InjectMocks ReviewService reviewService;

    private final User employer = user(2, Role.EMPLOYER);
    private final User student = user(3, Role.STUDENT);

    @Test
    void UT12_danhGiaKhiTinChuaHoanThanh_bao409() {
        when(jobService.find(10L)).thenReturn(job(10, employer, JobStatus.FILLED, 1));

        assertThatThrownBy(() -> reviewService.create(3L, 10L, new ReviewRequest(2L, 5, "Tốt")))
                .hasMessage("Chỉ đánh giá được khi công việc đã Hoàn thành")
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void UT13_danhGiaLan2_bao409() {
        JobPost job = job(10, employer, JobStatus.COMPLETED, 1);
        when(jobService.find(10L)).thenReturn(job);
        when(applicationRepository.findByJobIdAndStudentId(10L, 3L))
                .thenReturn(Optional.of(application(5, job, student, ApplicationStatus.ACCEPTED)));
        when(reviewRepository.existsByJobIdAndReviewerIdAndRevieweeId(10L, 3L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(3L, 10L, new ReviewRequest(2L, 4, null)))
                .hasMessage("Bạn đã đánh giá người này cho công việc này");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void nguoiKhongThamGiaDanhGia_bao403() {
        JobPost job = job(10, employer, JobStatus.COMPLETED, 1);
        when(jobService.find(10L)).thenReturn(job);
        when(applicationRepository.findByJobIdAndStudentId(10L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.create(7L, 10L, new ReviewRequest(2L, 5, null)))
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }
}
