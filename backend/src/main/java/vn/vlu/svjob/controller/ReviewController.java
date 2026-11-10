package vn.vlu.svjob.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vlu.svjob.dto.ReviewDtos.ReviewItem;
import vn.vlu.svjob.dto.ReviewDtos.ReviewRequest;
import vn.vlu.svjob.dto.ReviewDtos.UserReviews;
import vn.vlu.svjob.security.UserPrincipal;
import vn.vlu.svjob.service.ReviewService;

/** Đánh giá hai chiều (FR-18, FR-19). */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/jobs/{jobId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewItem create(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long jobId,
                             @Valid @RequestBody ReviewRequest req) {
        return reviewService.create(me.id(), jobId, req);
    }

    @GetMapping("/users/{id}/reviews")
    public UserReviews ofUser(@PathVariable Long id) {
        return reviewService.ofUser(id);
    }
}
