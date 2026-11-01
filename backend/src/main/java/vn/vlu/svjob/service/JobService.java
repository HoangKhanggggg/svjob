package vn.vlu.svjob.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.JobDtos.JobDetail;
import vn.vlu.svjob.dto.JobDtos.JobRequest;
import vn.vlu.svjob.dto.JobDtos.JobSummary;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.JobPostRepository;
import vn.vlu.svjob.repository.UserRepository;
import vn.vlu.svjob.security.UserPrincipal;

import java.util.List;

/** Tin tuyển dụng: đăng tin, xem chi tiết, tin của tôi (FR-06, FR-11). */
@Service
public class JobService {

    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final ProfileService profileService;

    public JobService(JobPostRepository jobPostRepository, UserRepository userRepository,
                      CategoryService categoryService, ProfileService profileService) {
        this.jobPostRepository = jobPostRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.profileService = profileService;
    }

    // ---------- Xem (công khai) ----------

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
        // Số người đã nhận sẽ tính khi có chức năng ứng tuyển (T7.4)
        return JobDetail.from(job, 0, profileService.toEmployerResponse(job.getEmployer()));
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
                .map(j -> JobSummary.from(j, company, 0, 0))
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
