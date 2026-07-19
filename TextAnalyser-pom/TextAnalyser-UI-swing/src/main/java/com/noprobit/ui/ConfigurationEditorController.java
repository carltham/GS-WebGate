package com.noprobit.analyzers.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ConfigurationEditorController {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationEditorController.class.getName());

    private AnalysisConfig currentConfig;
    private AnalysisConfig originalConfig;

    public ConfigurationEditorController() {
        LOGGER.info("ConfigurationEditorController initialized");
    }

    public void loadConfiguration(AnalysisConfig config) {
        this.currentConfig = new AnalysisConfig(
            config.getProjectName(),
            config.getSourcePath(),
            config.getReportPath()
        );
        this.originalConfig = new AnalysisConfig(
            config.getProjectName(),
            config.getSourcePath(),
            config.getReportPath()
        );
        LOGGER.info("Configuration loaded: " + config.getProjectName());
    }

    public AnalysisConfig getConfiguration() {
        return currentConfig;
    }

    public void updateProjectName(String projectName) {
        if (currentConfig != null) {
            currentConfig.setProjectName(projectName);
            LOGGER.fine("Project name updated: " + projectName);
        }
    }

    public void updateSourcePath(String sourcePath) {
        if (currentConfig != null) {
            currentConfig.setSourcePath(sourcePath);
            LOGGER.fine("Source path updated: " + sourcePath);
        }
    }

    public void updateReportPath(String reportPath) {
        if (currentConfig != null) {
            currentConfig.setReportPath(reportPath);
            LOGGER.fine("Report path updated: " + reportPath);
        }
    }

    public boolean validateConfiguration() {
        if (currentConfig == null) {
            return false;
        }
        ConfigurationValidator validator = new ConfigurationValidator();
        return validator.validateAll(currentConfig);
    }

    public void saveConfiguration(String path) throws IOException {
        if (currentConfig == null) {
            LOGGER.warning("No configuration to save");
            return;
        }
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        persistence.save(currentConfig, path);
        LOGGER.info("Configuration saved to: " + path);
    }

    public void resetConfiguration() {
        if (originalConfig != null) {
            this.currentConfig = new AnalysisConfig(
                originalConfig.getProjectName(),
                originalConfig.getSourcePath(),
                originalConfig.getReportPath()
            );
            LOGGER.info("Configuration reset to original state");
        }
    }
}
