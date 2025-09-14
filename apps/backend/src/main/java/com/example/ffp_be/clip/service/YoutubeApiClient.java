package com.example.ffp_be.clip.service;

import com.example.ffp_be.clip.dto.YoutubeClipDto;

import java.util.List;

public interface YoutubeApiClient {
    List<String> searchVideoIds(String query, int maxResults, boolean shortsOnly);
    List<YoutubeClipDto> fetchVideoDetails(List<String> videoIds);
}
