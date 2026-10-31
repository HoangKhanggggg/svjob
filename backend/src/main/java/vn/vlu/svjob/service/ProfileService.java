package vn.vlu.svjob.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.vlu.svjob.dto.ProfileDtos.EmployerProfileRequest;
import vn.vlu.svjob.dto.ProfileDtos.EmployerProfileResponse;
import vn.vlu.svjob.dto.ProfileDtos.StudentProfileRequest;
import vn.vlu.svjob.dto.ProfileDtos.StudentProfileResponse;
import vn.vlu.svjob.entity.EmployerProfile;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.StudentProfile;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.EmployerProfileRepository;
import vn.vlu.svjob.repository.StudentProfileRepository;
import vn.vlu.svjob.repository.UserRepository;

import static vn.vlu.svjob.service.AuthService.blankToNull;

/** Hồ sơ SV + upload CV (FR-04), hồ sơ NTD (FR-05). */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final FileStorageService fileStorageService;

    public ProfileService(UserRepository userRepository, StudentProfileRepository studentProfileRepository,
                          EmployerProfileRepository employerProfileRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.fileStorageService = fileStorageService;
    }

    // ---------- Sinh viên ----------

    @Transactional(readOnly = true)
    public StudentProfileResponse getStudent(Long userId) {
        User u = findUser(userId);
        // Điểm đánh giá sẽ có ở buổi 8 (FR-18)
        return StudentProfileResponse.from(u, studentProfile(userId), null, 0);
    }

    @Transactional
    public StudentProfileResponse updateStudent(Long userId, StudentProfileRequest req) {
        User u = findUser(userId);
        u.setFullName(req.fullName().trim());
        u.setPhone(blankToNull(req.phone()));
        StudentProfile p = studentProfile(userId);
        p.setSchool(blankToNull(req.school()));
        p.setMajor(blankToNull(req.major()));
        p.setYearOfStudy(req.yearOfStudy());
        p.setSkills(blankToNull(req.skills()));
        p.setBio(blankToNull(req.bio()));
        return getStudent(userId);
    }

    @Transactional
    public StudentProfileResponse uploadCv(Long userId, MultipartFile file) {
        StudentProfile p = studentProfile(userId);
        p.setCvUrl(fileStorageService.storeCv(file));
        return getStudent(userId);
    }

    @Transactional(readOnly = true)
    public Resource myCv(Long userId) {
        String cv = studentProfile(userId).getCvUrl();
        if (cv == null) {
            throw ApiException.notFound("Bạn chưa tải CV lên");
        }
        return fileStorageService.loadCv(cv);
    }

    // ---------- Nhà tuyển dụng ----------

    @Transactional(readOnly = true)
    public EmployerProfileResponse getEmployer(Long userId) {
        User u = findUser(userId);
        if (u.getRole() != Role.EMPLOYER) {
            throw ApiException.notFound("Không tìm thấy nhà tuyển dụng");
        }
        return toEmployerResponse(u);
    }

    @Transactional
    public EmployerProfileResponse updateEmployer(Long userId, EmployerProfileRequest req) {
        User u = findUser(userId);
        u.setFullName(req.fullName().trim());
        EmployerProfile p = employerProfile(userId);
        p.setCompanyName(req.companyName().trim());
        p.setAddress(blankToNull(req.address()));
        p.setDescription(blankToNull(req.description()));
        p.setContactPhone(blankToNull(req.contactPhone()));
        return toEmployerResponse(u);
    }

    /** Dùng chung cho chi tiết tin (FR-11). */
    EmployerProfileResponse toEmployerResponse(User u) {
        return EmployerProfileResponse.from(u, employerProfile(u.getId()), null, 0);
    }

    String companyName(Long employerId) {
        return employerProfileRepository.findByUserId(employerId).map(EmployerProfile::getCompanyName).orElse("");
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy tài khoản"));
    }

    StudentProfile studentProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy hồ sơ sinh viên"));
    }

    private EmployerProfile employerProfile(Long userId) {
        return employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy hồ sơ nhà tuyển dụng"));
    }
}
