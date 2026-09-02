package com.example.minip.ai;

import java.util.UUID;

public record AiJobResponse(UUID jobId, Status status, Result result, String error) {
    public enum Status { PENDING, COMPLETED, FAILED }
    public record Result(String summary, String model, boolean mock) {}
}

