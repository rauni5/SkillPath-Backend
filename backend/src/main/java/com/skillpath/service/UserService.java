package com.skillpath.service;
import com.skillpath.dto.request.RegisterDeviceTokenRequest;
import com.skillpath.dto.request.UpdateProfileRequest;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.User.User;
import com.skillpath.model.UserDeviceToken.UserDeviceToken;
import com.skillpath.repository.UserDeviceTokenRepository;
import com.skillpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final UserDeviceTokenRepository deviceTokenRepo;
    public UserResponse findById(Long userId) {
        return userRepo.findById(userId)
                    .map(UserResponse::from)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
    // Returns the raw entity — used by other services internally
    public User getEntityById(Long userId) {
        return userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
    public User getEntityByFirebaseUid(String uid) {
        return userRepo.findByFirebaseUid(uid)
        .orElseThrow(() -> new ResourceNotFoundException("User not found for UID: " + uid));
    }
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = getEntityById(userId);
        if (req.getName() != null) user.setName(req.getName());
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getExperienceLevel() != null) user.setExperienceLevel(req.getExperienceLevel());
        if (req.getAvailability() != null) user.setAvailability(req.getAvailability());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        return UserResponse.from(userRepo.save(user));
    }

    /**
     * Registers (or re-associates) a device's FCM token with this user, so
     * push notifications can be delivered to it. Tokens are unique across
     * users - if the same token was previously registered to someone else
     * (e.g. shared device, account switch, app reinstall) it is moved over.
     */
    @Transactional
    public void registerDeviceToken(Long userId, RegisterDeviceTokenRequest req) {
        getEntityById(userId); // 404s if the user doesn't exist
        String platform = (req.getPlatform() == null || req.getPlatform().isBlank())
                ? "UNKNOWN" : req.getPlatform().trim().toUpperCase();

        UserDeviceToken token = deviceTokenRepo.findByToken(req.getToken())
                .orElseGet(() -> UserDeviceToken.builder().token(req.getToken()).build());
        token.setUserId(userId);
        token.setPlatform(platform);
        deviceTokenRepo.save(token);
    }

    /** Called on logout so a shared/reinstalled device stops receiving this user's notifications. */
    @Transactional
    public void unregisterDeviceToken(String token) {
        deviceTokenRepo.deleteByToken(token);
    }
}