package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Configuration Editing Tests")
class IntegrationConfigurationEditTest {

    @Test
    void testCompleteEditWorkflow() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();

        AnalysisConfig config = new AnalysisConfig("OriginalProject", "/src", "/reports");
        assertDoesNotThrow(() -> {
            controller.loadConfiguration(config);
            panel.setProjectName(controller.getConfiguration().getProjectName());
            panel.setSourcePath(controller.getConfiguration().getSourcePath());
            panel.setReportPath(controller.getConfiguration().getReportPath());
        });
    }

    @Test
    void testEditAndSave() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel();

        AnalysisConfig config = new AnalysisConfig("Original", "/src", "/reports");
        controller.loadConfiguration(config);

        assertDoesNotThrow(() -> {
            controller.updateProjectName("Modified");
            controller.updateSourcePath("/new/src");
            controller.saveConfiguration("/tmp/edited_config.properties");
        });
    }

    @Test
    void testEditAndValidate() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        ConfigurationValidator validator = new ConfigurationValidator();

        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/reports");
        controller.loadConfiguration(config);
        controller.updateProjectName("NewName");

        assertTrue(validator.validateAll(controller.getConfiguration()));
    }

    @Test
    void testEditAndReset() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("Original", "/src", "/reports");

        controller.loadConfiguration(config);
        controller.updateProjectName("Modified");
        controller.resetConfiguration();

        assertEquals("Original", controller.getConfiguration().getProjectName());
    }

    @Test
    void testMultipleEditsAndSave() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/reports");

        assertDoesNotThrow(() -> {
            controller.loadConfiguration(config);
            controller.updateProjectName("Project1");
            controller.saveConfiguration("/tmp/config1.properties");

            controller.updateProjectName("Project2");
            controller.saveConfiguration("/tmp/config2.properties");

            assertNotNull(controller.getConfiguration());
        });
    }

    @Test
    void testLoadModifySave() {
        ConfigurationEditorController controller = new ConfigurationEditorController();
        ConfigurationPersistence persistence = new ConfigurationPersistence();

        assertDoesNotThrow(() -> {
            AnalysisConfig original = new AnalysisConfig("Original", "/src", "/reports");
            persistence.save(original, "/tmp/test_lms.properties");

            AnalysisConfig loaded = persistence.load("/tmp/test_lms.properties");
            controller.loadConfiguration(loaded);
            controller.updateProjectName("Modified");
            controller.saveConfiguration("/tmp/test_lms_modified.properties");

            AnalysisConfig final_config = persistence.load("/tmp/test_lms_modified.properties");
            assertNotNull(final_config);
        });
    }
}
