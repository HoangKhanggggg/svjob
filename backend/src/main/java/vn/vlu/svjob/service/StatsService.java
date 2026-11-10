package vn.vlu.svjob.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.CommonDtos.CategoryCount;
import vn.vlu.svjob.dto.CommonDtos.Stats;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.repository.JobPostRepository;
import vn.vlu.svjob.repository.UserRepository;

/** Số liệu tổng quan hiển thị ở trang chủ. */
@Service
public class StatsService {

    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;

    public StatsService(JobPostRepository jobPostRepository, UserRepository userRepository) {
        this.jobPostRepository = jobPostRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Stats overview() {
        var categories = jobPostRepository.countOpenByCategory().stream()
                .map(r -> new CategoryCount((Integer) r[0], (String) r[1], ((Number) r[2]).longValue()))
                .toList();
        return new Stats(jobPostRepository.countByStatus(JobStatus.OPEN),
                jobPostRepository.countByStatus(JobStatus.COMPLETED),
                userRepository.countByRole(Role.EMPLOYER),
                userRepository.countByRole(Role.STUDENT),
                categories);
    }
}
