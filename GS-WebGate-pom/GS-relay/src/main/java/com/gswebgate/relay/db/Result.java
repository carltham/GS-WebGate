package com.gswebgate.relay.db;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Persistence entity for a search result in the relay queue.
 * Stores the answer and metadata returned by the searcher.
 */
@Entity
@Table(name = "results")
public class Result {

    @Id
    @Column(name = "message_id", length = 36)
    private String messageId;

    @Column(name = "answer_found", nullable = false)
    private boolean answerFound;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "sources", columnDefinition = "TEXT")
    private String sources;  // JSON array as string

    @Column(name = "processing_time_ms", nullable = false)
    private long processingTimeMs;

    @Column(name = "state", nullable = false, length = 20)
    private String state;  // completed, consumed

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Result() {
    }

    public Result(String messageId, boolean answerFound, String answer, double confidence) {
        this.messageId = messageId;
        this.answerFound = answerFound;
        this.answer = answer;
        this.confidence = confidence;
        this.state = "completed";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isAnswerFound() {
        return answerFound;
    }

    public void setAnswerFound(boolean answerFound) {
        this.answerFound = answerFound;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Objects.equals(messageId, result.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "Result{" +
               "messageId='" + messageId + '\'' +
               ", answerFound=" + answerFound +
               ", confidence=" + confidence +
               ", state='" + state + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
