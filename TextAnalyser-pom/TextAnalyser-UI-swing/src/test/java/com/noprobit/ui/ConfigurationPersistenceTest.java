package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Configuration Persistence Tests")
class ConfigurationPersistenceTest {

    @Test
    void testSaveConfigurationToFile() {
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/reports");
        assertDoesNotThrow(() -> {
            persistence.save(config, "/tmp/test_save.properties");
        });
    }

    @Test
    void testLoadConfigurationFromFile() {
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/reports");
        assertDoesNotThrow(() -> {
            persistence.save(config, "/tmp/test_load.properties");
            AnalysisConfig loaded = persistence.load("/tmp/test_load.properties");
            assertNotNull(loaded);
        });
    }

    @Test
    void testSavePreservesProjectName() {
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        AnalysisConfig config = new AnalysisConfig("MyProject", "/src", "/reports");
        assertDoesNotThrow(() -> {
            persistence.save(config, "/tmp/test_project_name.properties");
            AnalysisConfig loaded = persistence.load("/tmp/test_project_name.properties");
            assertEquals("MyProject", loaded.getProjectName());
        });
    }

    @Test
    void testSavePreservesSourcePath() {
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        AnalysisConfig config = new AnalysisConfig("Test", "/home/user/src", "/reports");
        assertDoesNotThrow(() -> {
            persistence.save(config, "/tmp/test_source_path.properties");
            AnalysisConfig loaded = persistence.load("/tmp/test_source_path.properties");
            assertEquals("/home/user/src", loaded.getSourcePath());
        });
    }

    @Test
    void testSavePreservesReportPath() {
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/home/user/reports");
        assertDoesNotThrow(() -> {
            persistence.save(config, "/tmp/test_report_path.properties");
            AnalysisConfig loaded = persistence.load("/tmp/test_report_path.properties");
            assertEquals("/home/user/reports", loaded.getReportPath());
        });
    }

    @Test
    void testLoadNonExistentFile() {
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        assertDoesNotThrow(() -> {
            AnalysisConfig loaded = persistence.load("/tmp/nonexistent_12345.properties");
        });
    }

    @Test
    void testDeleteConfiguration() {
        ConfigurationPersistence persistence = new ConfigurationPersistence();
        AnalysisConfig config = new AnalysisConfig("Test", "/src", "/reports");
        assertDoesNotThrow(() -> {
            persistence.save(config, "/tmp/test_delete.properties");
            persistence.delete("/tmp/test_delete.properties");
        });
    }
}
