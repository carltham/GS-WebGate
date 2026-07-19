package com.noprobit.tools.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DashboardController {
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    private String projectName;
    private int totalFilesAnalyzed = 0;
    private int totalViolations = 0;
    private long lastAnalysisTime = System.currentTimeMillis();
    private Map<String, Integer> violationsByType = new HashMap<>();

    public DashboardController() {
        LOGGER.info("DashboardController initialized");
    }

    public void loadProjectStatistics(String projectName) {
        this.projectName = projectName;
        this.totalFilesAnalyzed = 100;
        this.totalViolations = 50;
        this.lastAnalysisTime = System.currentTimeMillis();
        populateViolationsByType();
        LOGGER.info("Project statistics loaded: " + projectName);
    }

    public String getProjectName() {
        return projectName;
    }

    public int getTotalFilesAnalyzed() {
        return totalFilesAnalyzed;
    }

    public int getTotalViolations() {
        return totalViolations;
    }

    public double getAverageViolationsPerFile() {
        if (totalFilesAnalyzed == 0) {
            return 0.0;
        }
        return (double) totalViolations / totalFilesAnalyzed;
    }

    public String getLastAnalysisTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new java.util.Date(lastAnalysisTime));
    }

    public void refreshDashboard() {
        this.lastAnalysisTime = System.currentTimeMillis();
        LOGGER.info("Dashboard refreshed at: " + getLastAnalysisTime());
    }

    public Map<String, Integer> getViolationsByType() {
        return new HashMap<>(violationsByType);
    }

    private void populateViolationsByType() {
        violationsByType.clear();
        violationsByType.put("ClassNaming", 20);
        violationsByType.put("MethodNaming", 15);
        violationsByType.put("Import", 10);
        violationsByType.put("MethodOrder", 5);
    }
}
