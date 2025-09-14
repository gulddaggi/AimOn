package com.example.ffp_be.auth.repository;

import com.example.ffp_be.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    void deleteByRefreshToken(String refreshToken);
}
