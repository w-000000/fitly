package com.example.minip.recommendation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CustomerRecommendationRequest(
    @NotBlank String tpo,
    Long styleId,
    String style,
    @NotBlank String size,
    @Positive Long budget,
    @NotEmpty List<@NotNull Long> wardrobeItemIds
) {
}
