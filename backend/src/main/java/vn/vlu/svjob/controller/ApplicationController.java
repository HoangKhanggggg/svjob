package vn.vlu.svjob.controller;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vlu.svjob.dto.ApplicationDtos.Applicant;
import vn.vlu.svjob.dto.ApplicationDtos.ApplicationResult;
import vn.vlu.svjob.dto.ApplicationDtos.DecisionRequest;
import vn.vlu.svjob.dto.ApplicationDtos.MyApplication;
import vn.vlu.svjob.security.UserPrincipal;
import vn.vlu.svjob.service.ApplicationService;

import java.util.List;

/** Ứng tuyển và duyệt ứng viên (FR-12 → FR-15). */
@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** multipart/form-data: cv (file, không bắt buộc), coverLetter, useProfileCv. */
    @PostMapping("/jobs/{jobId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResult apply(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long jobId,
                                   @RequestParam(value = "cv", required = false) MultipartFile cv,
                                   @RequestParam(value = "coverLetter", required = false) String coverLetter,
                                   @RequestParam(value = "useProfileCv", defaultValue = "false") boolean useProfileCv) {
        return applicationService.apply(me.id(), jobId, cv, coverLetter, useProfileCv);
    }

    @GetMapping("/students/me/applications")
    public List<MyApplication> myApplications(@AuthenticationPrincipal UserPrincipal me) {
        return applicationService.myApplications(me.id());
    }

    @PatchMapping("/applications/{id}/withdraw")
    public ApplicationResult withdraw(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return applicationService.withdraw(me.id(), id);
    }

    @GetMapping("/jobs/{jobId}/applications")
    public List<Applicant> applicants(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long jobId) {
        return applicationService.applicants(me.id(), jobId);
    }

    @PatchMapping("/applications/{id}/status")
    public ApplicationResult decide(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id,
                                    @Valid @RequestBody DecisionRequest req) {
        return applicationService.decide(me.id(), id, req.status());
    }

    @GetMapping("/applications/{id}/cv")
    public ResponseEntity<Resource> cv(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return FileResponses.pdf(applicationService.cvOfApplication(me, id), "cv-don-" + id + ".pdf");
    }
}
