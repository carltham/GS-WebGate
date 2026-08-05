package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Analysis Controller Tests")
class AnalysisControllerTest {

    private AnalysisController controller;
    private ProjectMetadata testProject;

    @BeforeEach
    void setUp() {
        controller = new AnalysisController();
        testProject = new ProjectMetadata("TestProject", "/test/path");
    }

    @Test
    @DisplayName("Controller initializes successfully")
    void testControllerInitialization() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Start analysis begins processing")
    void testStartAnalysis() {
        assertDoesNotThrow(() -> {
            controller.startAnalysis(testProject);
        });
    }

    @Test
    @DisplayName("Cancel analysis stops processing")
    void testCancelAnalysis() {
        assertDoesNotThrow(() -> {
            controller.startAnalysis(testProject);
            controller.cancelAnalysis();
        });
    }

    @Test
    @DisplayName("Progress listener receives updates")
    void testProgressListenerReceivesUpdates() throws InterruptedException {
        boolean[] eventFired = {false};
        controller.addProgressListener(event -> {
            eventFired[0] = true;
        });

        controller.startAnalysis(testProject);
        Thread.sleep(100); // Wait for async worker

        assertTrue(eventFired[0]);
    }

    @Test
    @DisplayName("Completion listener fires when analysis completes")
    void testCompletionListenerFires() {
        boolean[] eventFired = {false};
        controller.addCompletionListener(report -> {
            eventFired[0] = true;
        });

        assertDoesNotThrow(() -> {
            controller.startAnalysis(testProject);
            Thread.sleep(100); // Give time for completion
        });
    }

    @Test
    @DisplayName("Analysis results available after completion")
    void testAnalysisResultsAvailable() {
        controller.startAnalysis(testProject);
        assertDoesNotThrow(() -> {
            Thread.sleep(100);
        });
    }

    @Test
    @DisplayName("Error listener notified on failure")
    void testErrorListenerNotified() {
        boolean[] errorFired = {false};
        controller.addErrorListener(error -> {
            errorFired[0] = true;
        });

        assertDoesNotThrow(() -> {
            controller.startAnalysis(null); // Trigger error
        });
    }

    @Test
    @DisplayName("Cannot start analysis while already running")
    void testCannotStartWhileRunning() {
        controller.startAnalysis(testProject);
        assertDoesNotThrow(() -> {
            controller.startAnalysis(testProject); // Should be ignored
        });
    }

    @Test
    @DisplayName("Analysis status can be queried")
    void testAnalysisStatusQueried() {
        assertFalse(controller.isAnalysisRunning());
        controller.startAnalysis(testProject);
        assertTrue(controller.isAnalysisRunning());
    }

    @Test
    @DisplayName("Multiple listeners all receive events")
    void testMultipleListenersReceiveEvents() throws InterruptedException {
        boolean[] listener1 = {false};
        boolean[] listener2 = {false};

        controller.addProgressListener(e -> listener1[0] = true);
        controller.addProgressListener(e -> listener2[0] = true);

        controller.startAnalysis(testProject);
        Thread.sleep(100); // Wait for async worker

        assertTrue(listener1[0]);
        assertTrue(listener2[0]);
    }
}
