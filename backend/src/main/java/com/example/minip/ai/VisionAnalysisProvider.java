package com.example.minip.ai;

public interface VisionAnalysisProvider {
    VisionFeatures analyze(VisionAnalysisInput input);

    record VisionAnalysisInput(Long wardrobeItemId, String imageUrl, String category, String color) {
    }
}
