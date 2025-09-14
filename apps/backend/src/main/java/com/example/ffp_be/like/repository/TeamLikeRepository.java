package com.example.ffp_be.like.repository;

import com.example.ffp_be.like.entity.TeamLike;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamLikeRepository extends JpaRepository<TeamLike, Long> {

    // 특정 유저의 선호 팀 목록
    List<TeamLike> findAllByUser(User user);

    // 유저가 특정 팀을 선호 중인지 확인
    Optional<TeamLike> findByUserAndTeam(User user, Team team);

    // 선호 팀 삭제
    void deleteByUserAndTeam(User user, Team team);
}
