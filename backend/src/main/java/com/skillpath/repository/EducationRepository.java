package com.skillpath.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillpath.model.Education.Education;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByUserIdOrderByStartDateDesc(Long userId);
}