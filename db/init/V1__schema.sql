
CREATE TABLE IF NOT EXISTS `user_credential` (
  `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
  `email`        VARCHAR(100) NOT NULL UNIQUE,
  `password`     VARCHAR(255),
  `role`         VARCHAR(20) NOT NULL DEFAULT 'USER',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME
);

CREATE TABLE IF NOT EXISTS `refresh_token` (
  `token_id`     BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id`      BIGINT NOT NULL,
  `token`        VARCHAR(512) NOT NULL UNIQUE,
  `expires_at`   DATETIME NOT NULL,
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_refresh_token_user`
    FOREIGN KEY (`user_id`) REFERENCES `user_credential`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `users` (
  `id`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_credential_id`    BIGINT NOT NULL,
  `nickname`              VARCHAR(50) NOT NULL UNIQUE,
  `profile_image_url`     VARCHAR(255),
  `level`                 INT NOT NULL DEFAULT 0,
  `exp`                   INT NOT NULL DEFAULT 0,
  CONSTRAINT `uq_profile_user` UNIQUE (`user_credential_id`),
  CONSTRAINT `fk_users_user_credential`
    FOREIGN KEY (`user_credential_id`) REFERENCES `user_credential`(`id`) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS `game` (
  `id`   BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `league` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `game_id` BIGINT NOT NULL,
  `name`    VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_league_game_id` (`game_id`),
  CONSTRAINT `fk_league_game` FOREIGN KEY (`game_id`) REFERENCES `game`(`id`)
);

CREATE TABLE IF NOT EXISTS `team` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `country` varchar(100) NOT NULL,
    `team_name` varchar(100) NOT NULL,
    `game_id` BIGINT NOT NULL,
    `league_id` BIGINT NOT NULL,
    `win_rate` DOUBLE NOT NULL,
    `a_win_rate` DOUBLE NOT NULL,
    `d_win_rate` DOUBLE NOT NULL,
    `point` INT NOT NULL DEFAULT 0,
    `img_url` varchar(255) DEFAULT 'https://www.vlr.gg/img/base/ph/sil.png',
    PRIMARY KEY (`id`),
    KEY `idx_team_game_id` (`game_id`),
    KEY `idx_team_league_id` (`league_id`),
    CONSTRAINT `fk_team_game` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`),
    CONSTRAINT `fk_team_league` FOREIGN KEY (`league_id`) REFERENCES `league` (`id`)
);


CREATE TABLE IF NOT EXISTS `post_content` (
  `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id`    BIGINT NOT NULL,
  `title`      VARCHAR(100) NOT NULL,
  `body`       TEXT NOT NULL,
  `like_count` INT NOT NULL DEFAULT 0,
  `comment_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_post_content_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
);

CREATE TABLE IF NOT EXISTS `clip` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `video_id`      VARCHAR(50)  NOT NULL,
  `title`         VARCHAR(255) NOT NULL,
  `description`   TEXT,
  `video_url`     VARCHAR(255) NOT NULL,
  `thumbnail_url` VARCHAR(255) DEFAULT NULL,
  `channel_title` VARCHAR(255) DEFAULT NULL,
  `published_at`  DATETIME     DEFAULT NULL,
  `game_id`       BIGINT       NOT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `like_count`    BIGINT       DEFAULT NULL,
  `view_count`    BIGINT       DEFAULT NULL,
  `team_id`       BIGINT       DEFAULT NULL,
  `user_id`       BIGINT       DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_clip_video_id`        (`video_id`),
  KEY `idx_clip_game_published`  (`game_id`,`published_at`),
  KEY `idx_clip_team_published`  (`team_id`,`published_at`),
  KEY `idx_clip_user_created`    (`user_id`,`created_at`),
  CONSTRAINT `fk_clip_game` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_clip_team` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_clip_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS `comment` (
  `id`                 BIGINT NOT NULL AUTO_INCREMENT,
  `post_id`            BIGINT NOT NULL,
  `user_id`            BIGINT NULL,
  `parent_comment_id`  BIGINT DEFAULT NULL,
  `content`            TEXT NOT NULL,
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_post_id` (`post_id`),
  KEY `idx_comment_user_id` (`user_id`),
  KEY `idx_comment_parent_id` (`parent_comment_id`),
  CONSTRAINT `fk_comment_post`   FOREIGN KEY (`post_id`) REFERENCES `post_content`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user`   FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `comment`(`id`) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS `team_like` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) DEFAULT NULL,
  `team_id`    BIGINT NOT NULL,
  `user_id`    BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_team_like_team_id` (`team_id`),
  KEY `idx_team_like_user_id` (`user_id`),
  CONSTRAINT `fk_team_like_team` FOREIGN KEY (`team_id`) REFERENCES `team`(`id`),
  CONSTRAINT `fk_team_like_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
);

CREATE TABLE IF NOT EXISTS `news` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `content`      TEXT,
  `link`         VARCHAR(1000) NOT NULL,
  `published_at` DATETIME(6) NOT NULL,
  `title`        VARCHAR(255) NOT NULL,
  `game_id`      BIGINT NOT NULL,
  `team_id`      BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_news_game_id` (`game_id`),
  KEY `idx_news_team_id` (`team_id`),
  CONSTRAINT `fk_news_team` FOREIGN KEY (`team_id`) REFERENCES `team`(`id`),
  CONSTRAINT `fk_news_game` FOREIGN KEY (`game_id`) REFERENCES `game`(`id`)
);

