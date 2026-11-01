package vn.vlu.svjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vlu.svjob.entity.JobCategory;

import java.util.List;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Integer> {

    List<JobCategory> findByActiveTrueOrderByNameAsc();

    List<JobCategory> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
