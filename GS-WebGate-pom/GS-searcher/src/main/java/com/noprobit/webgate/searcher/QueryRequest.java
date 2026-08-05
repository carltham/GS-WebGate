package com.noprobit.webgate.searcher;

public class QueryRequest {
    private String question;
    private String context;
    private int maxResults;
    private long timeout;

    public QueryRequest() {
        this.maxResults = 5;
        this.timeout = 5000;
        this.context = null;
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

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return String.format(
            "QueryRequest{question='%s', context='%s', maxResults=%d, timeout=%dms}",
            question, context, maxResults, timeout
        );
    }
}
