package com.noprobit.tools.analyzers.model;

/**
 * Analysis Result
 * Represents the result of class/function purpose analysis
 */
public class AnalysisResult {
    public String actualName;
    public String suggestedName;
    public String purpose;
    public String extendsClass;

    public AnalysisResult(String actualName, String suggestedName, String purpose, String extendsClass) {
        this.actualName = actualName;
        this.suggestedName = suggestedName;
        this.purpose = purpose;
        this.extendsClass = extendsClass;
    }

    @Override
    public String toString() {
        return String.format("Analysis{actual='%s', suggested='%s', purpose='%s', extends='%s'}",
            actualName, suggestedName, purpose, extendsClass != null ? extendsClass : "N/A");
    }
}
