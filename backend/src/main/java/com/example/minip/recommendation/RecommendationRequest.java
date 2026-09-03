package com.example.minip.recommendation;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;

public record RecommendationRequest(
    @NotNull RentalPurpose purpose,
    PersonalSituation personalSituation,
    ClothingSize size,
    @Size(max = 100) String groupName,
    @Size(max = 100) String activityType,
    Map<ClothingSize, @Min(0) @Max(100) Integer> groupSizes,
    @Min(10000) int budget,
    @NotNull @FutureOrPresent LocalDate rentalStartDate,
    @NotNull LocalDate rentalEndDate
) {}
