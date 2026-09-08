package com.skillpath.repository;
import com.skillpath.model.ProjectComment.ProjectComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {
    Page<ProjectComment> findByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);
    java.util.List<ProjectComment> findByAuthorId(Long authorId);
}