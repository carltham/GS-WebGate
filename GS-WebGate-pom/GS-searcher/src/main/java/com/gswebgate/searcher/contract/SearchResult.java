package com.gswebgate.searcher.contract;

import java.util.List;
import java.util.Objects;

/**
 * Result contract for completed search execution.
 */
public class SearchResult {
    private String messageId;
    private boolean answerFound;
    private String answer;
    private double confidence;
    private List<String> sources;
    private long processingTimeMs;

    public SearchResult() {
    }

    public SearchResult(String messageId, boolean answerFound, String answer, double confidence) {
        this.messageId = messageId;
        this.answerFound = answerFound;
        this.answer = answer;
        this.confidence = confidence;
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

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchResult that = (SearchResult) o;
        return answerFound == that.answerFound &&
               Double.compare(that.confidence, confidence) == 0 &&
               processingTimeMs == that.processingTimeMs &&
               Objects.equals(messageId, that.messageId) &&
               Objects.equals(answer, that.answer) &&
               Objects.equals(sources, that.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, answerFound, answer, confidence, sources, processingTimeMs);
    }

    @Override
    public String toString() {
        return "SearchResult{" +
               "messageId='" + messageId + '\'' +
               ", answerFound=" + answerFound +
               ", answer='" + answer + '\'' +
               ", confidence=" + confidence +
               ", sources=" + sources +
               ", processingTimeMs=" + processingTimeMs +
               '}';
    }
}
