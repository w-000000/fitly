package com.example.minip.ai;

public interface AiSummaryProvider {
    AiJobResponse.Result summarize(String title, String content);
}

