package com.noprobit.tools.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TextAnalyserApplication {
    private static final Logger LOGGER = Logger.getLogger(TextAnalyserApplication.class.getName());
    private static final String DEFAULT_SOURCE_PATH = "src/main/java";

    private MainWindow mainWindow;
    private ProjectMetadata currentConfiguration;
    private List<ProjectMetadata> availableProjects;

    public TextAnalyserApplication() {
        LOGGER.fine("Initializing TextAnalyserApplication");
        initializeProjects();
        initializeConfiguration();
        initializeMainWindow();
        LOGGER.info("TextAnalyserApplication initialized successfully");
    }

    private void initializeProjects() {
        availableProjects = new ArrayList<>();
        availableProjects.add(new ProjectMetadata("TextAnalyser", DEFAULT_SOURCE_PATH));
        availableProjects.add(new ProjectMetadata("GSPos", DEFAULT_SOURCE_PATH));
        LOGGER.fine("Projects initialized: " + availableProjects.size() + " projects loaded");
    }

    private void initializeConfiguration() {
        currentConfiguration = new ProjectMetadata("TextAnalyser", DEFAULT_SOURCE_PATH);
        currentConfiguration.setReportPath("analysis");
        LOGGER.fine("Default configuration loaded: " + currentConfiguration.getProjectName());
    }

    private void initializeMainWindow() {
        mainWindow = new MainWindow(currentConfiguration, availableProjects);
        mainWindow.setVisible(true);
        LOGGER.fine("Main window created and displayed");
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public ProjectMetadata getCurrentConfiguration() {
        return currentConfiguration;
    }

    public List<ProjectMetadata> getAvailableProjects() {
        return availableProjects;
    }

    public void shutdown() {
        LOGGER.info("Shutting down TextAnalyserApplication");
        if (mainWindow != null) {
            mainWindow.setVisible(false);
            mainWindow.dispose();
            LOGGER.fine("Main window disposed");
        }
    }
}
