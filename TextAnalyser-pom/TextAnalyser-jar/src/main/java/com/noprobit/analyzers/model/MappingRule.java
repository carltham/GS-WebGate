package com.noprobit.analyzers.analyzers.model;

/**
 * Mapping Rule
 * Represents a pattern-to-purpose mapping rule from configuration
 */
public class MappingRule {
    private final String pattern;
    private final PurposeType purpose;
    private final double confidence;

    public MappingRule(String pattern, PurposeType purpose, double confidence) {
        this.pattern = pattern.toLowerCase();
        this.purpose = purpose;
        this.confidence = confidence;
    }

    public String getPattern() {
        return pattern;
    }

    public PurposeType getPurpose() {
        return purpose;
    }

    public double getConfidence() {
        return confidence;
    }
}
