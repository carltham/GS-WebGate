package com.gswebgate.relay.db;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Persistence entity for a work item in the relay queue.
 * Tracks the lifecycle of a single search request.
 */
@Entity
@Table(name = "work_items")
public class WorkItem {

    @Id
    @Column(name = "message_id", length = 36)
    private String messageId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    @Column(name = "target", length = 255)
    private String target;

    @Column(name = "mode", length = 50)
    private String mode;

    @Column(name = "state", nullable = false, length = 20)
    private String state;  // submitted, pending, claimed, completed, consumed

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WorkItem() {
    }

    public WorkItem(String messageId, String question) {
        this.messageId = messageId;
        this.question = question;
        this.state = "submitted";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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
        WorkItem workItem = (WorkItem) o;
        return Objects.equals(messageId, workItem.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "WorkItem{" +
               "messageId='" + messageId + '\'' +
               ", question='" + question + '\'' +
               ", state='" + state + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
