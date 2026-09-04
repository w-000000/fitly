package com.example.minip.ai;

public class AiIntegrationException extends RuntimeException {
    public enum Reason {
        CONFIGURATION,
        UPSTREAM_FAILURE,
        INVALID_RESPONSE
    }

    private final Reason reason;

    public AiIntegrationException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public AiIntegrationException(Reason reason, Throwable cause) {
        super(reason.name(), cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
