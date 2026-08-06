package com.gswebgate.relay.db;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents work items that failed during search execution.
 * Part of the dead-letter queue for failed searches.
 */
@Entity
@Table(name = "failed_work")
public class FailedWork {
    
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
    
    @Column(nullable = false, length = 1000)
    private String errorMessage;
    
    @Column(nullable = false)
    private int attemptCount;
    
    @Column(nullable = false)
    private LocalDateTime firstFailureTime;
    
    @Column(nullable = false)
    private LocalDateTime lastFailureTime;
    
    @Column
    @Enumerated(EnumType.STRING)
    private FailureReason failureReason;
    
    public enum FailureReason {
        NETWORK_ERROR,
        TIMEOUT,
        SEARCH_EXECUTION_ERROR,
        UNKNOWN_ERROR
    }

    // Constructors
    public FailedWork() {
    }

    public FailedWork(String messageId, String question, String context, String target, 
                     String mode, String errorMessage, FailureReason reason) {
        this.messageId = messageId;
        this.question = question;
        this.context = context;
        this.target = target;
        this.mode = mode;
        this.errorMessage = errorMessage;
        this.failureReason = reason;
        this.attemptCount = 1;
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

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(FailureReason failureReason) {
        this.failureReason = failureReason;
    }

    @Override
    public String toString() {
        return "FailedWork{" +
               "messageId='" + messageId + '\'' +
               ", question='" + question + '\'' +
               ", attemptCount=" + attemptCount +
               ", failureReason=" + failureReason +
               ", errorMessage='" + errorMessage + '\'' +
               ", lastFailureTime=" + lastFailureTime +
               '}';
    }
}
