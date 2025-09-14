package com.example.ffp_be.player.repository;

import com.example.ffp_be.player.entity.PlayerEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    //선수 검색(팀)
    List<PlayerEntity> findByTeamId(Long teamId);

    //선수 검색(이름)
    List<PlayerEntity> findByNameContaining(String name);

    // 팀 목록으로 선수 조회
    List<PlayerEntity> findByTeamIdIn(List<Long> teamIds);

}
