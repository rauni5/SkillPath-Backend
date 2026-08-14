package com.skillpath.repository;
import com.skillpath.model.RoleBranch.RoleBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RoleBranchRepository extends JpaRepository<RoleBranch, Long> {
 List<RoleBranch> findByRoleId(Long roleId);
}