package com.skillpath.repository;
import com.skillpath.model.ProjectCommentLike.ProjectCommentLike;
import com.skillpath.model.ProjectCommentLike.ProjectCommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCommentLikeRepository extends JpaRepository<ProjectCommentLike, ProjectCommentLikeId> {
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}