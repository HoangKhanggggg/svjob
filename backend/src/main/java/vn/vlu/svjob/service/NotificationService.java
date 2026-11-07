package vn.vlu.svjob.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.CommonDtos.NotificationItem;
import vn.vlu.svjob.dto.CommonDtos.NotificationList;
import vn.vlu.svjob.entity.Notification;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.NotificationRepository;

/** Thông báo in-app (FR-17). Các Service khác gọi notify() khi trạng thái thay đổi. */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notify(User user, String content, String link) {
        notificationRepository.save(new Notification(user, content, link));
    }

    @Transactional(readOnly = true)
    public NotificationList myNotifications(Long userId) {
        var items = notificationRepository.findTop50ByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(n -> new NotificationItem(n.getId(), n.getContent(), n.getLink(), n.isRead(), n.getCreatedAt()))
                .toList();
        return new NotificationList(notificationRepository.countByUserIdAndReadFalse(userId), items);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy thông báo"));
        if (!n.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("Bạn không có quyền với thông báo này");
        }
        n.setRead(true);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByUserIdAndReadFalse(userId).forEach(n -> n.setRead(true));
    }
}
