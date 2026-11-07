package vn.vlu.svjob.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.ReviewDtos.ReviewItem;
import vn.vlu.svjob.dto.ReviewDtos.ReviewRequest;
import vn.vlu.svjob.dto.ReviewDtos.UserReviews;
import vn.vlu.svjob.entity.ApplicationStatus;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.Review;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.ApplicationRepository;
import vn.vlu.svjob.repository.ReviewRepository;
import vn.vlu.svjob.repository.UserRepository;

/** Đánh giá hai chiều (FR-18, BR-10) và điểm trung bình (FR-19). */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobService jobService;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository, ApplicationRepository applicationRepository,
                         UserRepository userRepository, JobService jobService, NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobService = jobService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReviewItem create(Long reviewerId, Long jobId, ReviewRequest req) {
        JobPost job = jobService.find(jobId);
        if (job.getStatus() != JobStatus.COMPLETED) {
            throw ApiException.conflict("Chỉ đánh giá được khi công việc đã Hoàn thành");
        }
        Long employerId = job.getEmployer().getId();
        Long revieweeId = req.revieweeId();
        boolean employerReviewsStudent = reviewerId.equals(employerId) && wasAccepted(jobId, revieweeId);
        boolean studentReviewsEmployer = revieweeId.equals(employerId) && wasAccepted(jobId, reviewerId);
        if (!employerReviewsStudent && !studentReviewsEmployer) {
            throw ApiException.forbidden("Bạn không tham gia công việc này nên không thể đánh giá");
        }
        if (reviewRepository.existsByJobIdAndReviewerIdAndRevieweeId(jobId, reviewerId, revieweeId)) {
            throw ApiException.conflict("Bạn đã đánh giá người này cho công việc này");
        }
        User reviewer = userRepository.findById(reviewerId).orElseThrow();
        User reviewee = userRepository.findById(revieweeId).orElseThrow();
        Review r = new Review();
        r.setJob(job);
        r.setReviewer(reviewer);
        r.setReviewee(reviewee);
        r.setRating(req.rating());
        r.setComment(AuthService.blankToNull(req.comment()));
        reviewRepository.save(r);

        notificationService.notify(reviewee, reviewer.getFullName() + " đã đánh giá bạn " + req.rating()
                + " sao cho công việc “" + job.getTitle() + "”", "/reviews/" + revieweeId);
        return toItem(r);
    }

    @Transactional(readOnly = true)
    public UserReviews ofUser(Long userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Không tìm thấy người dùng"));
        Double avg = reviewRepository.averageRating(userId);
        var items = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId).stream().map(ReviewService::toItem).toList();
        return new UserReviews(userId, u.getFullName(), avg == null ? null : Math.round(avg * 10) / 10.0,
                items.size(), items);
    }

    private boolean wasAccepted(Long jobId, Long studentId) {
        return applicationRepository.findByJobIdAndStudentId(jobId, studentId)
                .map(a -> a.getStatus() == ApplicationStatus.ACCEPTED).orElse(false);
    }

    private static ReviewItem toItem(Review r) {
        return new ReviewItem(r.getId(), r.getJob().getId(), r.getJob().getTitle(), r.getReviewer().getFullName(),
                r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
