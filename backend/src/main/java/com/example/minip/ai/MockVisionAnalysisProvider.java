package com.example.minip.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "fitly.ai", name = "vision-provider", havingValue = "mock", matchIfMissing = true
)
public class MockVisionAnalysisProvider implements VisionAnalysisProvider {
    @Override
    public VisionFeatures analyze(VisionAnalysisInput input) {
        return new VisionFeatures(value(input.category()), "UNKNOWN", value(input.color()),
            "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", 1.0);
    }

    private String value(String input) {
        return input == null || input.isBlank() ? "UNKNOWN" : input;
    }
}
