package com.campusos.auth_service.repository;

import com.campusos.auth_service.entity.RefreshToken;
import com.campusos.auth_service.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);
    @Transactional
    @Modifying
    int deleteByUser(User user);
}
