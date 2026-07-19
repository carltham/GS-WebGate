package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Analysis Error Handling Tests")
class AnalysisErrorHandlingTest {

    private AnalysisController controller;
    private AnalysisPanel panel;

    @BeforeEach
    void setUp() {
        controller = new AnalysisController();
        panel = new AnalysisPanel();
    }

    @Test
    @DisplayName("Error message is displayed on failure")
    void testDisplayErrorMessage() {
        String errorMessage = "Test error";
        assertDoesNotThrow(() -> {
            panel.displayError(errorMessage);
        });
    }

    @Test
    @DisplayName("Controller recovers from error")
    void testRecoverFromError() {
        controller.startAnalysis(null); // Trigger error

        // Should be able to start new analysis
        ProjectMetadata validProject = new ProjectMetadata("Valid", "/path");
        assertDoesNotThrow(() -> {
            controller.startAnalysis(validProject);
        });
    }

    @Test
    @DisplayName("Invalid project handled gracefully")
    void testInvalidProjectHandled() {
        assertDoesNotThrow(() -> {
            controller.startAnalysis(null);
        });
    }

    @Test
    @DisplayName("Missing configuration handled gracefully")
    void testMissingConfigHandled() {
        ProjectMetadata projectWithoutConfig = new ProjectMetadata("", "");
        assertDoesNotThrow(() -> {
            controller.startAnalysis(projectWithoutConfig);
        });
    }

    @Test
    @DisplayName("IO exception handled gracefully")
    void testIOExceptionHandled() {
        boolean[] errorFired = {false};

        controller.addErrorListener(error -> {
            errorFired[0] = true;
        });

        controller.startAnalysis(null);

        assertTrue(errorFired[0]);
    }

    @Test
    @DisplayName("Cleanup on error completes")
    void testCleanupOnError() {
        controller.startAnalysis(null);

        assertFalse(controller.isAnalysisRunning(),
            "Analysis should complete even with error");
    }

    @Test
    @DisplayName("Retry analysis after error")
    void testRetryAnalysisAfterError() {
        controller.startAnalysis(null); // Error case

        ProjectMetadata project = new ProjectMetadata("Retry", "/retry/path");
        assertDoesNotThrow(() -> {
            controller.startAnalysis(project);
        });

        assertTrue(controller.isAnalysisRunning());
    }
}
