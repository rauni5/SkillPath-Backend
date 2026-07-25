package com.skillpath.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillpath.model.User.User;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
 Optional<User> findByFirebaseUid(String firebaseUid);
 boolean existsByFirebaseUid(String firebaseUid);
}
