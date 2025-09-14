package com.example.ffp_be.user.repository;

import com.example.ffp_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNickname(String nickname);

    Optional<User> findByUser_Id(Long userId);

    boolean existsByNickname(String nickname);

    List<User> findAllByNicknameContaining(String keyword);
}
