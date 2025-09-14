-- NEW_DB 스키마에 적용 (MySQL 8.x 가정)
-- 1) embeddings 검색 성능: FULLTEXT 인덱스 추가
ALTER TABLE embeddings
  ADD FULLTEXT INDEX ft_title_content (title, content);

-- 2) vector_index 고유성 보장(선택)
ALTER TABLE embeddings
  ADD UNIQUE KEY uk_vector_index (vector_index);


