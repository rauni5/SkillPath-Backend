package com.skillpath.repository;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkill;
import com.skillpath.model.BranchRequiredSkill.BranchRequiredSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.Set;
public interface BranchRequiredSkillRepository
 extends JpaRepository<BranchRequiredSkill, BranchRequiredSkillId> {
 List<BranchRequiredSkill> findByBranchId(Long branchId);
 @Query("SELECT b.skillId FROM BranchRequiredSkill b WHERE b.branchId = :id")
 Set<Long> findSkillIdsByBranchId(Long id);
}