package com.example.minip.recommendation.ai;

import com.example.minip.ai.VisionFeatures;
import java.util.List;

public record AiRecommendationContext(
    String tpo,
    List<String> preferredStyles,
    String size,
    Long budget,
    List<WardrobeCandidate> wardrobeItems,
    List<RentalCandidate> availableCandidates
) {
    public record WardrobeCandidate(
        Long wardrobeItemId,
        String itemName,
        VisionFeatures visionFeatures
    ) {
    }

    public record RentalCandidate(
        Long productId,
        Long productVariantId,
        String brandName,
        String productName,
        String category,
        String size,
        Long rentalPrice,
        int availableStock
    ) {
    }
}
