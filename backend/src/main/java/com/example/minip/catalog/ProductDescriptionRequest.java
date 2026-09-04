package com.example.minip.catalog;

import com.example.minip.ai.VisionFeatures;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductDescriptionRequest(
    @NotNull Long businessId,
    @NotNull Long businessMemberId,
    Long productId,
    @NotBlank String inputImageUrl,
    @NotNull @Valid ProductInput product,
    @NotNull VisionFeatures visionFeatures
) {
    public record ProductInput(
        @NotBlank String productName,
        @NotBlank String brandName,
        @NotBlank String category,
        String providedInformation
    ) {
    }
}
