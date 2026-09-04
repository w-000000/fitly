package com.example.minip.recommendation.ai;

import java.util.List;

public record AiRecommendationResult(
    Status status,
    List<Outfit> recommendations,
    List<String> missingFields
) {
    public enum Status {
        COMPLETED,
        NO_MATCH,
        NEEDS_INPUT
    }

    public record Outfit(
        int rank,
        String outfitTitle,
        int matchScore,
        String stylingReason,
        List<Long> wardrobeItemIds,
        List<Long> rentalProductVariantIds
    ) {
    }
}
