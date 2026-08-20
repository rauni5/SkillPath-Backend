package com.skillpath.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillpath.model.User.User;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
 Optional<User> findByFirebaseUid(String firebaseUid);
 boolean existsByFirebaseUid(String firebaseUid);
 @Query("""
     SELECT u FROM User u
     WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
     ORDER BY u.name ASC
     """)
 Page<User> searchByNameOrEmail(@Param("q") String q, Pageable pageable);
}
