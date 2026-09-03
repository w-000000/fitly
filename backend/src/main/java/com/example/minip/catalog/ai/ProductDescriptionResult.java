package com.example.minip.catalog.ai;

import java.util.List;

public record ProductDescriptionResult(
    Status status,
    String headline,
    String silhouetteAndFabric,
    String stylingAndTpo,
    List<String> details,
    List<String> reviewFlags
) {
    public enum Status {
        COMPLETED,
        NEEDS_INPUT
    }
}
