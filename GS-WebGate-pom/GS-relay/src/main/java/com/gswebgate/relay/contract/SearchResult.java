package com.gswebgate.relay.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * Result contract for completed search execution.
 * Contains the answer, confidence, and processing metadata.
 */
public class SearchResult {
    @NotBlank(message = "Message ID is required")
    private String messageId;

    @NotNull(message = "Answer found flag is required")
    private Boolean answerFound;

    @Size(max = 5000, message = "Answer must not exceed 5000 characters")
    private String answer;

    @DecimalMin(value = "0.0", message = "Confidence must be >= 0")
    @DecimalMax(value = "1.0", message = "Confidence must be <= 1.0")
    private Double confidence;

    private List<String> sources;

    @Min(value = 0, message = "Processing time must be >= 0")
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
