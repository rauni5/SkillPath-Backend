package com.skillpath.service;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.skillpath.model.AppNotification.AppNotification;
import com.skillpath.model.UserDeviceToken.UserDeviceToken;
import com.skillpath.repository.AppNotificationRepository;
import com.skillpath.repository.UserDeviceTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
@Service
@Slf4j
public class NotificationService {
    private static final String ANDROID_CHANNEL_ID = "notification_sound";
    private static final String IOS_SOUND_FILE = "notif_sound.caf";

    private final ObjectProvider<FirebaseApp> firebaseAppProvider;
    private final UserDeviceTokenRepository deviceTokenRepo;
    private final AppNotificationRepository notificationRepo;

    public NotificationService(ObjectProvider<FirebaseApp> firebaseAppProvider,
                                UserDeviceTokenRepository deviceTokenRepo,
                                AppNotificationRepository notificationRepo) {
        this.firebaseAppProvider = firebaseAppProvider;
        this.deviceTokenRepo = deviceTokenRepo;
        this.notificationRepo = notificationRepo;
    }

    @Async("notificationExecutor")
    @Transactional
    public void notifyUser(Long userId, String title, String body, Map<String, String> data) {
        if (userId == null) return;

        Map<String, String> payload = data == null ? Map.of() : data;

        // Persist first, independent of push delivery — this is what backs
        // the in-app notification center, so it needs to exist whether or
        // not Firebase is configured, the user has a device token, or the
        // push itself fails for some other reason.
        try {
            notificationRepo.save(AppNotification.builder()
                    .userId(userId)
                    .type(payload.getOrDefault("type", "UNKNOWN"))
                    .title(title)
                    .body(body)
                    .projectId(parseLongOrNull(payload.get("projectId")))
                    .postId(parseLongOrNull(payload.get("postId")))
                    .build());
        } catch (Exception e) {
            log.warn("Could not persist notification history for user {}", userId, e);
        }

        FirebaseApp app = firebaseAppProvider.getIfAvailable();
        if (app == null) {
            log.warn("Push notification skipped for user {} - Firebase is not configured", userId);
            return;
        }

        List<UserDeviceToken> tokens = deviceTokenRepo.findByUserId(userId);
        if (tokens.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder().setTitle(title).setBody(body).build();

        // Sent one at a time (rather than a single multicast call) so the exact
        // SDK version's multicast API doesn't matter, and one bad/stale token
        // can't affect delivery to the user's other devices.
        for (UserDeviceToken deviceToken : tokens) {
            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    .setNotification(notification)
                    .putAllData(payload)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId(ANDROID_CHANNEL_ID)
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setSound(IOS_SOUND_FILE).build())
                            .build())
                    .build();
            try {
                FirebaseMessaging.getInstance(app).send(message);
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                        || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                    // Token is stale (app uninstalled, etc) - stop trying it.
                    deviceTokenRepo.deleteByToken(deviceToken.getToken());
                } else {
                    log.warn("Push notification failed for user {}: {}", userId, e.getMessage());
                }
            } catch (Exception e) {
                //never let a notification failure surface past this method.
                log.warn("Unexpected error sending push notification to user {}", userId, e);
            }
        }
    }

    private Long parseLongOrNull(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Paginated, newest first. {@code size} is clamped to
     * [1, MAX_PAGE_SIZE] so a caller can't accidentally (or deliberately)
     * request a user's entire notification history in one call.
     */
    public java.util.List<com.skillpath.dto.response.AppNotificationResponse> getNotifications(
            Long userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = org.springframework.data.domain.PageRequest.of(
                safePage, safeSize,
                org.springframework.data.domain.Sort.by("createdAt").descending());
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .stream().map(com.skillpath.dto.response.AppNotificationResponse::from).toList();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        AppNotification n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new com.skillpath.exception.ResourceNotFoundException(
                        "Notification not found: " + notificationId));
        if (!n.getUserId().equals(userId))
            throw new SecurityException("User " + userId + " does not own notification " + notificationId);
        if (!n.isRead()) {
            n.setRead(true);
            notificationRepo.save(n);
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepo.markAllRead(userId);
    }
}