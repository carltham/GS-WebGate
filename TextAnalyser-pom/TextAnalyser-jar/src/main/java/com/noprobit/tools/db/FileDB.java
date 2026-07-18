package com.noprobit.tools.db;

import com.noprobit.tools.analyzers.ClassAnalysisEngine;
import com.noprobit.tools.linting.JavaClassLinter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileDB {

    private static final String DB_EXTENSION = ".analysis.txt";
    private final Path dbDirectory;
    private final Map<String, AnalysisRecord> cache = new HashMap<>();

    public FileDB(String dbDirectoryPath) {
        this.dbDirectory = Paths.get(dbDirectoryPath);
        ensureDirectoryExists();
    }

    public FileDB() {
        this(".analysis-db");
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(dbDirectory);
        } catch (IOException e) {
            System.err.println("Failed to create database directory: " + e.getMessage());
        }
    }

    public void storeAnalysisResult(ClassAnalysisEngine.AnalysisResult result) throws IOException {
        if (result == null) {
            return;
        }

        AnalysisRecord record = new AnalysisRecord(result);
        String key = result.fullName;
        cache.put(key, record);

        saveRecordToFile(record);
    }

    public void storeAnalysisResults(List<ClassAnalysisEngine.AnalysisResult> results) throws IOException {
        for (ClassAnalysisEngine.AnalysisResult result : results) {
            storeAnalysisResult(result);
        }
    }

    public AnalysisRecord getAnalysisResult(String fullClassName) throws IOException {
        if (cache.containsKey(fullClassName)) {
            return cache.get(fullClassName);
        }

        AnalysisRecord record = loadRecordFromFile(fullClassName);
        if (record != null) {
            cache.put(fullClassName, record);
        }
        return record;
    }

    public List<AnalysisRecord> getAllAnalysisResults() throws IOException {
        List<AnalysisRecord> results = new ArrayList<>();

        File[] files = dbDirectory.toFile().listFiles((dir, name) -> name.endsWith(DB_EXTENSION));
        if (files != null) {
            for (File file : files) {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                AnalysisRecord record = parseRecord(lines);
                if (record != null) {
                    results.add(record);
                    cache.put(record.fullName, record);
                }
            }
        }

        return results;
    }

    public boolean exists(String fullClassName) {
        if (cache.containsKey(fullClassName)) {
            return true;
        }

        Path filePath = getFilePath(fullClassName);
        return Files.exists(filePath);
    }

    public void clearCache() {
        cache.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }

    private void saveRecordToFile(AnalysisRecord record) throws IOException {
        Path filePath = getFilePath(record.fullName);
        List<String> lines = new ArrayList<>();

        lines.add("Full Name: " + record.fullName);
        lines.add("Current Name: " + record.currentName);
        lines.add("Suggested Name: " + record.suggestedName);
        lines.add("Extends Class: " + (record.extendsClass != null ? record.extendsClass : "Unknown"));
        lines.add("Purpose: " + record.purpose);
        lines.add("Validation: " + record.validationResult);
        lines.add("Suggestion: " + record.suggestionResult);
        lines.add("Lint Issues: " + (record.lintIssuesSummary != null ? record.lintIssuesSummary : ""));
        lines.add("Has Lint Errors: " + record.hasLintErrors);
        lines.add("Timestamp: " + System.currentTimeMillis());

        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    private AnalysisRecord loadRecordFromFile(String fullClassName) throws IOException {
        Path filePath = getFilePath(fullClassName);

        if (!Files.exists(filePath)) {
            return null;
        }

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        return parseRecord(lines);
    }

    private AnalysisRecord parseRecord(List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }

        AnalysisRecord record = new AnalysisRecord();

        for (String line : lines) {
            if (line.startsWith("Full Name: ")) {
                record.fullName = line.substring("Full Name: ".length());
            } else if (line.startsWith("Current Name: ")) {
                record.currentName = line.substring("Current Name: ".length());
            } else if (line.startsWith("Suggested Name: ")) {
                record.suggestedName = line.substring("Suggested Name: ".length());
            } else if (line.startsWith("Extends Class: ")) {
                String ext = line.substring("Extends Class: ".length());
                record.extendsClass = "Unknown".equals(ext) ? null : ext;
            } else if (line.startsWith("Purpose: ")) {
                record.purpose = line.substring("Purpose: ".length());
            } else if (line.startsWith("Validation: ")) {
                record.validationResult = line.substring("Validation: ".length());
            } else if (line.startsWith("Suggestion: ")) {
                record.suggestionResult = line.substring("Suggestion: ".length());
            } else if (line.startsWith("Lint Issues: ")) {
                record.lintIssuesSummary = line.substring("Lint Issues: ".length());
            } else if (line.startsWith("Has Lint Errors: ")) {
                record.hasLintErrors = Boolean.parseBoolean(line.substring("Has Lint Errors: ".length()));
            } else if (line.startsWith("Timestamp: ")) {
                record.timestamp = Long.parseLong(line.substring("Timestamp: ".length()));
            }
        }

        return record;
    }

    private Path getFilePath(String fullClassName) {
        String filename = fullClassName.replace(".", "_") + DB_EXTENSION;
        return dbDirectory.resolve(filename);
    }

    public static class AnalysisRecord {
        public String fullName;
        public String currentName;
        public String suggestedName;
        public String extendsClass;
        public String purpose;
        public String validationResult;
        public String suggestionResult;
        public String lintIssuesSummary;
        public boolean hasLintErrors;
        public long timestamp;

        public AnalysisRecord() {
            this.timestamp = System.currentTimeMillis();
            this.lintIssuesSummary = "";
            this.hasLintErrors = false;
        }

        public AnalysisRecord(ClassAnalysisEngine.AnalysisResult result) {
            this.fullName = result.fullName;
            this.currentName = result.currentName;
            this.suggestedName = result.suggestedName;
            this.extendsClass = result.extendsClass;
            this.purpose = result.purpose;
            this.validationResult = result.validationResult != null ? result.validationResult.toString() : "";
            this.suggestionResult = result.suggestionResult != null ? result.suggestionResult.toString() : "";
            this.timestamp = System.currentTimeMillis();
            this.hasLintErrors = result.hasErrors;
            this.lintIssuesSummary = serializeLintIssues(result.lintIssues);
        }

        @Override
        public String toString() {
            return String.format(
                    "AnalysisRecord{%s -> %s (extends %s)}",
                    currentName, suggestedName, extendsClass != null ? extendsClass : "Unknown");
        }

        private static String serializeLintIssues(List<JavaClassLinter.LintIssue> issues) {
            if (issues == null || issues.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (JavaClassLinter.LintIssue issue : issues) {
                sb.append(issue.severity).append("|").append(issue.ruleId).append("|").append(issue.message).append(";");
            }
            return sb.toString();
        }

        public List<JavaClassLinter.LintIssue> getLintIssues() {
            List<JavaClassLinter.LintIssue> issues = new ArrayList<>();
            if (lintIssuesSummary == null || lintIssuesSummary.isEmpty()) {
                return issues;
            }
            for (String issueStr : lintIssuesSummary.split(";")) {
                if (!issueStr.isEmpty()) {
                    String[] parts = issueStr.split("\\|");
                    if (parts.length == 3) {
                        JavaClassLinter.Severity severity = JavaClassLinter.Severity.valueOf(parts[0]);
                        issues.add(new JavaClassLinter.LintIssue(parts[1], severity, parts[2]));
                    }
                }
            }
            return issues;
        }
    }

    public List<AnalysisRecord> getViolations() throws IOException {
        return getAllAnalysisResults().stream()
                .filter(record -> !record.currentName.equals(record.suggestedName))
                .collect(Collectors.toList());
    }

    public int getViolationCount() throws IOException {
        return (int) getViolations().size();
    }

    public void exportToCSV(String csvFilePath) throws IOException {
        List<AnalysisRecord> records = getAllAnalysisResults();
        StringBuilder sb = new StringBuilder();
        sb.append("Full Name,Current Name,Suggested Name,Extends Class,Purpose,Validation,Lint Errors,Lint Warnings,Lint Issues,Timestamp\n");

        for (AnalysisRecord record : records) {
            List<JavaClassLinter.LintIssue> issues = record.getLintIssues();
            long errorCount = issues.stream().filter(i -> i.severity == JavaClassLinter.Severity.ERROR).count();
            long warningCount = issues.stream().filter(i -> i.severity == JavaClassLinter.Severity.WARNING).count();
            String issueDetails = formatLintSummary(issues);

            String line = String.format("%s,%s,%s,%s,%s,%s,%d,%d,%s,%d",
                    escapeCSV(record.fullName),
                    escapeCSV(record.currentName),
                    escapeCSV(record.suggestedName),
                    escapeCSV(record.extendsClass != null ? record.extendsClass : ""),
                    escapeCSV(record.purpose),
                    escapeCSV(record.validationResult),
                    errorCount,
                    warningCount,
                    escapeCSV(issueDetails),
                    record.timestamp);
            sb.append(line).append("\n");
        }

        try (FileOutputStream fos = new FileOutputStream(csvFilePath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public void exportToMarkdown(String markdownFilePath, String projectName, String sourceNodePath) throws IOException {
        exportToMarkdownInternal(markdownFilePath, projectName, sourceNodePath);
    }

    public void exportToMarkdown(String markdownFilePath) throws IOException {
        exportToMarkdownInternal(markdownFilePath, "Project", "src/main/java");
    }

    private void exportToMarkdownInternal(String markdownFilePath, String projectName, String sourceNodePath) throws IOException {
        List<AnalysisRecord> records = getAllAnalysisResults();
        List<AnalysisRecord> violations = getViolations();
        List<AnalysisRecord> compliant = records.stream()
                .filter(record -> record.currentName.equals(record.suggestedName))
                .collect(Collectors.toList());
        List<String> lines = new ArrayList<>();

        lines.add("# Class Analysis Report");
        lines.add("");
        lines.add("## Executive Summary");
        lines.add("");
        lines.add("This report documents the results of a comprehensive **class naming convention analysis**.");
        lines.add("The analysis validates that all Java classes follow the **PascalCase naming convention**");
        lines.add("(starts with an uppercase letter, e.g., `MyClassName`, `ClassAnalysisEngine`).");
        lines.add("");

        lines.add("### Project & Analysis Scope");
        lines.add("");
        lines.add("| Property | Value |");
        lines.add("|----------|-------|");
        lines.add("| **Project** | " + escapeMarkdown(projectName) + " |");
        lines.add("| **Source Node** | `" + escapeMarkdown(sourceNodePath) + "` |");
        lines.add("| **Analysis Type** | Class Naming Convention (PascalCase) |");
        lines.add("");

        lines.add("### Key Metrics");
        lines.add("");
        lines.add("| Metric | Value |");
        lines.add("|--------|-------|");
        lines.add("| Total Classes Analyzed | " + records.size() + " |");
        lines.add("| Compliant Classes | " + compliant.size() + " |");
        lines.add("| Naming Violations | " + violations.size() + " |");
        lines.add("| Compliance Rate | " + String.format("%.1f%%",
            (records.isEmpty() ? 100 : compliant.size() * 100.0 / records.size())) + " |");
        lines.add("| Analysis Timestamp | " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " |");
        lines.add("");

        lines.add("## Analysis Details");
        lines.add("");
        lines.add("### What Was Analyzed");
        lines.add("");
        lines.add("- **Scope**: Java source files across the project");
        lines.add("- **Convention Checked**: PascalCase naming (Class names must start with uppercase)");
        lines.add("- **Files Scanned**: Java class definitions");
        lines.add("- **Parent Classes**: Inheritance information captured for context");
        lines.add("- **Purpose Analysis**: Class purpose inference to guide naming suggestions");
        lines.add("");

        lines.add("### Naming Convention Rules");
        lines.add("");
        lines.add("A valid class name must:");
        lines.add("1. Start with an **uppercase letter**");
        lines.add("2. Use **CamelCase** formatting for multi-word names");
        lines.add("3. Contain **no underscores** or lowercase starting character");
        lines.add("");
        lines.add("**Valid Examples**: `MyClass`, `ClassAnalysisEngine`, `FileDB`, `EncodingSwitcher`");
        lines.add("");
        lines.add("**Invalid Examples**: `myClass`, `my_class`, `_MyClass`, `MYCLASS`");
        lines.add("");

        if (!violations.isEmpty()) {
            lines.add("## 🔴 Naming Violations & Linting Issues");
            lines.add("");
            lines.add("The following classes have naming convention violations or linting issues:");
            lines.add("");
            lines.add("| Current Name | Suggested Name | Full Classname | Extends | Linting Issues |");
            lines.add("|--------------|---|---|---|---|");
            for (AnalysisRecord record : violations) {
                String lintSummary = formatLintSummary(record.getLintIssues());
                String line = String.format("| `%s` | `%s` | %s | %s | %s |",
                        escapeMarkdown(record.currentName),
                        escapeMarkdown(record.suggestedName),
                        escapeMarkdown(record.fullName),
                        escapeMarkdown(record.extendsClass != null ? record.extendsClass : "Object"),
                        escapeMarkdown(lintSummary));
                lines.add(line);
            }
            lines.add("");

            List<AnalysisRecord> lintErrorRecords = violations.stream()
                    .filter(r -> r.hasLintErrors)
                    .collect(Collectors.toList());

            if (!lintErrorRecords.isEmpty()) {
                lines.add("### Linting Errors (Critical)");
                lines.add("");
                for (AnalysisRecord record : lintErrorRecords) {
                    List<JavaClassLinter.LintIssue> errors = record.getLintIssues().stream()
                            .filter(i -> i.severity == JavaClassLinter.Severity.ERROR)
                            .collect(Collectors.toList());
                    if (!errors.isEmpty()) {
                        lines.add("**" + record.currentName + "**");
                        for (JavaClassLinter.LintIssue issue : errors) {
                            lines.add("- ❌ [" + issue.ruleId + "] " + issue.message);
                        }
                        lines.add("");
                    }
                }
            }

            List<AnalysisRecord> lintWarningRecords = violations.stream()
                    .filter(r -> !r.getLintIssues().stream().allMatch(i -> i.severity == JavaClassLinter.Severity.ERROR))
                    .collect(Collectors.toList());

            if (!lintWarningRecords.isEmpty()) {
                lines.add("### Linting Warnings (Recommendations)");
                lines.add("");
                for (AnalysisRecord record : lintWarningRecords) {
                    List<JavaClassLinter.LintIssue> warnings = record.getLintIssues().stream()
                            .filter(i -> i.severity == JavaClassLinter.Severity.WARNING)
                            .collect(Collectors.toList());
                    if (!warnings.isEmpty()) {
                        lines.add("**" + record.currentName + "**");
                        for (JavaClassLinter.LintIssue issue : warnings) {
                            lines.add("- ⚠️ [" + issue.ruleId + "] " + issue.message);
                        }
                        lines.add("");
                    }
                }
            }

            lines.add("### Remediation");
            lines.add("");
            lines.add("To fix violations:");
            lines.add("1. **Resolve Linting Errors** — These are critical and prevent valid Java compilation");
            lines.add("2. **Address Warnings** — Follow recommended naming patterns");
            lines.add("3. **Rename Classes** — Update class names, filenames, and all references");
            lines.add("");
        }

        if (!compliant.isEmpty()) {
            lines.add("## ✅ Compliant Classes");
            lines.add("");
            lines.add("The following " + compliant.size() + " classes follow the PascalCase naming convention:");
            lines.add("");
            lines.add("| Class Name | Full Classname | Extends |");
            lines.add("|------------|---|---|");
            for (AnalysisRecord record : compliant) {
                String line = String.format("| `%s` | %s | %s |",
                        escapeMarkdown(record.currentName),
                        escapeMarkdown(record.fullName),
                        escapeMarkdown(record.extendsClass != null ? record.extendsClass : "Object"));
                lines.add(line);
            }
            lines.add("");
        } else {
            if (records.isEmpty()) {
                lines.add("## Status");
                lines.add("");
                lines.add("No classes were analyzed in this run. Please ensure the source directories are accessible.");
                lines.add("");
            }
        }

        lines.add("## Conclusion");
        lines.add("");
        if (violations.isEmpty() && !records.isEmpty()) {
            lines.add("✅ **All classes comply with the naming convention!**");
            lines.add("");
            lines.add("The project maintains excellent code quality standards with 100% compliance to PascalCase");
            lines.add("naming conventions for all analyzed classes.");
        } else if (!records.isEmpty()) {
            lines.add("⚠️ **" + violations.size() + " naming violation(s) detected.**");
            lines.add("");
            lines.add("Please review the violations section above and update class names accordingly.");
        }
        lines.add("");
        lines.add("---");
        lines.add("*Generated by TextAnalyser Class Analysis Engine*");
        lines.add("");

        Files.write(Paths.get(markdownFilePath), lines, StandardCharsets.UTF_8);
    }

    private String escapeMarkdown(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|");
    }

    private String formatLintSummary(List<JavaClassLinter.LintIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return "None";
        }
        long errorCount = issues.stream().filter(i -> i.severity == JavaClassLinter.Severity.ERROR).count();
        long warningCount = issues.stream().filter(i -> i.severity == JavaClassLinter.Severity.WARNING).count();

        StringBuilder sb = new StringBuilder();
        if (errorCount > 0) {
            sb.append(errorCount).append(" error");
            if (errorCount > 1) sb.append("s");
        }
        if (warningCount > 0) {
            if (errorCount > 0) sb.append(", ");
            sb.append(warningCount).append(" warning");
            if (warningCount > 1) sb.append("s");
        }
        return sb.toString();
    }
}
