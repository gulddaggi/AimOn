package com.example.ffp_be.clip.service;

import com.example.ffp_be.clip.dto.YoutubeClipDto;

import java.util.List;

public interface YouTubeApiService {
    List<YoutubeClipDto> searchClips(String query, int maxResults, boolean shortsOnly);
    YoutubeClipDto getVideoDetail(String videoId);
}
