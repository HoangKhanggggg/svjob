package vn.vlu.svjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vlu.svjob.entity.StudentProfile;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);
}
