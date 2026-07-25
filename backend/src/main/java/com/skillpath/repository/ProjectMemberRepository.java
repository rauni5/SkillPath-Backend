package com.skillpath.repository;
import com.skillpath.model.ProjectMember.ProjectMember;
import com.skillpath.model.ProjectMember.ProjectMemberId;
import com.skillpath.model.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProjectMemberRepository
 extends JpaRepository<ProjectMember, ProjectMemberId> {
 List<ProjectMember> findByProjectId(Long projectId);
 List<ProjectMember> findByProjectIdAndStatus(Long projectId, MemberStatus
status);
 List<ProjectMember> findByUserId(Long userId);
 boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
