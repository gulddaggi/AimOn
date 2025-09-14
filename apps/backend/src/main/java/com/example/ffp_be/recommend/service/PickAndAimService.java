package com.example.ffp_be.recommend.service;

import com.example.ffp_be.recommend.dto.response.PickAndAimCandidateResponse;
import com.example.ffp_be.recommend.dto.request.PickAndAimFilterRequest;
import com.example.ffp_be.recommend.dto.response.PickAndAimLeagueResponse;
import com.example.ffp_be.recommend.dto.response.PickAndAimKeywordMetaResponse;
import java.util.List;

public interface PickAndAimService {

    List<PickAndAimLeagueResponse> getSupportedLeagues();

    List<PickAndAimCandidateResponse> getCandidates(PickAndAimFilterRequest request);

    List<PickAndAimKeywordMetaResponse> getActiveKeywords();
}


