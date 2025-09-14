package com.example.ffp_be.clip.service;

import com.example.ffp_be.clip.dto.YoutubeClipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeApiServiceImpl implements YouTubeApiService {

    private final YoutubeApiClient youtubeApiClient;

    @Override
    public List<YoutubeClipDto> searchClips(String query, int maxResults, boolean shortsOnly) {
        List<String> ids = youtubeApiClient.searchVideoIds(query, maxResults, shortsOnly);
        return youtubeApiClient.fetchVideoDetails(ids);
    }

    @Override
    public YoutubeClipDto getVideoDetail(String videoId) {
        List<YoutubeClipDto> list = youtubeApiClient.fetchVideoDetails(List.of(videoId));
        return list.isEmpty() ? null : list.get(0);
    }
}
