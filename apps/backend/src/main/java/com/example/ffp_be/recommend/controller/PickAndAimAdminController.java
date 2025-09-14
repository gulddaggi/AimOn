package com.example.ffp_be.recommend.controller;

import com.example.ffp_be.recommend.domain.PickKeyword;
import com.example.ffp_be.recommend.dto.response.PickAndAimKeywordMetaResponse;
import com.example.ffp_be.recommend.entity.PickKeywordMeta;
import com.example.ffp_be.recommend.repository.PickKeywordMetaRepository;
import com.example.ffp_be.recommend.exception.PickKeywordMetaNotFoundException;
import com.example.ffp_be.recommend.exception.DuplicatedPickKeywordException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/pick-aim/keywords")
@RequiredArgsConstructor
@Validated
@Tag(name = "Pick&Aim Admin", description = "Pick 키워드 메타 CRUD")
public class PickAndAimAdminController {

    private final PickKeywordMetaRepository keywordMetaRepository;

    @GetMapping
    @Operation(summary = "키워드 메타 전체 조회")
    public ResponseEntity<List<PickAndAimKeywordMetaResponse>> listAll() {
        return ResponseEntity.ok(keywordMetaRepository.findAll().stream().map(
            m -> PickAndAimKeywordMetaResponse.of(m.getKeywordKey(), m.getDisplayName(),
                m.getDescription())).toList());
    }

    @PostMapping
    @Operation(summary = "키워드 메타 생성")
    public ResponseEntity<PickAndAimKeywordMetaResponse> create(@RequestParam("key") String key,
        @RequestParam("displayName") String displayName,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "active", required = false, defaultValue = "true") boolean active,
        @RequestParam(value = "order", required = false) Integer order) {
        PickKeyword k = PickKeyword.fromKey(key);
        keywordMetaRepository.findByKeywordKey(k).ifPresent(m -> {
            throw new DuplicatedPickKeywordException(key);
        });
        PickKeywordMeta saved = keywordMetaRepository.save(
            PickKeywordMeta.builder().keywordKey(k).displayName(displayName)
                .description(description).active(active).displayOrder(order).build());
        return ResponseEntity.ok(
            PickAndAimKeywordMetaResponse.of(saved.getKeywordKey(), saved.getDisplayName(),
                saved.getDescription()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "키워드 메타 수정")
    public ResponseEntity<PickAndAimKeywordMetaResponse> update(@PathVariable Long id,
        @RequestParam(value = "displayName", required = false) String displayName,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "order", required = false) Integer order) {
        PickKeywordMeta meta = keywordMetaRepository.findById(id)
            .orElseThrow(PickKeywordMetaNotFoundException::new);
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }
        if (description != null) {
            meta.setDescription(description);
        }
        if (active != null) {
            meta.setActive(active);
        }
        if (order != null) {
            meta.setDisplayOrder(order);
        }
        PickKeywordMeta saved = keywordMetaRepository.save(meta);
        return ResponseEntity.ok(
            PickAndAimKeywordMetaResponse.of(saved.getKeywordKey(), saved.getDisplayName(),
                saved.getDescription()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "키워드 메타 삭제")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        PickKeywordMeta meta = keywordMetaRepository.findById(id)
            .orElseThrow(PickKeywordMetaNotFoundException::new);
        keywordMetaRepository.delete(meta);
        return ResponseEntity.noContent().build();
    }
}


