package com.campusos.auth_service.repository;

import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    
    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);
}
