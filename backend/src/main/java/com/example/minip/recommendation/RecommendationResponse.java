package com.example.minip.recommendation;

import java.util.List;

public record RecommendationResponse(String message, String model, int rentalDays, List<ProductResult> products) {
    public record ProductResult(
        Long id,
        String category,
        String name,
        String description,
        int rentalPrice,
        int purchasePrice,
        String reason,
        int stock
    ) {}
}
