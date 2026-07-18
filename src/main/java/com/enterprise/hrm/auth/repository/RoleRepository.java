package com.enterprise.hrm.auth.repository;

import com.enterprise.hrm.auth.entity.ERole;
import com.enterprise.hrm.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role Repository — finds roles by their enum name.
 * Used during user registration to assign roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its enum name.
     * Generated JPQL: SELECT r FROM Role r WHERE r.name = :name
     */
    Optional<Role> findByName(ERole name);
}
