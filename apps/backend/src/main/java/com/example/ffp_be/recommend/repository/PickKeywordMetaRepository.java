package com.example.ffp_be.recommend.repository;

import com.example.ffp_be.recommend.domain.PickKeyword;
import com.example.ffp_be.recommend.entity.PickKeywordMeta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickKeywordMetaRepository extends JpaRepository<PickKeywordMeta, Long> {

    List<PickKeywordMeta> findByActiveTrueOrderByDisplayOrderAscIdAsc();

    Optional<PickKeywordMeta> findByKeywordKey(PickKeyword key);
}


