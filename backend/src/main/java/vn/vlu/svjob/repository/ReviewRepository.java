package vn.vlu.svjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vlu.svjob.entity.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByJobIdAndReviewerIdAndRevieweeId(Long jobId, Long reviewerId, Long revieweeId);

    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    /** Điểm trung bình (FR-19); trả null nếu chưa có đánh giá. */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double averageRating(@Param("userId") Long userId);

    long countByRevieweeId(Long revieweeId);
}
