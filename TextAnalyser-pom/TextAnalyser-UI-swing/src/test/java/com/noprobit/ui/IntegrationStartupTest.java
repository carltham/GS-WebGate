package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Startup Tests")
class IntegrationStartupTest {

    private TextAnalyserApplication application;

    @BeforeEach
    void setUp() {
        // Initialize before each test
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
    @DisplayName("Complete startup sequence works end-to-end")
    void testCompleteStartupSequence() {
        assertDoesNotThrow(() -> {
            // Step 1: Create application
            application = new TextAnalyserApplication();
            assertNotNull(application, "Application should be created");

            // Step 2: Verify window is visible
            MainWindow window = application.getMainWindow();
            assertNotNull(window, "Main window should exist");
            assertTrue(window.isVisible(), "Window should be visible");

            // Step 3: Verify projects are loaded
            List<ProjectMetadata> projects = application.getAvailableProjects();
            assertNotNull(projects, "Projects should be loaded");
            assertTrue(projects.size() > 0, "Should have at least one project");
        }, "Entire startup sequence should complete without exception");
    }

    @Test
    @DisplayName("Current configuration matches available projects")
    void testUIAppearanceCorrect() {
        application = new TextAnalyserApplication();
        ProjectMetadata currentConfig = application.getCurrentConfiguration();
        List<ProjectMetadata> availableProjects = application.getAvailableProjects();

        assertNotNull(currentConfig, "Current configuration should exist");
        assertNotNull(availableProjects, "Available projects should exist");

        // Verify current project exists in available projects
        boolean found = availableProjects.stream()
            .anyMatch(p -> p.getProjectName().equals(currentConfig.getProjectName()));

        assertTrue(found || availableProjects.isEmpty(),
            "Current project should be in available projects");
    }

    @Test
    @DisplayName("No exceptions occur during normal startup")
    void testNoExceptionsOnStartup() {
        Exception caughtException = null;

        try {
            application = new TextAnalyserApplication();
            MainWindow window = application.getMainWindow();
            assertNotNull(window, "Window should be created");

            ProjectMetadata config = application.getCurrentConfiguration();
            assertNotNull(config, "Configuration should be loaded");

            List<ProjectMetadata> projects = application.getAvailableProjects();
            assertNotNull(projects, "Projects should be loaded");
        } catch (Exception e) {
            caughtException = e;
        }

        assertNull(caughtException, "Startup should not throw exceptions");
    }

    @Test
    @DisplayName("Project list is properly populated")
    void testProjectListPopulated() {
        application = new TextAnalyserApplication();
        List<ProjectMetadata> projects = application.getAvailableProjects();

        assertNotNull(projects, "Project list should not be null");
        assertTrue(projects.size() > 0, "Should have at least one project");

        // Verify each project has required fields
        for (ProjectMetadata project : projects) {
            assertNotNull(project.getProjectName(),
                "Each project should have a name");
            assertFalse(project.getProjectName().isEmpty(),
                "Project name should not be empty");
        }
    }

    @Test
    @DisplayName("Configuration is displayed")
    void testConfigurationDisplayed() {
        application = new TextAnalyserApplication();
        MainWindow window = application.getMainWindow();

        assertNotNull(window, "Main window should exist");
        assertTrue(window.isVisible(), "Window should be visible");

        // Verify configuration panel exists and is displayed
        ProjectMetadata config = application.getCurrentConfiguration();
        assertNotNull(config, "Configuration should be loaded and available");
        assertNotNull(config.getProjectName(), "Project name should be set");
    }
}
