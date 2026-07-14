package com.skillpath.service;
import com.skillpath.dto.response.UserResponse;
import com.skillpath.model.User.User;
import com.skillpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    @Transactional
    public UserResponse syncUser(String firebaseUid, String email) {
        User user = userRepo.findByFirebaseUid(firebaseUid)
                     .orElseGet(() -> userRepo.save(
                                        User.builder()
                                        .firebaseUid(firebaseUid)
                                        .email(email)
                                        .availability(true)
                    .build()));
        return UserResponse.from(user);
    }
}
