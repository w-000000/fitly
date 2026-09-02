package com.example.minip.note;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Note {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String aiSummary;
    private Instant createdAt;

    protected Note() {}

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAiSummary() { return aiSummary; }
    public Instant getCreatedAt() { return createdAt; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}

