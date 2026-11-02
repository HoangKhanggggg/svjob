package vn.vlu.svjob.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.vlu.svjob.dto.ApplicationDtos.Applicant;
import vn.vlu.svjob.dto.ApplicationDtos.ApplicationResult;
import vn.vlu.svjob.entity.Application;
import vn.vlu.svjob.entity.ApplicationStatus;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.StudentProfile;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.ApplicationRepository;
import vn.vlu.svjob.repository.StudentProfileRepository;
import vn.vlu.svjob.repository.UserRepository;
import vn.vlu.svjob.security.UserPrincipal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static vn.vlu.svjob.service.AuthService.blankToNull;

/** Ứng tuyển, xem ứng viên, duyệt / từ chối (FR-12, FR-14, FR-15). */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final JobService jobService;
    private final ProfileService profileService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              StudentProfileRepository studentProfileRepository, UserRepository userRepository,
                              JobService jobService, ProfileService profileService,
                              FileStorageService fileStorageService, NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.jobService = jobService;
        this.profileService = profileService;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    /**
     * FR-12. Đơn phải có CV hoặc lời giới thiệu (BR-06). CV lấy từ file gửi kèm,
     * nếu không gửi file mà useProfileCv = true thì dùng CV đã tải lên ở hồ sơ.
     */
    @Transactional
    public ApplicationResult apply(Long studentId, Long jobId, MultipartFile cv, String coverLetter,
                                   boolean useProfileCv) {
        JobPost job = jobService.find(jobId);
        if (job.getStatus() != JobStatus.OPEN || job.getDeadline().isBefore(LocalDate.now())) {
            throw ApiException.conflict("Tin đã ngừng nhận hồ sơ");
        }
        if (applicationRepository.existsByJobIdAndStudentId(jobId, studentId)) {       // BR-05
            throw ApiException.conflict("Bạn đã ứng tuyển công việc này");
        }
        String letter = blankToNull(coverLetter);
        if (letter != null && letter.length() > 1000) {
            throw ApiException.badRequest("Lời giới thiệu tối đa 1000 ký tự");
        }
        String cvUrl = null;
        if (cv != null && !cv.isEmpty()) {
            cvUrl = fileStorageService.storeCv(cv);
        } else if (useProfileCv) {
            cvUrl = studentProfileRepository.findByUserId(studentId).map(StudentProfile::getCvUrl).orElse(null);
        }
        if (cvUrl == null && letter == null) {
            throw ApiException.badRequest("Cần đính kèm CV hoặc viết lời giới thiệu");
        }
        User student = userRepository.findById(studentId).orElseThrow();
        Application a = new Application();
        a.setJob(job);
        a.setStudent(student);
        a.setCoverLetter(letter);
        a.setCvUrl(cvUrl);
        a.setStatus(ApplicationStatus.PENDING);
        applicationRepository.save(a);

        notificationService.notify(job.getEmployer(), student.getFullName() + " vừa ứng tuyển tin “"
                + job.getTitle() + "”", "/employer/jobs/" + job.getId() + "/applicants");
        return toResult(a);
    }

    /** FR-14: NTD chỉ xem ứng viên của tin mình. */
    @Transactional(readOnly = true)
    public List<Applicant> applicants(Long employerId, Long jobId) {
        jobService.findOwned(jobId, employerId);
        return applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId).stream().map(a -> {
            User s = a.getStudent();
            StudentProfile p = studentProfileRepository.findByUserId(s.getId()).orElse(new StudentProfile(s));
            return new Applicant(a.getId(), s.getId(), s.getFullName(), s.getEmail(), s.getPhone(), p.getSchool(),
                    p.getMajor(), p.getYearOfStudy(), p.getSkills(), p.getBio(), a.getCoverLetter(),
                    a.getCvUrl() != null, a.getStatus(), a.getAppliedAt(), a.getProcessedAt(),
                    null, false);
        }).toList();
    }

    /** FR-15: duyệt / từ chối, không vượt số lượng cần tuyển (BR-08), ghi thời điểm xử lý. */
    @Transactional
    public ApplicationResult decide(Long employerId, Long applicationId, ApplicationStatus decision) {
        if (decision != ApplicationStatus.ACCEPTED && decision != ApplicationStatus.REJECTED) {
            throw ApiException.badRequest("Chỉ được chọn ACCEPTED hoặc REJECTED");
        }
        Application a = find(applicationId);
        JobPost job = jobService.findOwned(a.getJob().getId(), employerId);
        if (a.getStatus() != ApplicationStatus.PENDING) {
            throw ApiException.conflict("Đơn này đã được xử lý");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw ApiException.conflict("Tin không còn ở trạng thái Đang tuyển");
        }
        if (decision == ApplicationStatus.ACCEPTED
                && applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.ACCEPTED) >= job.getQuantity()) {
            throw ApiException.conflict("Tin đã nhận đủ số lượng");
        }
        a.setStatus(decision);
        a.setProcessedAt(LocalDateTime.now());
        String msg = decision == ApplicationStatus.ACCEPTED
                ? "Chúc mừng! Đơn ứng tuyển “" + job.getTitle() + "” của bạn đã được nhận"
                : "Đơn ứng tuyển “" + job.getTitle() + "” của bạn chưa phù hợp lần này";
        notificationService.notify(a.getStudent(), msg, "/student/applications");
        return toResult(a);
    }

    /** Tải CV của đơn: chỉ chủ tin hoặc chính SV nộp đơn (NFR-02). */
    @Transactional(readOnly = true)
    public Resource cvOfApplication(UserPrincipal user, Long applicationId) {
        Application a = find(applicationId);
        boolean isStudent = a.getStudent().getId().equals(user.id());
        boolean isJobOwner = JobService.isOwner(a.getJob(), user.id());
        if (!isStudent && !isJobOwner) {
            throw ApiException.forbidden("Bạn không có quyền xem CV này");
        }
        if (a.getCvUrl() == null) {
            throw ApiException.notFound("Đơn này không đính kèm CV");
        }
        return fileStorageService.loadCv(a.getCvUrl());
    }

    private Application find(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn ứng tuyển"));
    }

    private static ApplicationResult toResult(Application a) {
        return new ApplicationResult(a.getId(), a.getJob().getId(), a.getStudent().getFullName(), a.getStatus(),
                a.getProcessedAt());
    }
}
