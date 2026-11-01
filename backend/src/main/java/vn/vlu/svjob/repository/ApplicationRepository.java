package vn.vlu.svjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vlu.svjob.entity.Application;
import vn.vlu.svjob.entity.ApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndStudentId(Long jobId, Long studentId);

    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);

    List<Application> findByJobIdOrderByAppliedAtDesc(Long jobId);

    List<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status);

    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    Optional<Application> findByJobIdAndStudentId(Long jobId, Long studentId);
}
