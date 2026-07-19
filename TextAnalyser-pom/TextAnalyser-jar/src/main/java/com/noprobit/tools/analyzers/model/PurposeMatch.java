package com.noprobit.tools.analyzers.model;

/**
 * Purpose Match
 * Represents a matched purpose with confidence score
 */
public class PurposeMatch {
    private final PurposeType purpose;
    private final double confidence;
    private final String sourceEngine;

    public PurposeMatch(PurposeType purpose, double confidence, String sourceEngine) {
        this.purpose = purpose;
        this.confidence = confidence;
        this.sourceEngine = sourceEngine;
    }

    public PurposeType getPurpose() {
        return purpose;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getSourceEngine() {
        return sourceEngine;
    }

    @Override
    public String toString() {
        return String.format("[%s] (Conf: %.0f%% via %s)", purpose, confidence * 100, sourceEngine);
    }
}
