package com.noprobit.tools.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AnalysisConfig {

    private static final String DEFAULT_PROJECT_NAME = "TextAnalyser";
    private static final String DEFAULT_SOURCE_NODE = "src/main/java";
    private static final String CONFIG_FILE_NAME = "analysis.properties";

    private String projectName;
    private String sourceNodePath;

    public AnalysisConfig() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        projectName = DEFAULT_PROJECT_NAME;
        sourceNodePath = DEFAULT_SOURCE_NODE;

        // 1. Try parent directory config folder (for multi-module maven)
        Path parentConfig = Paths.get("../config", CONFIG_FILE_NAME);
        if (loadFromPath(parentConfig)) {
            return;
        }

        // 2. Try project root config folder
        Path projectRootConfig = Paths.get("config", CONFIG_FILE_NAME);
        if (loadFromPath(projectRootConfig)) {
            return;
        }

        // 3. Try /mnt/DATA/WORKSPACE/Textanalyser
        Path workspaceConfig = Paths.get("/mnt/DATA/WORKSPACE/Textanalyser", CONFIG_FILE_NAME);
        if (loadFromPath(workspaceConfig)) {
            return;
        }

        // 4. Try subdirectories in /mnt/DATA/WORKSPACE/Textanalyser
        try {
            Path workspaceDir = Paths.get("/mnt/DATA/WORKSPACE/Textanalyser");
            if (Files.exists(workspaceDir) && Files.isDirectory(workspaceDir)) {
                Files.list(workspaceDir)
                        .filter(Files::isDirectory)
                        .map(dir -> dir.resolve(CONFIG_FILE_NAME))
                        .filter(Files::exists)
                        .findFirst()
                        .ifPresent(this::loadFromPath);
            }
        } catch (IOException e) {
            // Continue with defaults
        }
    }

    private boolean loadFromPath(Path configPath) {
        try {
            if (Files.exists(configPath)) {
                Properties props = new Properties();
                props.load(Files.newInputStream(configPath));

                String project = props.getProperty("project.name");
                String sourceNode = props.getProperty("source.node.path");

                if (project != null && !project.trim().isEmpty()) {
                    projectName = project.trim();
                }
                if (sourceNode != null && !sourceNode.trim().isEmpty()) {
                    sourceNodePath = sourceNode.trim();
                }
                return true;
            }
        } catch (IOException e) {
            // Continue with next fallback
        }
        return false;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getSourceNodePath() {
        return sourceNodePath;
    }

    public String getReportPrefix() {
        return projectName.toLowerCase() + "-analysis-report";
    }
}
