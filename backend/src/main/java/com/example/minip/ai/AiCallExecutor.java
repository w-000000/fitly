package com.example.minip.ai;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class AiCallExecutor {
    private static final int MAX_ATTEMPTS = 2;

    public <T> T execute(Supplier<T> operation) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return operation.get();
            } catch (RuntimeException exception) {
                lastFailure = exception;
                if (attempt == MAX_ATTEMPTS || !isRetryable(exception)) {
                    break;
                }
            }
        }
        throw new AiIntegrationException(classify(lastFailure), lastFailure);
    }

    private boolean isRetryable(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            String name = current.getClass().getSimpleName();
            if (current instanceof AiOutputException || name.contains("TransientAiException")
                || name.contains("Json") || name.contains("OutputConverter")) {
                return true;
            }
            if (name.contains("NonTransientAiException")) {
                return false;
            }
        }
        return false;
    }

    private AiIntegrationException.Reason classify(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            String name = current.getClass().getSimpleName();
            if (current instanceof AiOutputException || name.contains("Json")
                || name.contains("OutputConverter")) {
                return AiIntegrationException.Reason.INVALID_RESPONSE;
            }
        }
        return AiIntegrationException.Reason.UPSTREAM_FAILURE;
    }
}
