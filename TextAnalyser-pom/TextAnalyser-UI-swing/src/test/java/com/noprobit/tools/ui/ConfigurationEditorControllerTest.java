package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Configuration Editor Controller Tests")
class ConfigurationEditorControllerTest {

    @Test
    void testControllerInitialization() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        assertNotNull(controller);
    }

    @Test
    void testLoadConfiguration() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("TestProject", "/src", "/reports");
        assertDoesNotThrow(() -> {
            controller.loadConfiguration(config);
        });
    }

    @Test
    void testGetConfiguration() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("TestProject", "/src", "/reports");
        controller.loadConfiguration(config);
        assertNotNull(controller.getConfiguration());
    }

    @Test
    void testUpdateProjectName() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("OldName", "/src", "/reports");
        controller.loadConfiguration(config);
        controller.updateProjectName("NewName");
        assertEquals("NewName", controller.getConfiguration().getProjectName());
    }

    @Test
    void testUpdateSourcePath() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("Test", "/old/path", "/reports");
        controller.loadConfiguration(config);
        controller.updateSourcePath("/new/path");
        assertEquals("/new/path", controller.getConfiguration().getSourcePath());
    }

    @Test
    void testUpdateReportPath() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/old/reports");
        controller.loadConfiguration(config);
        controller.updateReportPath("/new/reports");
        assertEquals("/new/reports", controller.getConfiguration().getReportPath());
    }

    @Test
    void testValidateConfiguration() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("Valid", "/src", "/reports");
        controller.loadConfiguration(config);
        assertTrue(controller.validateConfiguration());
    }

    @Test
    void testSaveConfiguration() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/reports");
        controller.loadConfiguration(config);
        assertDoesNotThrow(() -> {
            controller.saveConfiguration("/tmp/test_config.properties");
        });
    }

    @Test
    void testResetConfiguration() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig original = new AnalysisConfig("Original", "/src", "/reports");
        controller.loadConfiguration(original);
        controller.updateProjectName("Modified");
        controller.resetConfiguration();
        assertEquals("Original", controller.getConfiguration().getProjectName());
    }
}
