package vn.vlu.svjob.controller;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vlu.svjob.dto.ProfileDtos.EmployerProfileRequest;
import vn.vlu.svjob.dto.ProfileDtos.EmployerProfileResponse;
import vn.vlu.svjob.dto.ProfileDtos.StudentProfileRequest;
import vn.vlu.svjob.dto.ProfileDtos.StudentProfileResponse;
import vn.vlu.svjob.security.UserPrincipal;
import vn.vlu.svjob.service.ProfileService;

/** Hồ sơ SV (FR-04), hồ sơ NTD (FR-05). */
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/students/me/profile")
    public StudentProfileResponse getStudent(@AuthenticationPrincipal UserPrincipal me) {
        return profileService.getStudent(me.id());
    }

    @PutMapping("/students/me/profile")
    public StudentProfileResponse updateStudent(@AuthenticationPrincipal UserPrincipal me,
                                                @Valid @RequestBody StudentProfileRequest req) {
        return profileService.updateStudent(me.id(), req);
    }

    @PostMapping("/students/me/cv")
    public StudentProfileResponse uploadCv(@AuthenticationPrincipal UserPrincipal me,
                                           @RequestParam("file") MultipartFile file) {
        return profileService.uploadCv(me.id(), file);
    }

    @GetMapping("/students/me/cv")
    public ResponseEntity<Resource> myCv(@AuthenticationPrincipal UserPrincipal me) {
        return FileResponses.pdf(profileService.myCv(me.id()), "cv.pdf");
    }

    @GetMapping("/employers/me/profile")
    public EmployerProfileResponse getEmployer(@AuthenticationPrincipal UserPrincipal me) {
        return profileService.getEmployer(me.id());
    }

    @PutMapping("/employers/me/profile")
    public EmployerProfileResponse updateEmployer(@AuthenticationPrincipal UserPrincipal me,
                                                  @Valid @RequestBody EmployerProfileRequest req) {
        return profileService.updateEmployer(me.id(), req);
    }
}
