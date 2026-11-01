package vn.vlu.svjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;

import java.util.List;

/** JpaSpecificationExecutor dùng cho tìm kiếm/lọc kết hợp nhiều điều kiện (FR-10), xem JobSpecs. */
public interface JobPostRepository extends JpaRepository<JobPost, Long>, JpaSpecificationExecutor<JobPost> {

    List<JobPost> findByEmployerIdOrderByCreatedAtDesc(Long employerId);

    long countByStatus(JobStatus status);

    /** Số tin Đang tuyển theo từng ngành nghề, dùng cho trang chủ: [id, tên, số tin]. */
    @Query("""
            SELECT c.id, c.name, COUNT(j.id) FROM JobCategory c
            LEFT JOIN JobPost j ON j.category = c AND j.status = vn.vlu.svjob.entity.JobStatus.OPEN
            WHERE c.active = true
            GROUP BY c.id, c.name
            ORDER BY COUNT(j.id) DESC, c.name
            """)
    List<Object[]> countOpenByCategory();
}
