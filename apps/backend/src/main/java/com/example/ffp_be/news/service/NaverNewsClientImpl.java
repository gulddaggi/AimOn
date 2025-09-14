package com.example.ffp_be.news.service;

import com.example.ffp_be.news.entity.News;
import com.example.ffp_be.news.repository.NewsRepository;
import com.example.ffp_be.news.exception.NaverCredentialsMissingException;
import com.example.ffp_be.news.exception.NaverApiException;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverNewsClientImpl implements NaverNewsClient {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private static final String NAVER_API_URL =
        "https://openapi.naver.com/v1/search/news.json?query=%s&display=10&sort=date";

    private final RestTemplate restTemplate = createRestTemplate();

    private final NewsRepository newsRepository;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;

    private static final DateTimeFormatter NAVER_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    /**
     * 네이버 뉴스 검색 및 DB 저장
     */
    @Override
    public List<News> searchAndSaveNews(String keyword, Long teamId, Long gameId) {
        if (clientId == null || clientId.isBlank() || clientSecret == null
            || clientSecret.isBlank()) {
            throw new NaverCredentialsMissingException();
        }
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = String.format(NAVER_API_URL, encoded);

        ResponseEntity<Map> response;
        try {
            response = sendNaverApiRequest(url);
        } catch (RestClientException ex) {
            log.warn("네이버 뉴스 API 호출 예외: {}", ex.getMessage());
            throw new NaverApiException(ex.getMessage());
        }
        List<News> savedNewsList = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Object itemsObj = response.getBody().get("items");
            if (!(itemsObj instanceof List<?> rawList)) {
                log.warn("네이버 뉴스 응답에 items가 없거나 형식이 다릅니다: {}", itemsObj);
                return savedNewsList;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) rawList;

            Team team = teamRepository.getReferenceById(teamId);
            Game game = gameRepository.getReferenceById(gameId);

            for (Map<String, Object> item : items) {
                String link = String.valueOf(item.get("link"));

                // 중복 뉴스 저장 방지
                if (newsRepository.findByLink(link).isPresent()) {
                    continue;
                }

                News news = News.builder()
                    .title(stripHtml(Objects.toString(item.get("title"), "")))
                    .content(stripHtml(Objects.toString(item.get("description"), "")))
                    .link(link)
                    .publishedAt(parseDate(Objects.toString(item.get("pubDate"), null)))
                    .team(team)
                    .game(game)
                    .build();

                newsRepository.save(news);
                savedNewsList.add(news);
            }
        }

        return savedNewsList;
    }

    /**
     * 네이버 뉴스 API 요청 처리
     */
    private ResponseEntity<Map> sendNaverApiRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
    }

    /**
     * HTML 태그 제거
     */
    private String stripHtml(String input) {
        return Jsoup.parse(input).text();
    }

    /**
     * pubDate 문자열을 LocalDateTime으로 파싱
     */
    private LocalDateTime parseDate(String pubDateStr) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(pubDateStr, NAVER_DATE_FORMATTER);
            return zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", pubDateStr, e);
            return LocalDateTime.now(); // 실패 시 현재 시간으로 대체
        }
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
