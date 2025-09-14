package com.example.ffp_be.post.repository;

import com.example.ffp_be.post.entity.PostContentEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostContentRepository extends JpaRepository<PostContentEntity, Long> {

    List<PostContentEntity> findByTitleContainingIgnoreCase(String title, Sort sort);

    @EntityGraph(attributePaths = {"user"})
    @Query("select p from PostContentEntity p")
    List<PostContentEntity> findAllWithUser(Sort sort);

    @EntityGraph(attributePaths = {"user"})
    @Query("select p from PostContentEntity p")
    Page<PostContentEntity> findAllWithUser(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("select p from PostContentEntity p where lower(p.title) like lower(concat('%', :title, '%'))")
    List<PostContentEntity> findByTitleContainingIgnoreCaseWithUser(String title, Sort sort);

    @EntityGraph(attributePaths = {"user"})
    List<PostContentEntity> findAllByUserId(Long userId, Sort sort);
}
