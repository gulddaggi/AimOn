package com.example.ffp_be.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
        "예상치 못한 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "유효성 검사에 실패했습니다."),
    ILLEGAL_STATE(HttpStatus.BAD_REQUEST, "ILLEGAL_STATE", "잘못된 상태입니다."),

    // 인증 관련 (Auth 도메인)
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "AUTH_001", "이미 가입된 사용자입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_002", "로그인에 실패했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_004", "리프레시 토큰이 만료되었습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_005", "잘못된 인증 정보입니다."),

    // 사용자 관련 (User 도메인)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_002", "사용자 프로필을 찾을 수 없습니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 닉네임입니다."),
    INVALID_LEVEL(HttpStatus.BAD_REQUEST, "USER_004", "레벨은 0 이상이어야 합니다."),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "USER_005", "인증 정보에 userId가 없습니다."),

    // 게시글 관련 (Post 도메인)
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "존재하지 않는 게시글입니다."),
    NOT_POST_AUTHOR(HttpStatus.FORBIDDEN, "POST_002", "작성자만 수행할 수 있는 작업입니다."),

    // 댓글 관련 (Comment 도메인)
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "존재하지 않는 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_002", "부모 댓글을 찾을 수 없습니다."),
    NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, "COMMENT_003", "작성자만 수행할 수 있는 작업입니다."),

    // 게임 관련 (Game 도메인)
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME_001", "존재하지 않는 게임입니다."),
    DUPLICATED_GAME(HttpStatus.CONFLICT, "GAME_002", "이미 존재하는 게임입니다."),

    // 팀 관련 (Team 도메인)
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_001", "존재하지 않는 팀입니다."),
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYER_001", "존재하지 않는 선수입니다."),

    // 매치 관련 (Match 도메인)
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_001", "존재하지 않는 경기입니다."),
    MATCH_INVALID_SCORE_PAIR(HttpStatus.BAD_REQUEST, "MATCH_002", "점수는 둘 다 null이거나 둘 다 존재해야 합니다."),

    // 리그 관련 (League 도메인)
    LEAGUE_NOT_FOUND(HttpStatus.NOT_FOUND, "LEAGUE_001", "존재하지 않는 리그입니다."),
    DUPLICATED_LEAGUE(HttpStatus.CONFLICT, "LEAGUE_002", "같은 게임에 이미 존재하는 리그명입니다."),
    LEAGUE_HAS_TEAMS(HttpStatus.CONFLICT, "LEAGUE_003", "팀이 소속된 리그는 삭제할 수 없습니다."),
    LEAGUE_HAS_MATCHES(HttpStatus.CONFLICT, "LEAGUE_004", "경기가 있는 리그는 삭제할 수 없습니다."),

    // 추천(Pick&Aim) 관련
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "RECO_001", "유효하지 않은 키워드입니다."),
    PICK_KEYWORDS_TOO_MANY(HttpStatus.BAD_REQUEST, "RECO_002", "키워드는 최대 3개까지 선택할 수 있습니다."),
    PICK_KEYWORD_META_NOT_FOUND(HttpStatus.NOT_FOUND, "RECO_003", "키워드 정보를 찾을 수 없습니다."),
    DUPLICATED_PICK_KEYWORD(HttpStatus.CONFLICT, "RECO_004", "이미 존재하는 키워드입니다."),

    // 뉴스 관련 (News 도메인)
    NAVER_CREDENTIALS_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_001", "네이버 API 자격 증명이 누락되었습니다."),
    NAVER_API_ERROR(HttpStatus.BAD_GATEWAY, "NEWS_002", "네이버 뉴스 API 호출에 실패했습니다."),

    // 클립 관련 (Clip 도메인)
    CLIP_NOT_FOUND(HttpStatus.NOT_FOUND, "CLIP_001", "존재하지 않는 클립입니다."),
    INVALID_YOUTUBE_URL(HttpStatus.BAD_REQUEST, "CLIP_002", "유효하지 않은 유튜브 링크입니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}


