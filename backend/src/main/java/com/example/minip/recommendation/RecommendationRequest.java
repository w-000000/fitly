package com.example.minip.recommendation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
