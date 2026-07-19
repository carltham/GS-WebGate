package com.noprobit.analyzers.ui;

public class AnalysisReport {
    private String projectName;
    private int totalFiles;
    private int violationsFound;
    private long analysisTime;

    public AnalysisReport(String projectName, int totalFiles, int violationsFound, long analysisTime) {
        this.projectName = projectName;
        this.totalFiles = totalFiles;
        this.violationsFound = violationsFound;
        this.analysisTime = analysisTime;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public int getViolationsFound() {
        return violationsFound;
    }

    public long getAnalysisTime() {
        return analysisTime;
    }

    @Override
    public String toString() {
        return "AnalysisReport{" +
                "project='" + projectName + '\'' +
                ", files=" + totalFiles +
                ", violations=" + violationsFound +
                ", time=" + analysisTime + "ms" +
                '}';
    }
}