CREATE TABLE IF NOT EXISTS `game_like` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) DEFAULT NULL,
  `game_id`    BIGINT NOT NULL,
  `user_id`    BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_game_like_game_id` (`game_id`),
  KEY `idx_game_like_user_id` (`user_id`),
  CONSTRAINT `fk_game_like_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
  CONSTRAINT `fk_game_like_game` FOREIGN KEY (`game_id`) REFERENCES `game`(`id`)
);

CREATE TABLE IF NOT EXISTS `post_like` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT NOT NULL,
  `post_id`    BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_post` (`user_id`, `post_id`),
  CONSTRAINT `fk_post_like_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `post_content`(`id`) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS `player` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `team_id` bigint DEFAULT NULL,
    `game_id` bigint DEFAULT NULL,
    `name` varchar(100) NOT NULL,
    `handle` varchar(100) NOT NULL,
    `country` varchar(50) NOT NULL,
    `img_url` varchar(255) DEFAULT 'https://www.vlr.gg/img/base/ph/sil.png',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_player_handle` (`handle`),
    KEY `team_id` (`team_id`),
    KEY `game_id` (`game_id`),
    CONSTRAINT `player_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE SET NULL,
    CONSTRAINT `player_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS `valorant_player_stats` (
    `player_id` BIGINT NOT NULL,
    `round`     INT    NOT NULL,
    `acs`   DOUBLE NOT NULL,
    `kast`  DOUBLE NOT NULL,
    `adr`   DOUBLE NOT NULL,
    `apr`   DOUBLE NOT NULL,
    `fkpr`  DOUBLE NOT NULL,
    `fdpr`  DOUBLE NOT NULL,
    `hs`    DOUBLE NOT NULL,
    `cl`    DOUBLE NOT NULL,
    `kda`   DOUBLE NOT NULL,
    PRIMARY KEY (`player_id`),
    CONSTRAINT `fk_valo_stats_player` FOREIGN KEY (`player_id`)
        REFERENCES `player`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `matches` (
    `id`           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `team_id`      BIGINT NOT NULL,
    `op_team`      VARCHAR(100) NOT NULL,
    `game_id`      BIGINT NOT NULL,
    `league_id`    BIGINT NOT NULL,
    `match_date`   DATETIME NOT NULL,
    `my_score`     INT NULL,
    `op_score`     INT NULL,
    `is_played`    BOOLEAN NOT NULL DEFAULT FALSE,
    `vlr_match_id` BIGINT NULL UNIQUE,

    CONSTRAINT `fk_match_team`
    FOREIGN KEY (`team_id`)     REFERENCES `team`(`id`)   ON DELETE CASCADE,
    CONSTRAINT `fk_match_game`
    FOREIGN KEY (`game_id`)     REFERENCES `game`(`id`)   ON DELETE CASCADE,
    CONSTRAINT `fk_match_league`
    FOREIGN KEY (`league_id`)   REFERENCES `league`(`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_scores` CHECK ((`my_score` IS NULL AND `op_score` IS NULL)
        OR(`my_score` >= 0 AND `op_score` >= 0)),
    KEY `idx_match_played_date` (`is_played`, `match_date`),
    KEY `idx_match_team_date`   (`team_id`, `match_date`),
    KEY `idx_match_league_date` (`league_id`, `match_date`),
    KEY `idx_match_game_date`   (`game_id`, `match_date`)
);

CREATE TABLE IF NOT EXISTS `embeddings` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `vector_index` INT NOT NULL,
    `title` TEXT NOT NULL,
    `content` TEXT NOT NULL,
    `embedding` JSON NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (`vector_index`)
);

CREATE TABLE IF NOT EXISTS `pick_keyword_meta` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `keyword_key`   VARCHAR(50)  NOT NULL UNIQUE,
    `display_name`  VARCHAR(100) NOT NULL,
    `description`   TEXT NULL,
    `active`        BOOLEAN NOT NULL DEFAULT TRUE,
    `display_order` INT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_pick_keyword_active_order` (`active`, `display_order`)
);

CREATE TABLE IF NOT EXISTS  `post_images` (
    `post_id`   BIGINT      NOT NULL,
    `image_key` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`post_id`, `image_key`),
    CONSTRAINT `fk_post_images_post`
    FOREIGN KEY (`post_id`) REFERENCES `post_content`(`id`)
   ON DELETE CASCADE
);