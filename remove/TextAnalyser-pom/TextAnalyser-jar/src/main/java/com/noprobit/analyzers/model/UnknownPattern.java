package com.noprobit.analyzers.analyzers.model;

/**
 * Unknown Pattern
 * Tracks patterns that weren't recognized during analysis
 */
public class UnknownPattern {
    public String pattern;
    public PurposeType suggestedPurpose;
    public long firstSeen;
    public int occurrences;

    public UnknownPattern(String pattern, PurposeType suggestedPurpose) {
        this.pattern = pattern;
        this.suggestedPurpose = suggestedPurpose;
        this.firstSeen = System.currentTimeMillis();
        this.occurrences = 1;
    }

    @Override
    public String toString() {
        return String.format("Pattern: %s | Suggested: %s | Seen: %d times",
            pattern, suggestedPurpose, occurrences);
    }
}
