package com.example.minip.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AiCallExecutorTest {
    private final AiCallExecutor executor = new AiCallExecutor();

    @Test
    void retriesStructuredOutputFailureOnlyOnce() {
        AtomicInteger attempts = new AtomicInteger();

        String result = executor.execute(() -> {
            if (attempts.incrementAndGet() == 1) {
                throw new AiOutputException();
            }
            return "completed";
        });

        assertThat(result).isEqualTo("completed");
        assertThat(attempts).hasValue(2);
    }

    @Test
    void doesNotRetryNonTransientFailure() {
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            throw new IllegalArgumentException("invalid request");
        }))
            .isInstanceOf(AiIntegrationException.class)
            .extracting(exception -> ((AiIntegrationException) exception).getReason())
            .isEqualTo(AiIntegrationException.Reason.UPSTREAM_FAILURE);
        assertThat(attempts).hasValue(1);
    }
}
