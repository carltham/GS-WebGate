package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Configuration Validation Tests")
class ConfigurationValidationTest {

    @Test
    void testValidProjectName() {
        ConfigurationValidator validator = new ConfigurationValidator();
        assertTrue(validator.validateProjectName("MyProject"));
    }

    @Test
    void testInvalidProjectName() {
        ConfigurationValidator validator = new ConfigurationValidator();
        assertFalse(validator.validateProjectName(""));
    }

    @Test
    void testValidSourcePath() {
        ConfigurationValidator validator = new ConfigurationValidator();
        assertTrue(validator.validatePath("/src/main/java"));
    }

    @Test
    void testInvalidSourcePath() {
        ConfigurationValidator validator = new ConfigurationValidator();
        assertFalse(validator.validatePath(""));
    }

    @Test
    void testValidReportPath() {
        ConfigurationValidator validator = new ConfigurationValidator();
        assertTrue(validator.validatePath("/reports"));
    }

    @Test
    void testValidateCompleteConfiguration() {
        ConfigurationValidator validator = new ConfigurationValidator();
        AnalysisConfig config = new AnalysisConfig("Valid", "/src", "/reports");
        assertTrue(validator.validateAll(config));
    }

    @Test
    void testValidateInvalidConfiguration() {
        ConfigurationValidator validator = new ConfigurationValidator();
        AnalysisConfig config = new AnalysisConfig("", "", "");
        assertFalse(validator.validateAll(config));
    }

    @Test
    void testValidatePartiallyInvalidConfiguration() {
        ConfigurationValidator validator = new ConfigurationValidator();
        AnalysisConfig config = new AnalysisConfig("Valid", "", "/reports");
        assertFalse(validator.validateAll(config));
    }
}
