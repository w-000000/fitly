package com.example.minip.catalog.ai;

import com.example.minip.ai.VisionFeatures;

public record ProductDescriptionContext(
    String productName,
    String brandName,
    String category,
    String providedInformation,
    VisionFeatures visionFeatures
) {
}
