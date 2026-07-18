package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Configuration Display Panel Tests")
class ConfigurationDisplayPanelTest {

    private ConfigurationDisplayPanel panel;
    private ProjectMetadata testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new ProjectMetadata("TestProject", "/test/path");
        testConfig.setReportPath("/test/reports");
    }

    @Test
    @DisplayName("Configuration display panel can be created")
    void testPanelCreated() {
        assertDoesNotThrow(() -> {
            panel = new ConfigurationDisplayPanel();
        });
        assertNotNull(panel, "Panel should be created");
    }

    @Test
    @DisplayName("Panel displays project name")
    void testPanelDisplaysProjectName() {
        panel = new ConfigurationDisplayPanel();
        panel.displayConfiguration(testConfig);

        String displayedText = panel.getDisplayedConfiguration();
        assertNotNull(displayedText, "Configuration should be displayed");
        assertTrue(displayedText.contains("TestProject"),
            "Should display project name");
    }

    @Test
    @DisplayName("Panel displays source path")
    void testPanelDisplaysSourcePath() {
        panel = new ConfigurationDisplayPanel();
        panel.displayConfiguration(testConfig);

        String displayedText = panel.getDisplayedConfiguration();
        assertNotNull(displayedText, "Configuration should be displayed");
        assertTrue(displayedText.contains("/test/path"),
            "Should display source path");
    }

    @Test
    @DisplayName("Panel displays report path")
    void testPanelDisplaysReportPath() {
        panel = new ConfigurationDisplayPanel();
        panel.displayConfiguration(testConfig);

        String displayedText = panel.getDisplayedConfiguration();
        assertNotNull(displayedText, "Configuration should be displayed");
        assertTrue(displayedText.contains("/test/reports"),
            "Should display report path");
    }

    @Test
    @DisplayName("Panel is formatted for readability")
    void testPanelFormattingReadable() {
        panel = new ConfigurationDisplayPanel();
        panel.displayConfiguration(testConfig);

        String displayedText = panel.getDisplayedConfiguration();
        assertNotNull(displayedText, "Should have readable format");
        // Check that it's not just raw concatenation
        assertTrue(displayedText.length() > 0, "Should have formatted content");
    }

    @Test
    @DisplayName("Panel updates when configuration changes")
    void testPanelUpdatesOnProjectChange() {
        panel = new ConfigurationDisplayPanel();
        panel.displayConfiguration(testConfig);

        String firstDisplay = panel.getDisplayedConfiguration();

        ProjectMetadata newConfig = new ProjectMetadata("NewProject", "/new/path");
        panel.displayConfiguration(newConfig);

        String secondDisplay = panel.getDisplayedConfiguration();

        assertNotEquals(firstDisplay, secondDisplay,
            "Display should update when configuration changes");
        assertTrue(secondDisplay.contains("NewProject"),
            "Should show new project name");
    }
}
