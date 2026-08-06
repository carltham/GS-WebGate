package com.gswebgate.searcher.contract;

import java.util.Objects;

/**
 * Wrapper for work that is polled from the relay.
 * Includes both the work item and the message ID for correlation.
 */
public class PolledWork {
    private String messageId;
    private String question;
    private String context;
    private String target;
    private String mode;

    public PolledWork() {
    }

    public PolledWork(String messageId, String question) {
        this.messageId = messageId;
        this.question = question;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolledWork that = (PolledWork) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "PolledWork{" +
               "messageId='" + messageId + '\'' +
               ", question='" + question + '\'' +
               ", context='" + context + '\'' +
               ", target='" + target + '\'' +
               ", mode='" + mode + '\'' +
               '}';
    }
}
