package com.example.ffp_be.clip.repository;

import com.example.ffp_be.clip.entity.Clip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClipRepository extends JpaRepository<Clip, Long> {

    List<Clip> findAllByGameIdOrderByPublishedAtDesc(Long gameId);
    List<Clip> findTop6ByGameIdOrderByPublishedAtDesc(Long gameId);
    List<Clip> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // ✅ 유저가 좋아요한(선호한) 팀의 최신 클립 (최대 6개는 Pageable로 제한)
    @Query(
        value = """
                SELECT c.* 
                FROM clip c 
                JOIN team_like tl ON tl.team_id = c.team_id 
                WHERE tl.user_id = :userId 
                ORDER BY c.published_at DESC, c.created_at DESC
                """,
        countQuery = """
                SELECT COUNT(*) 
                FROM clip c 
                JOIN team_like tl ON tl.team_id = c.team_id 
                WHERE tl.user_id = :userId
                """,
        nativeQuery = true
    )
    List<Clip> findPreferredByUserId(@Param("userId") Long userId, Pageable pageable);
}
