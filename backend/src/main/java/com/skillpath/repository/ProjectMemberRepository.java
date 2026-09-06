package com.skillpath.repository;
import com.skillpath.model.ProjectMember.ProjectMember;
import com.skillpath.model.ProjectMember.ProjectMemberId;
import com.skillpath.model.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ProjectMemberRepository
 extends JpaRepository<ProjectMember, ProjectMemberId> {
 List<ProjectMember> findByProjectId(Long projectId);
 List<ProjectMember> findByProjectIdAndStatus(Long projectId, MemberStatus
status);
 List<ProjectMember> findByUserId(Long userId);
 List<ProjectMember> findByUserIdAndStatus(Long userId, MemberStatus status);
 boolean existsByProjectIdAndUserId(Long projectId, Long userId);

 /** Pending, self-requested (not owner-invited) memberships across every
  * project this owner owns — what backs "join requests waiting on me". */
 @Query("select m from ProjectMember m where m.status = :status and m.invitedByOwner = false " +
        "and m.projectId in (select p.id from Project p where p.ownerId = :ownerId)")
 List<ProjectMember> findPendingJoinRequestsForOwner(@Param("ownerId") Long ownerId, @Param("status") MemberStatus status);
}