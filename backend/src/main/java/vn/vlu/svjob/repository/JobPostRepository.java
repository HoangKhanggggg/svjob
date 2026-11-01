package vn.vlu.svjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.vlu.svjob.entity.JobPost;

import java.util.List;

/** JpaSpecificationExecutor dùng cho tìm kiếm/lọc kết hợp nhiều điều kiện (FR-10), xem JobSpecs. */
public interface JobPostRepository extends JpaRepository<JobPost, Long>, JpaSpecificationExecutor<JobPost> {

    List<JobPost> findByEmployerIdOrderByCreatedAtDesc(Long employerId);
}
