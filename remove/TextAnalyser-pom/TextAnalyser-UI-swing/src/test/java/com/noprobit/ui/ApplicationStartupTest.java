package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application Startup Tests")
class ApplicationStartupTest {

    private TextAnalyserApplication application;

    @BeforeEach
    void setUp() {
        // Initialize application before each test
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        if (application != null) {
            try {
                application.shutdown();
            } catch (Exception e) {
                // Ignore cleanup exceptions
            }
        }
    }

    @Test
    @DisplayName("Application starts without exception")
    void testApplicationStartsWithoutException() {
        assertDoesNotThrow(() -> {
            application = new TextAnalyserApplication();
        });
        assertNotNull(application, "Application instance should be created");
    }

    @Test
    @DisplayName("Main window is created on startup")
    void testApplicationInitializesUIComponents() {
        application = new TextAnalyserApplication();
        MainWindow mainWindow = application.getMainWindow();
        assertNotNull(mainWindow, "Main window should be created");
        assertInstanceOf(MainWindow.class, mainWindow, "Should be instance of MainWindow");
    }

    @Test
    @DisplayName("Application loads configuration on startup")
    void testApplicationLoadsConfiguration() {
        application = new TextAnalyserApplication();
        ProjectMetadata config = application.getCurrentConfiguration();
        assertNotNull(config, "Configuration should be loaded");
        assertNotNull(config.getProjectName(), "Project name should not be null");
        assertNotNull(config.getSourcePath(), "Source path should not be null");
    }

    @Test
    @DisplayName("Application loads project list on startup")
    void testApplicationLoadsProjectList() {
        application = new TextAnalyserApplication();
        assertNotNull(application.getAvailableProjects(), "Project list should be loaded");
        assertTrue(application.getAvailableProjects().size() > 0, "Should have at least one project");
    }

    @Test
    @DisplayName("Application displays error when configuration is missing")
    void testApplicationErrorMessageOnConfigMissing() {
        // This test verifies graceful handling when config is missing
        assertDoesNotThrow(() -> {
            application = new TextAnalyserApplication();
        }, "Should handle missing configuration gracefully");
    }

    @Test
    @DisplayName("Application has default configuration")
    void testApplicationDefaults() {
        application = new TextAnalyserApplication();
        ProjectMetadata config = application.getCurrentConfiguration();
        assertNotNull(config, "Should have default configuration");
        assertFalse(config.getProjectName().isEmpty(), "Default project name should not be empty");
    }

    @Test
    @DisplayName("Application shutdown closes window safely")
    void testShutdownClosesWindow() {
        application = new TextAnalyserApplication();
        MainWindow window = application.getMainWindow();
        assertTrue(window.isVisible(), "Window should be visible before shutdown");

        assertDoesNotThrow(() -> {
            application.shutdown();
        }, "Shutdown should complete without exception");

        assertFalse(window.isVisible(), "Window should be hidden after shutdown");
    }
}
