package com.skillpath.repository;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkill;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkillId;
import com.skillpath.repository.UserCareerGoalRepository.CountByRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.Set;
public interface BranchRequiredSkillRepository
 extends JpaRepository<BranchRequiredSkill, BranchRequiredSkillId> {
 List<BranchRequiredSkill> findByBranchId(Long branchId);
 @Query("SELECT b.skillId FROM BranchRequiredSkill b WHERE b.branchId = :id")
 Set<Long> findSkillIdsByBranchId(Long id);

@Query("""
        SELECT rb.roleId AS roleId, COUNT(DISTINCT brs.skillId) AS cnt
        FROM RoleBranch rb
        JOIN BranchRequiredSkill brs ON brs.branchId = rb.id
        GROUP BY rb.roleId
    """)
    List<CountByRole> countDistinctSkillsGroupedByRole();

    interface CountByRole {
        Long getRoleId();
        long getCnt();
    }

}