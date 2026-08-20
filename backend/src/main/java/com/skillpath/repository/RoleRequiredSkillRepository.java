package com.skillpath.repository;
import com.skillpath.model.RoleRequiredSkill.RoleRequiredSkill;
import com.skillpath.model.RoleRequiredSkill.RoleRequiredSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.Set;
public interface RoleRequiredSkillRepository
 extends JpaRepository<RoleRequiredSkill, RoleRequiredSkillId> {
 List<RoleRequiredSkill> findByRoleId(Long roleId);
 @Query("SELECT r.skillId FROM RoleRequiredSkill r WHERE r.roleId = :roleId")
 Set<Long> findSkillIdsByRoleId(Long roleId);
 @Query("SELECT r.roleId AS roleId, COUNT(r) AS cnt FROM RoleRequiredSkill r GROUP BY r.roleId")
 List<CountByRole> countGroupedByRole();
 interface CountByRole {
     Long getRoleId();
     long getCnt();
 }
}