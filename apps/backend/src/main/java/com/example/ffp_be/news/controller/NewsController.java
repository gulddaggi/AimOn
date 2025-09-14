package com.example.ffp_be.news.controller;

import com.example.ffp_be.auth.entity.UserCredential;
import com.example.ffp_be.auth.security.CustomUserDetails;
import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.news.dto.NewsResponse;
import com.example.ffp_be.news.service.NewsService;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.user.repository.UserRepository;
import com.example.ffp_be.like.service.GameLikeService;
import com.example.ffp_be.like.service.TeamLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "News", description = "뉴스 관리 API")
public class NewsController {

    private final NewsService newsService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamLikeService teamLikeService;
    private final GameLikeService gameLikeService;


    @PostMapping("/fetch")
    @Operation(summary = "뉴스 기사 수집")
    public ResponseEntity<String> fetchNewsForAllGames() {
        newsService.fetchAndSaveNewsForAllGames();
        return ResponseEntity.ok("전체 게임 기반 뉴스 수집 완료");
    }


    @GetMapping("/latest")
    @Operation(summary = "최신 뉴스 조회")
    public ResponseEntity<List<NewsResponse>> getLatestNews() {
        List<NewsResponse> newsList = newsService.getLatestNews();
        return ResponseEntity.ok(newsList);
    }


    @GetMapping("/preferred")
    @Operation(summary = "유저 선호팀 뉴스 조회")
    public ResponseEntity<List<NewsResponse>> getNewsByUserPreference(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");


        Long principalId = principal.getUserId();

        User user = userRepository.findByUser_Id(principalId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Team> likedTeams = teamLikeService.getLikedTeams(user);
        List<Long> teamIds = likedTeams.stream().map(Team::getId).toList();
        List<Long> gameIds = likedTeams.stream().map(t -> t.getGame().getId()).distinct().toList();

        return ResponseEntity.ok(newsService.getNewsByTeamsAndGames(teamIds, gameIds));
    }
}
