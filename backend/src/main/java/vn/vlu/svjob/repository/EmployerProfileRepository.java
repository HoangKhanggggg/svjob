package vn.vlu.svjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vlu.svjob.entity.EmployerProfile;

import java.util.Optional;

public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, Long> {

    Optional<EmployerProfile> findByUserId(Long userId);
}
