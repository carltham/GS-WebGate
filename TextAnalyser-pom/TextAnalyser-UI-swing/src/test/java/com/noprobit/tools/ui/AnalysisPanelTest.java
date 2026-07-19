package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Analysis Panel Tests")
class AnalysisPanelTest {

    private AnalysisPanel panel;

    @BeforeEach
    void setUp() {
        panel = new AnalysisPanel();
    }

    @Test
    @DisplayName("Analysis panel can be created")
    void testPanelCreated() {
        assertNotNull(panel);
    }

    @Test
    @DisplayName("Progress bar is visible")
    void testProgressBarVisible() {
        assertNotNull(panel.getProgressBar());
    }

    @Test
    @DisplayName("Status label is visible")
    void testStatusLabelVisible() {
        assertNotNull(panel.getStatusLabel());
    }

    @Test
    @DisplayName("Cancel button is visible")
    void testCancelButtonVisible() {
        assertNotNull(panel.getCancelButton());
    }

    @Test
    @DisplayName("Progress bar updates on progress event")
    void testProgressBarUpdates() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 50, "test.java", 100, 50);
        assertDoesNotThrow(() -> {
            panel.updateProgress(event);
        });
    }

    @Test
    @DisplayName("Status message updates on progress")
    void testStatusMessageUpdates() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 25, "file.java", 100, 25);
        assertDoesNotThrow(() -> {
            panel.updateProgress(event);
        });
        assertTrue(panel.getStatusLabel().getText().contains("file.java"));
    }

    @Test
    @DisplayName("Cancel button disabled when analysis inactive")
    void testCancelButtonDisabledInactive() {
        assertFalse(panel.getCancelButton().isEnabled());
    }

    @Test
    @DisplayName("Cancel button enabled during analysis")
    void testCancelButtonEnabledDuringAnalysis() {
        panel.setAnalysisRunning(true);
        assertTrue(panel.getCancelButton().isEnabled());
    }

    @Test
    @DisplayName("Error message displayed on error")
    void testErrorDisplayed() {
        assertDoesNotThrow(() -> {
            panel.displayError("Test error message");
        });
    }

    @Test
    @DisplayName("Progress reset on new analysis")
    void testProgressResetOnNewAnalysis() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 50, "test.java", 100, 50);
        panel.updateProgress(event);

        panel.resetProgress();
        assertEquals(0, panel.getProgressBar().getValue());
    }
}
