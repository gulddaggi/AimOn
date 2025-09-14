package com.example.ffp_be.clip.service;

import com.example.ffp_be.clip.dto.ClipResponse;
import com.example.ffp_be.clip.dto.CreateClipRequest;
import com.example.ffp_be.clip.dto.YoutubeClipDto;
import com.example.ffp_be.clip.entity.Clip;
import com.example.ffp_be.clip.repository.ClipRepository;
import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClipServiceImpl implements ClipService {

    private final ClipRepository clipRepository;
    private final TeamRepository teamRepository;
    private final YouTubeApiService youtubeApiService;
    private final ClipKeywordService keywordService;

    @PersistenceContext
    private EntityManager em;

    private static final int MAX_PER_TEAM = 10;
    private static final boolean SHORTS_ONLY = true;


    @Override
    @Transactional(readOnly = true)
    public List<ClipResponse> getPreferredTeamClips(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId는 필수입니다.");
        }
        var page = PageRequest.of(0, 6);
        return clipRepository.findPreferredByUserId(userId, page)
            .stream()
            .map(ClipResponse::fromEntity)
            .toList();
    }

    @Override
    @Transactional
    public ClipResponse registerUserClip(CreateClipRequest request) {
        if (request.getGameId() == null || request.getTeamId() == null ||
            request.getUserId() == null ||
            request.getYoutubeUrl() == null || request.getTitle() == null || request.getTitle()
            .isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "gameId, teamId, userId, youtubeUrl, title는 모두 필수입니다.");
        }

        String videoId = extractYoutubeVideoId(request.getYoutubeUrl());
        if (videoId == null || videoId.isBlank()) {
            throw new com.example.ffp_be.clip.exception.InvalidYoutubeUrlException();
        }

        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
        String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        String channelTitle = null;
        String description = null;
        LocalDateTime publishedAt = LocalDateTime.now();
        Long likeCount = null;
        Long viewCount = null;

        try {
            YoutubeClipDto d = youtubeApiService.getVideoDetail(videoId);
            if (d != null) {
                if (d.getChannelTitle() != null) {
                    channelTitle = d.getChannelTitle();
                }
                if (d.getThumbnailUrl() != null) {
                    thumbnailUrl = d.getThumbnailUrl();
                }
                if (d.getDescription() != null) {
                    description = d.getDescription();
                }
                if (d.getPublishedAt() != null) {
                    publishedAt = d.getPublishedAt();
                }
                if (d.getLikeCount() != null) {
                    likeCount = d.getLikeCount();
                }
                if (d.getViewCount() != null) {
                    viewCount = d.getViewCount();
                }
            }
        } catch (Exception e) {
            log.warn("YouTube 상세 조회 실패(무시하고 저장 진행): {}", e.getMessage());
        }

        Clip toSave = Clip.builder()
            .videoId(videoId)
            .title(request.getTitle())
            .description(description)
            .videoUrl(videoUrl)
            .thumbnailUrl(thumbnailUrl)
            .channelTitle(channelTitle)
            .publishedAt(publishedAt)
            .gameId(request.getGameId())
            .teamId(request.getTeamId())
            .userId(request.getUserId())
            .likeCount(likeCount)
            .viewCount(viewCount)
            .build();

        return ClipResponse.fromEntity(clipRepository.save(toSave));
    }


    private String extractYoutubeVideoId(String url) {
        try {
            URI uri = URI.create(url.trim());
            if (uri.getQuery() != null) {
                for (String q : uri.getQuery().split("&")) {
                    String[] kv = q.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("v")) {
                        return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    }
                }
            }
            String path = uri.getPath();
            if (path != null) {
                Pattern p = Pattern.compile("/(?:embed|shorts)?/?([A-Za-z0-9_-]{8,})");
                Matcher m = p.matcher(path);
                if (m.find()) {
                    return m.group(1);
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ClipResponse> getClipsByUserId(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId는 필수입니다.");
        }
        return clipRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(ClipResponse::fromEntity).toList();
    }


    @Override
    @Transactional
    public void ingestAllClips() {
        List<Team> teams = teamRepository.findAll();
        ingestForTeams(teams);
    }

    @Override
    @Transactional
    public void fetchAndSaveClips(Long gameId) {
        Game gameRef = em.getReference(Game.class, gameId);
        List<Team> teams = teamRepository.findAllByGame(gameRef);
        ingestForTeams(teams);
    }

    private void ingestForTeams(List<Team> teams) {
        if (teams == null || teams.isEmpty()) {
            log.warn("팀 데이터가 없습니다. 수집 중단.");
            return;
        }
        Set<String> existing = clipRepository.findAll().stream()
            .map(Clip::getVideoId).collect(Collectors.toSet());

        List<Clip> toSave = new ArrayList<>();
        int scanned = 0;

        for (Team team : teams) {
            if (team == null || team.getGame() == null || team.getGame().getId() == null) {
                continue;
            }

            String teamName = team.getTeamName() == null ? "" : team.getTeamName().trim();
            if (teamName.isBlank()) {
                continue;
            }

            Long gid = team.getGame().getId();
            String gameDisplay = team.getGame().getName().name();

            Set<String> picked = new LinkedHashSet<>();
            var queries = keywordService.buildQueries(teamName, gid, gameDisplay);

            for (String q : queries) {
                List<YoutubeClipDto> found = youtubeApiService.searchClips(q, MAX_PER_TEAM,
                    SHORTS_ONLY);
                for (YoutubeClipDto dto : found) {
                    String vid = dto.getVideoId();
                    if (vid == null || existing.contains(vid) || !picked.add(vid)) {
                        continue;
                    }

                    if (!containsIgnoreCase(dto.getTitle(), teamName)
                        && !containsIgnoreCase(dto.getDescription(), teamName)
                        && !containsIgnoreCase(dto.getChannelTitle(), teamName)) {
                        continue;
                    }

                    toSave.add(Clip.builder()
                        .videoId(dto.getVideoId())
                        .title(dto.getTitle())
                        .description(dto.getDescription())
                        .videoUrl(dto.getVideoUrl())
                        .thumbnailUrl(dto.getThumbnailUrl())
                        .channelTitle(dto.getChannelTitle())
                        .publishedAt(dto.getPublishedAt())
                        .gameId(gid)
                        .teamId(team.getId())
                        .likeCount(dto.getLikeCount())
                        .viewCount(dto.getViewCount())
                        .userId(null) // ✅ 자동수집엔 소유자 없음
                        .build());
                    existing.add(vid);
                }
                if (picked.size() >= MAX_PER_TEAM) {
                    break;
                }
            }
            scanned++;
        }
        if (!toSave.isEmpty()) {
            toSave.sort(Comparator.comparing(Clip::getPublishedAt).reversed());
            clipRepository.saveAll(toSave);
            log.info("수집 완료: {}건 저장 (스캔 팀: {}).", toSave.size(), scanned);
        } else {
            log.info("신규 수집 결과 없음 (스캔 팀: {}).", scanned);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClipResponse getClipById(Long clipId) {
        Clip clip = clipRepository.findById(clipId)
            .orElseThrow(com.example.ffp_be.clip.exception.ClipNotFoundException::new);
        return ClipResponse.fromEntity(clip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClipResponse> getClipsByGameId(Long gameId) {
        return clipRepository.findAllByGameIdOrderByPublishedAtDesc(gameId)
            .stream().map(ClipResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClipResponse> getLatestClipsByGameId(Long gameId) {
        return clipRepository.findTop6ByGameIdOrderByPublishedAtDesc(gameId)
            .stream().map(ClipResponse::fromEntity).toList();
    }

    @Override
    @Transactional
    public void deleteClip(Long clipId) {
        if (!clipRepository.existsById(clipId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "삭제할 클립이 없습니다. (id=" + clipId + ")");
        }
        clipRepository.deleteById(clipId);
    }


    private boolean containsIgnoreCase(String text, String needle) {
        return text != null && needle != null && text.toLowerCase().contains(needle.toLowerCase());
    }
}
