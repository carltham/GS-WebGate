package com.gswebgate.searcher.db;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for persisting failed work items to the searcher's local database.
 * This allows failures to survive searcher restarts and enables monitoring/retry logic.
 */
@Entity
@Table(name = "failure_records")
public class FailureRecord {

    @Id
    private String messageId;

    @Column(nullable = false)
    private String question;

    @Column
    private String context;

    @Column
    private String target;

    @Column
    private String mode;

    @Column(length = 1000)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FailureReason failureReason;

    @Column(nullable = false)
    private int attemptCount = 0;

    @Column(nullable = false)
    private LocalDateTime firstFailureTime;

    @Column(nullable = false)
    private LocalDateTime lastFailureTime;

    public enum FailureReason {
        NETWORK_ERROR,
        TIMEOUT,
        SEARCH_EXECUTION_ERROR,
        UNKNOWN_ERROR
    }

    // Constructors
    public FailureRecord() {
    }

    public FailureRecord(String messageId, String question, String context, 
                         String target, String mode) {
        this.messageId = messageId;
        this.question = question;
        this.context = context;
        this.target = target;
        this.mode = mode;
        this.attemptCount = 0;
        this.firstFailureTime = LocalDateTime.now();
        this.lastFailureTime = LocalDateTime.now();
    }

    // Getters and Setters
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(FailureReason failureReason) {
        this.failureReason = failureReason;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getFirstFailureTime() {
        return firstFailureTime;
    }

    public void setFirstFailureTime(LocalDateTime firstFailureTime) {
        this.firstFailureTime = firstFailureTime;
    }

    public LocalDateTime getLastFailureTime() {
        return lastFailureTime;
    }

    public void setLastFailureTime(LocalDateTime lastFailureTime) {
        this.lastFailureTime = lastFailureTime;
    }

    @Override
    public String toString() {
        return String.format("FailureRecord{msgId=%s, attempts=%d, reason=%s, error=%s}",
                messageId, attemptCount, failureReason, errorMessage);
    }
}
