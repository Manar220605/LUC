package com.luc.qa.common.seed;

import com.luc.qa.module.semantic.service.SemanticIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBackfillRunner implements CommandLineRunner {

    private final SemanticIndexService semanticIndexService;

    @Override
    public void run(String... args) {
        try {
            semanticIndexService.backfill();
        } catch (Exception ex) {
            log.warn("Embedding backfill failed: {}", ex.getMessage());
        }
    }
}
