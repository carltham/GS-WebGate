package com.noprobit.analyzers.ui;

public class AnalysisConfig {
    private String projectName;
    private String sourcePath;
    private String reportPath;

    public AnalysisConfig(String projectName, String sourcePath, String reportPath) {
        this.projectName = projectName;
        this.sourcePath = sourcePath;
        this.reportPath = reportPath;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    @Override
    public String toString() {
        return "AnalysisConfig{" +
                "projectName='" + projectName + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", reportPath='" + reportPath + '\'' +
                '}';
    }
}
