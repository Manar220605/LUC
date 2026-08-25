package com.luc.qa.module.semantic.client;

import com.luc.qa.module.semantic.config.EmbedderProperties;
import com.luc.qa.module.semantic.dto.EmbedResponseDTO;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class EmbeddingClient {

    public static final int DIMS = 384;

    private final RestClient restClient;

    public EmbeddingClient(EmbedderProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        this.restClient = RestClient.builder()
            .baseUrl(properties.getUrl())
            .requestFactory(factory)
            .build();
    }

    public boolean probe() {
        try {
            restClient.get().uri("/health").retrieve().toBodilessEntity();
            return true;
        } catch (RestClientException ex) {
            log.warn("Embedder health check failed: {}", ex.getMessage());
            return false;
        }
    }

    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        try {
            EmbedResponseDTO response = restClient.post()
                .uri("/embed")
                .body(Map.of("texts", texts))
                .retrieve()
                .body(EmbedResponseDTO.class);
            if (response == null || response.embeddings() == null) {
                return List.of();
            }
            return response.embeddings().stream().map(this::toFloatArray).toList();
        } catch (RestClientException ex) {
            log.warn("Embedder request failed: {}", ex.getMessage());
            return List.of();
        }
    }

    public float[] embedOne(String text) {
        List<float[]> result = embed(List.of(text));
        return result.isEmpty() ? null : result.getFirst();
    }

    private float[] toFloatArray(List<Double> values) {
        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.get(i).floatValue();
        }
        return vector;
    }
}
