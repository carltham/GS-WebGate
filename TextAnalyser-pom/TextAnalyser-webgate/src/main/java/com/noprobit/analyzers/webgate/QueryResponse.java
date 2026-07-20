package com.noprobit.analyzers.webgate;

import java.util.ArrayList;
import java.util.List;

public class QueryResponse {
    private String question;
    private String answer;
    private String context;
    private List<String> sources;
    private double confidence;
    private long processingTime;
    private boolean answerFound;
    private String summary;
    private int maxResults;

    public QueryResponse() {
        this.sources = new ArrayList<>();
        this.confidence = 0.0;
        this.answerFound = false;
        this.maxResults = 5;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public void addSource(String source) {
        this.sources.add(source);
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.min(1.0, Math.max(0.0, confidence));
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public boolean isAnswerFound() {
        return answerFound;
    }

    public void setAnswerFound(boolean answerFound) {
        this.answerFound = answerFound;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return String.format(
            "QueryResponse{found=%b, confidence=%.2f, sources=%d, time=%dms}",
            answerFound, confidence, sources.size(), processingTime
        );
    }
}
