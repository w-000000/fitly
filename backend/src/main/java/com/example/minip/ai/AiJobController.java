package com.example.minip.ai;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai-jobs")
public class AiJobController {
    private final AiSummaryService service;

    public AiJobController(AiSummaryService service) { this.service = service; }

    @GetMapping("/{jobId}")
    public AiJobResponse get(@PathVariable UUID jobId) {
        AiJobResponse job = service.find(jobId);
        if (job == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI 작업을 찾을 수 없습니다.");
        return job;
    }
}

