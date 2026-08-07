package com.gswebgate.relay.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

/**
 * DTO for receiving dead-letter queue sync requests from searcher.
 * Represents a failed work item being synced from searcher's local DLQ.
 */
public class DLQSyncRequest {
    
    @NotBlank(message = "Message ID is required")
    private String messageId;

    @NotBlank(message = "Question is required")
    @Size(min = 1, max = 1000, message = "Question must be between 1 and 1000 characters")
    private String question;

    @Size(max = 5000, message = "Context must not exceed 5000 characters")
    private String context;

    @Size(max = 500, message = "Target must not exceed 500 characters")
    private String target;

    @Size(max = 100, message = "Mode must not exceed 100 characters")
    private String mode;

    @NotBlank(message = "Error message is required")
    @Size(min = 1, max = 1000, message = "Error message must be between 1 and 1000 characters")
    private String errorMessage;

    @NotBlank(message = "Failure reason is required")
    private String failureReason;

    @Min(value = 1, message = "Attempt count must be >= 1")
    @NotNull(message = "Attempt count is required")
    private Integer attemptCount;

    public DLQSyncRequest() {
    }

    public DLQSyncRequest(String messageId, String question, String context, String target,
                        String mode, String errorMessage, String failureReason, Integer attemptCount) {
        this.messageId = messageId;
        this.question = question;
        this.context = context;
        this.target = target;
        this.mode = mode;
        this.errorMessage = errorMessage;
        this.failureReason = failureReason;
        this.attemptCount = attemptCount;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public int getAttemptCount() {
        return attemptCount != null ? attemptCount : 0;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    @Override
    public String toString() {
        return "DLQSyncRequest{" +
                "messageId='" + messageId + '\'' +
                ", question='" + question + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", failureReason='" + failureReason + '\'' +
                ", attemptCount=" + attemptCount +
                '}';
    }
}
