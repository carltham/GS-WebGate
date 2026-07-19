package com.noprobit.analyzers.ui;

public class ProjectMetadata {
    private String projectName;
    private String sourcePath;
    private String reportPath;

    public ProjectMetadata(String projectName, String sourcePath) {
        this.projectName = projectName;
        this.sourcePath = sourcePath;
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
        return "ProjectMetadata{" +
                "projectName='" + projectName + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", reportPath='" + reportPath + '\'' +
                '}';
    }
}
