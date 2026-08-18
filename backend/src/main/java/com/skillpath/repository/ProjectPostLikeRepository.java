package com.skillpath.repository;
import com.skillpath.model.ProjectPostLike.ProjectPostLike;
import com.skillpath.model.ProjectPostLike.ProjectPostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPostLikeRepository extends JpaRepository<ProjectPostLike, ProjectPostLikeId> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}