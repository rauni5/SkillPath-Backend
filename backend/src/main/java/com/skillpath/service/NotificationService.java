package com.skillpath.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.skillpath.model.UserDeviceToken.UserDeviceToken;
import com.skillpath.repository.UserDeviceTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public class NotificationService {

    private final ObjectProvider<FirebaseApp> firebaseAppProvider;
    private final UserDeviceTokenRepository deviceTokenRepo;

    public NotificationService(ObjectProvider<FirebaseApp> firebaseAppProvider,
                                UserDeviceTokenRepository deviceTokenRepo) {
        this.firebaseAppProvider = firebaseAppProvider;
        this.deviceTokenRepo = deviceTokenRepo;
    }

    @Async("notificationExecutor")
    public void notifyUser(Long userId, String title, String body, Map<String, String> data) {
        if (userId == null) return;

        FirebaseApp app = firebaseAppProvider.getIfAvailable();
        if (app == null) {
            return;
        }

        List<UserDeviceToken> tokens = deviceTokenRepo.findByUserId(userId);
        if (tokens.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder().setTitle(title).setBody(body).build();
        Map<String, String> payload = data == null ? Map.of() : data;

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
                                    .setChannelId("project_invites")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setSound("default").build())
                            .build())
                    .build();
            try {
                FirebaseMessaging.getInstance(app).send(message);
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                        || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                    // Token is stale - stop trying it.
                    deviceTokenRepo.deleteByToken(deviceToken.getToken());
                }
            } catch (Exception e) {
                    return;
            }
        }
    }
}
