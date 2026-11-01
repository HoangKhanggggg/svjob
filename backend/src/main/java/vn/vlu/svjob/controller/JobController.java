package vn.vlu.svjob.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vlu.svjob.dto.CommonDtos.CategoryResponse;
import vn.vlu.svjob.dto.CommonDtos.PageResponse;
import vn.vlu.svjob.dto.JobDtos.JobDetail;
import vn.vlu.svjob.dto.JobDtos.JobRequest;
import vn.vlu.svjob.dto.JobDtos.JobSummary;
import vn.vlu.svjob.security.UserPrincipal;
import vn.vlu.svjob.service.CategoryService;
import vn.vlu.svjob.service.JobService;

import java.util.List;

/** Tin tuyển dụng và danh mục ngành nghề (FR-06 → FR-11). */
@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;
    private final CategoryService categoryService;

    public JobController(JobService jobService, CategoryService categoryService) {
        this.jobService = jobService;
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return categoryService.activeCategories();
    }

    @GetMapping("/jobs")
    public PageResponse<JobSummary> search(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer categoryId,
                                           @RequestParam(required = false) String location,
                                           @RequestParam(required = false) Integer minSalary,
                                           @RequestParam(required = false) Integer maxSalary,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return jobService.search(keyword, categoryId, location, minSalary, maxSalary, page, size);
    }

    @GetMapping("/jobs/{id}")
    public JobDetail detail(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal me) {
        return jobService.detail(id, me);
    }

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobDetail create(@AuthenticationPrincipal UserPrincipal me, @Valid @RequestBody JobRequest req) {
        return jobService.create(me.id(), req);
    }

    @GetMapping("/employers/me/jobs")
    public List<JobSummary> myJobs(@AuthenticationPrincipal UserPrincipal me) {
        return jobService.myJobs(me.id());
    }
}
