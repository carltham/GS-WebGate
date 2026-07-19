package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Configuration Editor Panel Tests")
class ConfigurationEditorPanelTest {

    @Test
    void testPanelInitialization() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertNotNull(panel);
    }

    @Test
    void testProjectNameFieldExists() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertNotNull(panel.getProjectNameField());
    }

    @Test
    void testSourcePathFieldExists() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertNotNull(panel.getSourcePathField());
    }

    @Test
    void testReportPathFieldExists() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertNotNull(panel.getReportPathField());
    }

    @Test
    void testSetProjectName() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertDoesNotThrow(() -> {
            panel.setProjectName("TestProject");
            assertEquals("TestProject", panel.getProjectName());
        });
    }

    @Test
    void testSetSourcePath() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertDoesNotThrow(() -> {
            panel.setSourcePath("/src/main");
            assertEquals("/src/main", panel.getSourcePath());
        });
    }

    @Test
    void testSetReportPath() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertDoesNotThrow(() -> {
            panel.setReportPath("/reports");
            assertEquals("/reports", panel.getReportPath());
        });
    }

    @Test
    void testSaveButtonExists() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertNotNull(panel.getSaveButton());
    }

    @Test
    void testResetButtonExists() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        assertNotNull(panel.getResetButton());
    }

    @Test
    void testGetAllFieldValues() {
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();
        panel.setProjectName("Test");
        panel.setSourcePath("/src");
        panel.setReportPath("/reports");

        assertNotNull(panel.getProjectName());
        assertNotNull(panel.getSourcePath());
        assertNotNull(panel.getReportPath());
    }
}
