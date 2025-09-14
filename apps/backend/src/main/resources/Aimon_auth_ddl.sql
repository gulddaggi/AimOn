-- DB & 계정 ---------------------------------------------------------------
DROP DATABASE IF EXISTS aimonDB;

CREATE DATABASE IF NOT EXISTS aimonDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ssafy'@'localhost' IDENTIFIED BY 'ssafy';
GRANT ALL PRIVILEGES ON aimonDB.* TO 'ssafy'@'localhost';
FLUSH PRIVILEGES;

USE aimonDB;

-- 1) UserCredential: 계정/인증 --------------------------------------------
CREATE TABLE IF NOT EXISTS UserCredential (
  user_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  username     VARCHAR(100) NOT NULL UNIQUE,          -- 이메일 등
  password     VARCHAR(255),                          -- 소셜이면 NULL 가능
  role         VARCHAR(20) NOT NULL DEFAULT 'USER',
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP
);

-- 2) RefreshToken: 리프레시 토큰 ------------------------------------------
CREATE TABLE IF NOT EXISTS RefreshToken (
  token_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT NOT NULL,
  refresh_token  VARCHAR(512) NOT NULL UNIQUE,
  expires_at     DATETIME NOT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_refresh_user
    FOREIGN KEY (user_id) REFERENCES UserCredential(user_id)
    ON DELETE CASCADE
);

-- 3) UserProfile: 유저프로필(설계 필드 반영) ------------------------------
CREATE TABLE IF NOT EXISTS UserProfile (
  profile_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id            BIGINT NOT NULL,
  nickname           VARCHAR(50) NOT NULL UNIQUE,
  profile_image_url  VARCHAR(255),
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  level              INT NOT NULL DEFAULT 0,
  exp                INT NOT NULL DEFAULT 0,
  CONSTRAINT uq_profile_user UNIQUE (user_id),  -- 유저당 프로필 1개 보장
  CONSTRAINT fk_profile_user
    FOREIGN KEY (user_id) REFERENCES UserCredential(user_id)
    ON DELETE CASCADE
);
