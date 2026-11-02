package vn.vlu.svjob.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vlu.svjob.dto.CommonDtos.MessageResponse;
import vn.vlu.svjob.dto.CommonDtos.NotificationList;
import vn.vlu.svjob.security.UserPrincipal;
import vn.vlu.svjob.service.NotificationService;

/** Thông báo của người đang đăng nhập (FR-17). */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public NotificationList list(@AuthenticationPrincipal UserPrincipal me) {
        return notificationService.myNotifications(me.id());
    }

    @PatchMapping("/{id}/read")
    public MessageResponse read(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        notificationService.markRead(me.id(), id);
        return new MessageResponse("Đã đánh dấu đã đọc");
    }

    @PatchMapping("/read-all")
    public MessageResponse readAll(@AuthenticationPrincipal UserPrincipal me) {
        notificationService.markAllRead(me.id());
        return new MessageResponse("Đã đánh dấu tất cả là đã đọc");
    }
}
