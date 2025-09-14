package com.example.ffp_be.clip.service;

import com.example.ffp_be.clip.dto.YoutubeClipDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeApiClientImpl implements YoutubeApiClient {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<String> searchVideoIds(String query, int maxResults, boolean shortsOnly) {
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("q", shortsOnly ? (query + " shorts") : query)
                .queryParam("maxResults", Math.max(1, Math.min(50, maxResults)))
                .queryParam("order", "date")
                .queryParam("videoEmbeddable", "true")
                .queryParam("videoDuration", shortsOnly ? "short" : "any")
                .build().toUriString();

            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> body = resp.getBody();
            if (body == null || body.get("items") == null) return Collections.emptyList();

            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            List<String> ids = new ArrayList<>();
            for (Map<String, Object> it : items) {
                Map<String, Object> id = (Map<String, Object>) it.get("id");
                if (id != null && id.get("videoId") != null) {
                    ids.add(String.valueOf(id.get("videoId")));
                }
            }
            return ids;
        } catch (HttpClientErrorException.Forbidden ex) {
            throw ex;
        }
    }

    @Override
    public List<YoutubeClipDto> fetchVideoDetails(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) return Collections.emptyList();
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet,statistics")
                .queryParam("id", String.join(",", videoIds))
                .build().toUriString();

            Map<?, ?> res = restTemplate.getForObject(url, Map.class);
            if (res == null || res.get("items") == null) return Collections.emptyList();

            List<Map<String, Object>> items = (List<Map<String, Object>>) res.get("items");
            List<YoutubeClipDto> list = new ArrayList<>();

            for (Map<String, Object> item : items) {
                String id = String.valueOf(item.get("id"));
                Map<String, Object> sn = (Map<String, Object>) item.get("snippet");
                Map<String, Object> st = (Map<String, Object>) item.get("statistics");

                String title = sn != null ? String.valueOf(sn.get("title")) : "";
                String desc  = sn != null ? String.valueOf(sn.get("description")) : "";
                String ch    = sn != null ? String.valueOf(sn.get("channelTitle")) : "";
                String thumb = null;
                if (sn != null && sn.get("thumbnails") instanceof Map<?, ?> thumbs) {
                    Object high = ((Map<?, ?>) thumbs).get("high");
                    Object def  = ((Map<?, ?>) thumbs).get("default");
                    if (high instanceof Map<?, ?> h && h.get("url") != null) thumb = String.valueOf(h.get("url"));
                    else if (def instanceof Map<?, ?> d && d.get("url") != null) thumb = String.valueOf(d.get("url"));
                }

                OffsetDateTime published = OffsetDateTime.now(ZoneOffset.UTC);
                if (sn != null && sn.get("publishedAt") != null) {
                    published = OffsetDateTime.parse(String.valueOf(sn.get("publishedAt")), DateTimeFormatter.ISO_DATE_TIME);
                }

                Long likes = null, views = null;
                if (st != null) {
                    Object lc = st.get("likeCount");
                    Object vc = st.get("viewCount");
                    try { if (lc != null) likes = Long.parseLong(String.valueOf(lc)); } catch (Exception ignore) {}
                    try { if (vc != null) views = Long.parseLong(String.valueOf(vc)); } catch (Exception ignore) {}
                }

                list.add(YoutubeClipDto.builder()
                    .videoId(id)
                    .title(title)
                    .description(desc)
                    .videoUrl("https://www.youtube.com/watch?v=" + id)
                    .thumbnailUrl(thumb)
                    .channelTitle(ch)
                    .publishedAt(published.toLocalDateTime())
                    .likeCount(likes)     // ✅
                    .viewCount(views)     // ✅
                    .build());
            }
            return list;
        } catch (HttpClientErrorException.Forbidden ex) {
            throw ex;
        }
    }
}
