package com.example.minip.recommendation;

import com.example.minip.recommendation.ai.AiRecommendationResult;
import java.util.List;

public record CustomerRecommendationResponse(
    Long requestId,
    AiRecommendationResult.Status status,
    List<RecommendationView> recommendations,
    List<String> missingFields
) {
    public record RecommendationView(
        Long recommendationId,
        int rank,
        String outfitTitle,
        int matchScore,
        String stylingComment,
        List<WardrobeItemView> wardrobeItems,
        List<RentalItemView> rentalItems,
        long totalRentalPrice
    ) {
    }

    public record WardrobeItemView(Long wardrobeItemId, String itemName, long cost) {
    }

    public record RentalItemView(
        Long productId,
        Long productVariantId,
        String productName,
        String brandName,
        String size,
        long rentalPrice
    ) {
    }
}
