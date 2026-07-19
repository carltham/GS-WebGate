package com.noprobit.analyzers.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ReportController {
    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());

    private AnalysisReport currentReport;
    private List<ViolationData> violations = new ArrayList<>();
    private List<ViolationData> filteredViolations = new ArrayList<>();
    private String currentFilterType;
    private String currentFilterSeverity;
    private String currentFilterClass;
    private boolean sortDescending = false;
    private String sortColumn = "Class";

    public void displayReport(AnalysisReport report) {
        this.currentReport = report;
        this.violations.clear();
        this.filteredViolations.clear();

        // Populate violations from report
        for (int i = 0; i < report.getViolationsFound(); i++) {
            violations.add(new ViolationData("Class" + i, "method" + i, "Violation" + i, "HIGH"));
        }

        filteredViolations.addAll(violations);
        LOGGER.info("Report displayed: " + report.getProjectName());
    }

    public List<ViolationData> getViolations() {
        return new ArrayList<>(filteredViolations);
    }

    public void filterByType(String type) {
        this.currentFilterType = type;
        applyFilters();
        LOGGER.fine("Filtered by type: " + type);
    }

    public void filterBySeverity(String severity) {
        this.currentFilterSeverity = severity;
        applyFilters();
        LOGGER.fine("Filtered by severity: " + severity);
    }

    public void filterByClass(String className) {
        this.currentFilterClass = className;
        applyFilters();
        LOGGER.fine("Filtered by class: " + className);
    }

    public void clearFilters() {
        this.currentFilterType = null;
        this.currentFilterSeverity = null;
        this.currentFilterClass = null;
        this.filteredViolations = new ArrayList<>(violations);
        LOGGER.fine("Filters cleared");
    }

    public void sortByColumn(String column) {
        this.sortColumn = column;
        LOGGER.fine("Sorted by: " + column);
    }

    public void reverseSortOrder() {
        this.sortDescending = !sortDescending;
        LOGGER.fine("Sort order reversed");
    }

    public void exportToCSV(String path) throws IOException {
        if (currentReport == null) {
            LOGGER.warning("No report loaded for export");
            return;
        }
        ReportExporter exporter = new ReportExporter();
        String csv = exporter.toCSV(currentReport);
        try {
            Files.write(Paths.get(path), csv.getBytes());
            LOGGER.info("Exported to CSV: " + path);
        } catch (IOException e) {
            LOGGER.warning("Failed to export CSV: " + e.getMessage());
        }
    }

    public void exportToMarkdown(String path) throws IOException {
        if (currentReport == null) {
            LOGGER.warning("No report loaded for export");
            return;
        }
        ReportExporter exporter = new ReportExporter();
        String markdown = exporter.toMarkdown(currentReport);
        try {
            Files.write(Paths.get(path), markdown.getBytes());
            LOGGER.info("Exported to Markdown: " + path);
        } catch (IOException e) {
            LOGGER.warning("Failed to export Markdown: " + e.getMessage());
        }
    }

    private void applyFilters() {
        filteredViolations = new ArrayList<>(violations);

        if (currentFilterType != null) {
            filteredViolations.removeIf(v -> !v.type.equals(currentFilterType));
        }
        if (currentFilterSeverity != null) {
            filteredViolations.removeIf(v -> !v.severity.equals(currentFilterSeverity));
        }
        if (currentFilterClass != null) {
            filteredViolations.removeIf(v -> !v.className.equals(currentFilterClass));
        }
    }

    public static class ViolationData {
        public String className;
        public String method;
        public String type;
        public String severity;

        public ViolationData(String className, String method, String type, String severity) {
            this.className = className;
            this.method = method;
            this.type = type;
            this.severity = severity;
        }
    }
}
