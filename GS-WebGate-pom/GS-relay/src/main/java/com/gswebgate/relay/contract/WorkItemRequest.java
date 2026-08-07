package com.gswebgate.relay.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * Request contract for submitting a work item to the relay.
 * Contains the question and optional context for search execution.
 */
public class WorkItemRequest {
    @NotBlank(message = "Question is required")
    @Size(min = 1, max = 1000, message = "Question must be between 1 and 1000 characters")
    private String question;

    @Size(max = 5000, message = "Context must not exceed 5000 characters")
    private String context;

    @Size(max = 500, message = "Target must not exceed 500 characters")
    private String target;

    @Size(max = 100, message = "Mode must not exceed 100 characters")
    private String mode;

    public WorkItemRequest() {
    }

    public WorkItemRequest(String question) {
        this.question = question;
    }

    public WorkItemRequest(String question, String context) {
        this.question = question;
        this.context = context;
    }

    public WorkItemRequest(String question, String context, String target, String mode) {
        this.question = question;
        this.context = context;
        this.target = target;
        this.mode = mode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkItemRequest that = (WorkItemRequest) o;
        return Objects.equals(question, that.question) &&
               Objects.equals(context, that.context) &&
               Objects.equals(target, that.target) &&
               Objects.equals(mode, that.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, context, target, mode);
    }

    @Override
    public String toString() {
        return "WorkItemRequest{" +
               "question='" + question + '\'' +
               ", context='" + context + '\'' +
               ", target='" + target + '\'' +
               ", mode='" + mode + '\'' +
               '}';
    }
}
