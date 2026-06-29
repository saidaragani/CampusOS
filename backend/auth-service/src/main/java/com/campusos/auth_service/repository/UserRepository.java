package com.campusos.auth_service.repository;

import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findBySchoolIdAndRole_Name(UUID schoolId, RoleType roleName);
}
