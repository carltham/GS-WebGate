package com.noprobit.analyzers.ui;

import java.util.logging.Logger;

public class ConfigurationValidator {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationValidator.class.getName());

    public boolean validateProjectName(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            LOGGER.fine("Project name validation failed: empty");
            return false;
        }
        LOGGER.fine("Project name validation passed: " + projectName);
        return true;
    }

    public boolean validatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            LOGGER.fine("Path validation failed: empty");
            return false;
        }
        LOGGER.fine("Path validation passed: " + path);
        return true;
    }

    public boolean validateAll(AnalysisConfig config) {
        if (config == null) {
            LOGGER.warning("Configuration validation failed: null config");
            return false;
        }

        boolean projectValid = validateProjectName(config.getProjectName());
        boolean sourceValid = validatePath(config.getSourcePath());
        boolean reportValid = validatePath(config.getReportPath());

        boolean result = projectValid && sourceValid && reportValid;
        if (result) {
            LOGGER.fine("Full configuration validation passed");
        } else {
            LOGGER.warning("Full configuration validation failed");
        }
        return result;
    }
}
