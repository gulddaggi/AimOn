package com.example.ffp_be.like.repository;

import com.example.ffp_be.like.entity.GameLike;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameLikeRepository extends JpaRepository<GameLike, Long> {

    // 특정 유저의 선호 게임 목록
    List<GameLike> findAllByUser(User user);

    // 유저가 특정 게임을 선호 중인지 확인
    Optional<GameLike> findByUserAndGame(User user, Game game);

    // 선호 게임 삭제
    void deleteByUserAndGame(User user, Game game);
}
