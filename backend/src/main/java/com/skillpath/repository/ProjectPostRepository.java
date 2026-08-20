package com.skillpath.repository;
import com.skillpath.model.ProjectPost.ProjectPost;
import com.skillpath.model.enums.DiscussionChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPostRepository extends JpaRepository<ProjectPost, Long> {
    Page<ProjectPost> findByProjectIdAndChannelOrderByCreatedAtDesc(
            Long projectId, DiscussionChannel channel, Pageable pageable);
}