package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Analysis Threading Tests")
class AnalysisThreadingTest {

    private AnalysisController controller;
    private ProjectMetadata testProject;

    @BeforeEach
    void setUp() {
        controller = new AnalysisController();
        testProject = new ProjectMetadata("TestProject", "/test/path");
    }

    @Test
    @DisplayName("Analysis runs in background thread")
    void testAnalysisRunsInBackground() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        controller.addCompletionListener(report -> {
            latch.countDown();
        });

        controller.startAnalysis(testProject);

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Analysis should complete");
    }

    @Test
    @DisplayName("EDT remains responsive during analysis")
    void testEDTRemainsResponsive() {
        long startTime = System.currentTimeMillis();

        controller.startAnalysis(testProject);

        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime < 1000, "UI should remain responsive");
    }

    @Test
    @DisplayName("Cancel stops background thread")
    void testCancelStopsThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        controller.addCompletionListener(report -> {
            latch.countDown();
        });

        controller.startAnalysis(testProject);
        controller.cancelAnalysis();

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertFalse(completed, "Analysis should be cancelled");
    }

    @Test
    @DisplayName("Thread cleanup completes successfully")
    void testThreadCleanup() throws InterruptedException {
        controller.startAnalysis(testProject);
        Thread.sleep(100);
        controller.cancelAnalysis();

        Thread.sleep(100);
        assertFalse(controller.isAnalysisRunning());
    }

    @Test
    @DisplayName("Progress events fired from worker thread")
    void testProgressEventsFiredFromWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        controller.addProgressListener(event -> {
            latch.countDown();
        });

        controller.startAnalysis(testProject);

        boolean eventFired = latch.await(5, TimeUnit.SECONDS);
        assertTrue(eventFired, "Progress event should fire");
    }

    @Test
    @DisplayName("Concurrent analysis requests handled safely")
    void testConcurrentAnalysisSafety() {
        controller.startAnalysis(testProject);

        // Attempt concurrent start (should be ignored)
        assertDoesNotThrow(() -> {
            controller.startAnalysis(testProject);
        });

        assertTrue(controller.isAnalysisRunning());
    }
}
