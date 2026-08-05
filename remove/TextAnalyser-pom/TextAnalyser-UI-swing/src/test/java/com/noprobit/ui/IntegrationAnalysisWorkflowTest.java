package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Analysis Workflow Tests")
class IntegrationAnalysisWorkflowTest {

    private AnalysisController controller;
    private AnalysisPanel panel;
    private ProjectMetadata testProject;

    @BeforeEach
    void setUp() {
        controller = new AnalysisController();
        panel = new AnalysisPanel();
        testProject = new ProjectMetadata("TestProject", "/test/path");
    }

    @Test
    @DisplayName("Complete analysis workflow")
    void testCompleteWorkflow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        controller.addCompletionListener(report -> {
            latch.countDown();
        });

        controller.startAnalysis(testProject);

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Analysis should complete");
    }

    @Test
    @DisplayName("Real-time progress updates during analysis")
    void testRealTimeProgressUpdates() throws InterruptedException {
        CountDownLatch progressLatch = new CountDownLatch(1);

        controller.addProgressListener(event -> {
            progressLatch.countDown();
        });

        controller.startAnalysis(testProject);

        boolean eventFired = progressLatch.await(5, TimeUnit.SECONDS);
        assertTrue(eventFired, "Progress event should fire");
    }

    @Test
    @DisplayName("Cancel during analysis stops processing")
    void testCancelDuringAnalysis() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        controller.addCompletionListener(report -> {
            latch.countDown();
        });

        controller.startAnalysis(testProject);
        Thread.sleep(50);
        controller.cancelAnalysis();

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertFalse(completed, "Analysis should be cancelled");
    }

    @Test
    @DisplayName("Results available after completion")
    void testResultsAvailableAfterCompletion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AnalysisReport[] report = new AnalysisReport[1];

        controller.addCompletionListener(r -> {
            report[0] = r;
            latch.countDown();
        });

        controller.startAnalysis(testProject);

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed);
        assertNotNull(report[0], "Results should be available");
    }

    @Test
    @DisplayName("State transitions during analysis")
    void testStateTransitions() {
        assertFalse(controller.isAnalysisRunning(), "Initially not running");

        controller.startAnalysis(testProject);
        assertTrue(controller.isAnalysisRunning(), "Running after start");

        controller.cancelAnalysis();
        assertFalse(controller.isAnalysisRunning(), "Not running after cancel");
    }

    @Test
    @DisplayName("Error recovery workflow")
    void testErrorRecoveryWorkflow() throws InterruptedException {
        CountDownLatch errorLatch = new CountDownLatch(1);

        controller.addErrorListener(error -> {
            errorLatch.countDown();
        });

        controller.startAnalysis(null); // Error case

        boolean errorFired = errorLatch.await(5, TimeUnit.SECONDS);
        assertTrue(errorFired, "Error should be reported");

        // Verify can recover
        ProjectMetadata validProject = new ProjectMetadata("Valid", "/valid");
        assertDoesNotThrow(() -> {
            controller.startAnalysis(validProject);
        });
    }
}
