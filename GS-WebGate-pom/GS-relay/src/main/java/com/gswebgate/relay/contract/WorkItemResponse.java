package com.gswebgate.relay.contract;

import java.util.Objects;

/**
 * Response contract for work item submission.
 * Contains the assigned message ID for later result retrieval.
 */
public class WorkItemResponse {
    private String messageId;

    public WorkItemResponse() {
    }

    public WorkItemResponse(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkItemResponse that = (WorkItemResponse) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "WorkItemResponse{" +
               "messageId='" + messageId + '\'' +
               '}';
    }
}
