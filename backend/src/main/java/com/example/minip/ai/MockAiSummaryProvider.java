package com.example.minip.ai;

import org.springframework.stereotype.Component;

@Component
public class MockAiSummaryProvider implements AiSummaryProvider {
    @Override
    public AiJobResponse.Result summarize(String title, String content) {
        String normalized = content.replaceAll("\\s+", " ").trim();
        String excerpt = normalized.length() > 80 ? normalized.substring(0, 80) + "…" : normalized;
        return new AiJobResponse.Result(title + ": " + excerpt, "mock-summary-v1", true);
    }
}

