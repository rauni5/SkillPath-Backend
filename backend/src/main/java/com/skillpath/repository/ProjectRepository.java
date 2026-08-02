package com.skillpath.repository;
import com.skillpath.model.Project.Project;
import com.skillpath.model.enums.ProjectStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface ProjectRepository extends JpaRepository<Project, Long> {
 Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
 Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

 @Query("""
     SELECT DISTINCT p FROM Project p
     LEFT JOIN ProjectRequiredSkill prs ON prs.projectId = p.id
     LEFT JOIN ProjectRequiredRole prr ON prr.projectId = p.id
     WHERE p.status = :status
     AND (:difficulty IS NULL OR p.difficulty = :difficulty)
     AND (:skillIds IS NULL OR prs.skillId IN :skillIds)
     AND (:roleIds IS NULL OR prr.roleId IN :roleIds)
     AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
     """)
 Page<Project> search(@Param("status") ProjectStatus status,
                       @Param("difficulty") String difficulty,
                       @Param("skillIds") List<Long> skillIds,
                       @Param("roleIds") List<Long> roleIds,
                       @Param("q") String q,
                       Pageable pageable);
}