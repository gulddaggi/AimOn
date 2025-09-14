package com.example.ffp_be.auth.repository;

import com.example.ffp_be.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findById(Long userId);
}
