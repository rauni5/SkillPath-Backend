package com.skillpath.repository;
import com.skillpath.model.CareerRole.CareerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CareerRoleRepository extends JpaRepository<CareerRole, Long> {
    Optional<CareerRole> findByName(String name);
}
