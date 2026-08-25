package com.luc.qa.module.semantic.dto;

import java.util.List;

public record EmbedResponseDTO(List<List<Double>> embeddings, int dims, String model) {
}
