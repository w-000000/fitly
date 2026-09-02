package com.example.minip.ai;

import com.example.minip.note.Note;
import com.example.minip.note.NoteRepository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class AiSummaryService {
    private final Map<UUID, AiJobResponse> jobs = new ConcurrentHashMap<>();
    private final AiSummaryProvider provider;
    private final NoteRepository notes;
    private final TaskExecutor taskExecutor;

    public AiSummaryService(AiSummaryProvider provider, NoteRepository notes, TaskExecutor taskExecutor) {
        this.provider = provider;
        this.notes = notes;
        this.taskExecutor = taskExecutor;
    }

    public AiJobResponse start(Note note) {
        UUID jobId = UUID.randomUUID();
        AiJobResponse pending = new AiJobResponse(jobId, AiJobResponse.Status.PENDING, null, null);
        jobs.put(jobId, pending);
        taskExecutor.execute(() -> process(jobId, note.getId(), note.getTitle(), note.getContent()));
        return pending;
    }

    private void process(UUID jobId, Long noteId, String title, String content) {
        try {
            AiJobResponse.Result result = provider.summarize(title, content);
            notes.findById(noteId).ifPresent(note -> { note.setAiSummary(result.summary()); notes.save(note); });
            jobs.put(jobId, new AiJobResponse(jobId, AiJobResponse.Status.COMPLETED, result, null));
        } catch (RuntimeException exception) {
            jobs.put(jobId, new AiJobResponse(jobId, AiJobResponse.Status.FAILED, null, exception.getMessage()));
        }
    }

    public AiJobResponse find(UUID jobId) {
        return jobs.get(jobId);
    }
}
