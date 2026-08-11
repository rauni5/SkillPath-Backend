package com.skillpath.repository;

import com.skillpath.model.Certification.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByUserIdOrderByEarnedOnDesc(Long userId);
}