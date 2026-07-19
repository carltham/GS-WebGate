package com.noprobit.analyzers.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ConfigurationPersistence {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationPersistence.class.getName());

    public void save(AnalysisConfig config, String path) throws IOException {
        try {
            StringBuilder content = new StringBuilder();
            content.append("projectName=").append(config.getProjectName()).append("\n");
            content.append("sourcePath=").append(config.getSourcePath()).append("\n");
            content.append("reportPath=").append(config.getReportPath()).append("\n");

            Files.write(Paths.get(path), content.toString().getBytes());
            LOGGER.info("Configuration saved to: " + path);
        } catch (IOException e) {
            LOGGER.warning("Failed to save configuration: " + e.getMessage());
            throw e;
        }
    }

    public AnalysisConfig load(String path) throws IOException {
        try {
            if (!Files.exists(Paths.get(path))) {
                LOGGER.warning("Configuration file not found: " + path);
                return null;
            }

            String content = new String(Files.readAllBytes(Paths.get(path)));
            String projectName = extractValue(content, "projectName");
            String sourcePath = extractValue(content, "sourcePath");
            String reportPath = extractValue(content, "reportPath");

            AnalysisConfig config = new AnalysisConfig(projectName, sourcePath, reportPath);
            LOGGER.info("Configuration loaded from: " + path);
            return config;
        } catch (IOException e) {
            LOGGER.warning("Failed to load configuration: " + e.getMessage());
            throw e;
        }
    }

    public void delete(String path) throws IOException {
        try {
            Files.deleteIfExists(Paths.get(path));
            LOGGER.info("Configuration deleted: " + path);
        } catch (IOException e) {
            LOGGER.warning("Failed to delete configuration: " + e.getMessage());
            throw e;
        }
    }

    private String extractValue(String content, String key) {
        for (String line : content.split("\n")) {
            if (line.startsWith(key + "=")) {
                return line.substring(key.length() + 1);
            }
        }
        return "";
    }
}
