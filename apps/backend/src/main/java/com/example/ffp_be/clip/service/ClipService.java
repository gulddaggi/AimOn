package com.example.ffp_be.clip.service;

import com.example.ffp_be.clip.dto.ClipResponse;
import com.example.ffp_be.clip.dto.CreateClipRequest;

import java.util.List;

public interface ClipService {

    default void ingestAllClips() { }
    default void fetchAndSaveClips(Long gameId) { }

    ClipResponse registerUserClip(CreateClipRequest request);

    List<ClipResponse> getClipsByUserId(Long userId);

    void deleteClip(Long clipId);
    ClipResponse getClipById(Long clipId);
    List<ClipResponse> getClipsByGameId(Long gameId);
    List<ClipResponse> getLatestClipsByGameId(Long gameId);

    // ✅ 유저 선호팀 클립 6개
    List<ClipResponse> getPreferredTeamClips(Long userId);
}
