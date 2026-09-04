package com.example.minip.ai;

public class AiOutputException extends RuntimeException {
    public AiOutputException() {
        super("AI structured output is invalid");
    }
}
