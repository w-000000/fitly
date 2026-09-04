package com.example.minip.ai;

public record VisionFeatures(
    String category,
    String subcategory,
    String primaryColor,
    String fit,
    String silhouette,
    String pattern,
    String styleType,
    Double confidence
) {
    public boolean hasUsableFeature() {
        return known(category) || known(primaryColor) || known(fit)
            || known(silhouette) || known(pattern);
    }

    private boolean known(String value) {
        return value != null && !value.isBlank() && !"UNKNOWN".equalsIgnoreCase(value);
    }
}
