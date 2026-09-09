package com.skillpath.repository;
import com.skillpath.model.RoleBranch.RoleBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface RoleBranchRepository extends JpaRepository<RoleBranch, Long> {
 List<RoleBranch> findByRoleId(Long roleId);
 @Query("SELECT rb.roleId AS roleId, COUNT(rb) AS cnt FROM RoleBranch rb GROUP BY rb.roleId")
 List<UserCareerGoalRepository.CountByRole> countGroupedByRole();
}