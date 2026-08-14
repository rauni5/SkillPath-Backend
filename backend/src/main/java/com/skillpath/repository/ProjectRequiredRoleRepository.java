package com.skillpath.repository;
import com.skillpath.model.ProjectRequiredRole.ProjectRequiredRole;
import com.skillpath.model.ProjectRequiredRole.ProjectRequiredRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.Set;
public interface ProjectRequiredRoleRepository
 extends JpaRepository<ProjectRequiredRole, ProjectRequiredRoleId> {
    List<ProjectRequiredRole> findByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
    @Query("SELECT p.roleId FROM ProjectRequiredRole p WHERE p.projectId = :id")
    Set<Long> findRoleIdsByProjectId(Long id);
}