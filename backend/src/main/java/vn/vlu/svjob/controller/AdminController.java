package vn.vlu.svjob.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vlu.svjob.dto.AuthDtos.UserResponse;
import vn.vlu.svjob.dto.CommonDtos.CategoryRequest;
import vn.vlu.svjob.dto.CommonDtos.CategoryResponse;
import vn.vlu.svjob.dto.CommonDtos.PageResponse;
import vn.vlu.svjob.dto.CommonDtos.UserStatusRequest;
import vn.vlu.svjob.dto.JobDtos.HideRequest;
import vn.vlu.svjob.dto.JobDtos.JobSummary;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.UserStatus;
import vn.vlu.svjob.security.UserPrincipal;
import vn.vlu.svjob.service.AdminUserService;
import vn.vlu.svjob.service.CategoryService;
import vn.vlu.svjob.service.JobService;

import java.util.List;

/** Chức năng quản trị: người dùng (FR-20), kiểm duyệt tin (FR-08), danh mục (FR-21). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUserService adminUserService;
    private final JobService jobService;
    private final CategoryService categoryService;

    public AdminController(AdminUserService adminUserService, JobService jobService, CategoryService categoryService) {
        this.adminUserService = adminUserService;
        this.jobService = jobService;
        this.categoryService = categoryService;
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> users(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Role role,
                                            @RequestParam(required = false) UserStatus status,
                                            @RequestParam(defaultValue = "0") int page) {
        return adminUserService.search(keyword, role, status, page);
    }

    @PatchMapping("/users/{id}/status")
    public UserResponse setUserStatus(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id,
                                      @Valid @RequestBody UserStatusRequest req) {
        return adminUserService.setStatus(me.id(), id, req.status());
    }

    @GetMapping("/jobs")
    public PageResponse<JobSummary> jobs(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) JobStatus status,
                                         @RequestParam(defaultValue = "0") int page) {
        return jobService.adminList(keyword, status, page);
    }

    @PatchMapping("/jobs/{id}/hide")
    public JobSummary hide(@PathVariable Long id, @Valid @RequestBody HideRequest req) {
        return jobService.hide(id, req.hidden(), req.reason());
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return categoryService.allCategories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest req) {
        return categoryService.create(req);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest req) {
        return categoryService.update(id, req);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideCategory(@PathVariable Integer id) {
        categoryService.hide(id);
    }
}
