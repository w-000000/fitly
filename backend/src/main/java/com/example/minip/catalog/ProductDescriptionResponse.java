package com.example.minip.catalog;

public record ProductDescriptionResponse(
    Long generationId,
    ProductDescriptionGeneration.Status status,
    String generatedDescription
) {
}
