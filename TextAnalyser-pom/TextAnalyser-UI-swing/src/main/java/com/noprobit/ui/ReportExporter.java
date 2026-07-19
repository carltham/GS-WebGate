package com.noprobit.analyzers.ui;

public class ReportExporter {

    public String toCSV(AnalysisReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Project,Files,Violations,Time\n");
        csv.append(report.getProjectName()).append(",")
            .append(report.getTotalFiles()).append(",")
            .append(report.getViolationsFound()).append(",")
            .append(report.getAnalysisTime()).append("\n");
        return csv.toString();
    }

    public String toMarkdown(AnalysisReport report) {
        StringBuilder md = new StringBuilder();
        md.append("# Analysis Report\n\n");
        md.append("| Property | Value |\n");
        md.append("|---|---|\n");
        md.append("| Project | ").append(report.getProjectName()).append(" |\n");
        md.append("| Files | ").append(report.getTotalFiles()).append(" |\n");
        md.append("| Violations | ").append(report.getViolationsFound()).append(" |\n");
        md.append("| Time | ").append(report.getAnalysisTime()).append(" |\n");
        return md.toString();
    }
}
