package vn.vlu.svjob.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.CommonDtos.PageResponse;
import vn.vlu.svjob.dto.JobDtos.JobDetail;
import vn.vlu.svjob.dto.JobDtos.JobRequest;
import vn.vlu.svjob.dto.JobDtos.JobSummary;
import vn.vlu.svjob.entity.ApplicationStatus;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.ApplicationRepository;
import vn.vlu.svjob.repository.JobPostRepository;
import vn.vlu.svjob.repository.JobSpecs;
import vn.vlu.svjob.repository.UserRepository;
import vn.vlu.svjob.security.UserPrincipal;

import java.util.List;

/** Tin tuyển dụng: đăng tin, xem chi tiết, tin của tôi, tìm kiếm (FR-06, FR-09 → FR-11). */
@Service
public class JobService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final ProfileService profileService;

    public JobService(JobPostRepository jobPostRepository, ApplicationRepository applicationRepository,
                      UserRepository userRepository, CategoryService categoryService, ProfileService profileService) {
        this.jobPostRepository = jobPostRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.profileService = profileService;
    }

    // ---------- Xem / tìm kiếm (công khai) ----------

    /** FR-09, FR-10: chỉ tin Đang tuyển, mới nhất lên đầu, tối đa 20 tin/trang. */
    @Transactional(readOnly = true)
    public PageResponse<JobSummary> search(String keyword, Integer categoryId, String location,
                                           Integer minSalary, Integer maxSalary, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 20),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobPost> result = jobPostRepository.findAll(
                JobSpecs.filter(JobStatus.OPEN, keyword, categoryId, location, minSalary, maxSalary), pageable);
        return PageResponse.of(result.map(j -> JobSummary.from(j, profileService.companyName(j.getEmployer().getId()), 0, 0)));
    }

    /** FR-11. Tin bị Admin ẩn thì chỉ chủ tin và Admin còn xem được. */
    @Transactional(readOnly = true)
    public JobDetail detail(Long jobId, UserPrincipal viewer) {
        JobPost job = find(jobId);
        if (job.getStatus() == JobStatus.HIDDEN) {
            boolean allowed = viewer != null && (viewer.role() == Role.ADMIN || isOwner(job, viewer.id()));
            if (!allowed) {
                throw ApiException.notFound("Tin tuyển dụng không tồn tại hoặc đã bị ẩn");
            }
        }
        long accepted = applicationRepository.countByJobIdAndStatus(jobId, ApplicationStatus.ACCEPTED);
        return JobDetail.from(job, accepted, profileService.toEmployerResponse(job.getEmployer()));
    }

    // ---------- Nhà tuyển dụng ----------

    @Transactional
    public JobDetail create(Long employerId, JobRequest req) {
        validateSalary(req);
        User employer = userRepository.getReferenceById(employerId);
        JobPost job = new JobPost();
        job.setEmployer(employer);
        apply(job, req);
        job.setStatus(JobStatus.OPEN);
        jobPostRepository.save(job);
        return JobDetail.from(job, 0, profileService.toEmployerResponse(userRepository.findById(employerId).orElseThrow()));
    }

    @Transactional(readOnly = true)
    public List<JobSummary> myJobs(Long employerId) {
        String company = profileService.companyName(employerId);
        return jobPostRepository.findByEmployerIdOrderByCreatedAtDesc(employerId).stream()
                .map(j -> JobSummary.from(j, company,
                        applicationRepository.countByJobIdAndStatus(j.getId(), ApplicationStatus.PENDING),
                        applicationRepository.countByJobIdAndStatus(j.getId(), ApplicationStatus.ACCEPTED)))
                .toList();
    }

    // ---------- Hàm dùng chung ----------

    JobPost find(Long jobId) {
        return jobPostRepository.findById(jobId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy tin tuyển dụng"));
    }

    /** BR-03: chỉ chủ tin được thao tác. */
    JobPost findOwned(Long jobId, Long employerId) {
        JobPost job = find(jobId);
        if (!isOwner(job, employerId)) {
            throw ApiException.forbidden("Bạn không có quyền với tin này");
        }
        return job;
    }

    static boolean isOwner(JobPost job, Long userId) {
        return job.getEmployer().getId().equals(userId);
    }

    private void validateSalary(JobRequest req) {
        if (req.salaryMax() != null && req.salaryMax() < req.salaryMin()) {
            throw ApiException.badRequest("Lương đến phải lớn hơn hoặc bằng lương từ");
        }
    }

    private void apply(JobPost job, JobRequest req) {
        job.setTitle(req.title().trim());
        job.setCategory(categoryService.findActive(req.categoryId()));
        job.setDescription(req.description().trim());
        job.setSalaryMin(req.salaryMin());
        job.setSalaryMax(req.salaryMax());
        job.setSalaryUnit(req.salaryUnit());
        job.setLocation(req.location().trim());
        job.setWorkTime(req.workTime().trim());
        job.setQuantity(req.quantity());
        job.setDeadline(req.deadline());
    }
}
