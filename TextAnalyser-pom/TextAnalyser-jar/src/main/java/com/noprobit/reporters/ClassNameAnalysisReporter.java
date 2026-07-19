package com.noprobit.analyzers.reporters;

import com.noprobit.analyzers.analyzers.ClassAnalysisEngine;
import java.util.List;

public class ClassNameAnalysisReporter {

    private static final String INDENT = "     ";
    private static final String SEPARATOR = "========================================";

    public void printReport(List<ClassAnalysisEngine.AnalysisResult> violations) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("CLASS NAMING VALIDATION & ANALYSIS");
        System.out.println(SEPARATOR);
        System.out.println("Convention: PascalCase (starts with uppercase)");
        System.out.println("Total violations found: " + violations.size());
        System.out.println();

        if (!violations.isEmpty()) {
            System.out.println("DETAILED ANALYSIS WITH SUGGESTIONS:");
            System.out.println();
            violations.forEach(this::printViolation);
        } else {
            System.out.println("✓ All classes follow PascalCase naming convention!");
        }

        System.out.println(SEPARATOR);
        System.out.println();
    }

    private void printViolation(ClassAnalysisEngine.AnalysisResult result) {
        System.out.println("❌ VIOLATION FOUND");
        System.out.println("   Full Name: " + result.fullName);
        System.out.println("   Extends: " + (result.extendsClass != null ? result.extendsClass : "Unknown"));
        System.out.println();

        System.out.println("   Validation Result:");
        System.out.println(INDENT + result.validationResult);
        System.out.println();

        System.out.println("   Purpose Analysis:");
        System.out.println(INDENT + result.purpose);
        System.out.println();

        System.out.println("   Suggestion Result:");
        System.out.println(INDENT + result.suggestionResult);
        System.out.println();
    }

    public String getViolationSummary(int violationCount) {
        if (violationCount == 0) {
            return "All classes follow PascalCase naming convention";
        }
        return violationCount + " classes violate PascalCase naming convention";
    }
}
