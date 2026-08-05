package com.noprobit.webgate.searcher;

/**
 * Search Result Model
 * Represents the result of an internet search for purpose verification
 */
public class SearchResult {

    private boolean relevant;
    private String reason;
    private String source;
    private double confidence;
    private long processingTime;

    // Getters and Setters
    public boolean isRelevant() {
        return relevant;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    @Override
    public String toString() {
        return String.format(
            "SearchResult{relevant=%b, reason='%s', source='%s', confidence=%.2f, time=%dms}",
            relevant, reason, source, confidence, processingTime
        );
    }
}
