package com.example.minip.note;

import com.example.minip.ai.AiJobResponse;
import com.example.minip.ai.AiSummaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteRepository notes;
    private final AiSummaryService aiSummaryService;

    public NoteController(NoteRepository notes, AiSummaryService aiSummaryService) {
        this.notes = notes;
        this.aiSummaryService = aiSummaryService;
    }

    @GetMapping
    public List<Note> list() {
        return notes.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note create(@Valid @RequestBody CreateNoteRequest request) {
        return notes.save(new Note(request.title(), request.content()));
    }

    @PostMapping("/{id}/ai-summary")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AiJobResponse summarize(@PathVariable Long id) {
        Note note = notes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."));
        return aiSummaryService.start(note);
    }

    public record CreateNoteRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 2000) String content
    ) {}
}

