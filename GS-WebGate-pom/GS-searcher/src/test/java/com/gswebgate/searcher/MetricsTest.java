package com.gswebgate.searcher;

import com.gswebgate.searcher.metrics.OperationalMetrics;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Phase 7: Searcher Metrics Tests")
class MetricsTest {

    @Autowired
    private OperationalMetrics metrics;

    // Work Processing Metrics

    @Test
    @DisplayName("metrics_records_work_item_polled")
    void testMetricsRecordsWorkItemPolled() {
        // Act
        metrics.recordWorkItemPolled();

        // Assert
        assertEquals(1.0, metrics.getWorkItemsPolledCount());
    }

    @Test
    @DisplayName("metrics_records_work_item_executed")
    void testMetricsRecordsWorkItemExecuted() {
        // Act
        metrics.recordWorkItemExecuted();

        // Assert
        assertEquals(1.0, metrics.getWorkItemsExecutedCount());
    }

    @Test
    @DisplayName("metrics_records_work_item_succeeded")
    void testMetricsRecordsWorkItemSucceeded() {
        // Act
        metrics.recordWorkItemSucceeded();

        // Assert
        assertEquals(1.0, metrics.getWorkItemsSucceededCount());
    }

    @Test
    @DisplayName("metrics_records_work_item_failed")
    void testMetricsRecordsWorkItemFailed() {
        // Act
        metrics.recordWorkItemFailed();

        // Assert
        assertEquals(1.0, metrics.getWorkItemsFailedCount());
    }

    @Test
    @DisplayName("metrics_records_work_item_submitted")
    void testMetricsRecordsWorkItemSubmitted() {
        // Act
        metrics.recordWorkItemSubmitted();

        // Assert
        assertEquals(1.0, metrics.getWorkItemsSubmittedCount());
    }

    @Test
    @DisplayName("metrics_records_multiple_work_items")
    void testMetricsRecordsMultipleWorkItems() {
        // Act
        for (int i = 0; i < 5; i++) {
            metrics.recordWorkItemPolled();
            metrics.recordWorkItemExecuted();
            if (i % 2 == 0) {
                metrics.recordWorkItemSucceeded();
            } else {
                metrics.recordWorkItemFailed();
            }
            metrics.recordWorkItemSubmitted();
        }

        // Assert
        assertEquals(5.0, metrics.getWorkItemsPolledCount());
        assertEquals(5.0, metrics.getWorkItemsExecutedCount());
        assertEquals(3.0, metrics.getWorkItemsSucceededCount());
        assertEquals(2.0, metrics.getWorkItemsFailedCount());
        assertEquals(5.0, metrics.getWorkItemsSubmittedCount());
    }

    // DLQ & Failure Metrics

    @Test
    @DisplayName("metrics_records_dlq_failure_captured")
    void testMetricsRecordsDLQFailureCaptured() {
        // Act
        metrics.recordDLQFailureCaptured();

        // Assert
        assertEquals(1.0, metrics.getDLQFailuresCapturedCount());
    }

    @Test
    @DisplayName("metrics_records_dlq_failure_retried")
    void testMetricsRecordsDLQFailureRetried() {
        // Act
        metrics.recordDLQFailureRetried();

        // Assert
        assertEquals(1.0, metrics.getDLQFailuresRetriedCount());
    }

    @Test
    @DisplayName("metrics_records_dlq_sync_attempt")
    void testMetricsRecordsDLQSyncAttempt() {
        // Act
        metrics.recordDLQSyncAttempt();

        // Assert
        assertEquals(1.0, metrics.getDLQSyncAttemptsCount());
    }

    @Test
    @DisplayName("metrics_records_dlq_sync_error")
    void testMetricsRecordsDLQSyncError() {
        // Act
        metrics.recordDLQSyncError();

        // Assert
        assertEquals(1.0, metrics.getDLQSyncErrorsCount());
    }

    @Test
    @DisplayName("metrics_failure_retry_cycle")
    void testMetricsFailureRetryCycle() {
        // Arrange - Simulate a failure being captured and retried
        // Act
        metrics.recordDLQFailureCaptured();
        metrics.recordDLQFailureCaptured();
        metrics.recordDLQFailureCaptured();

        // Retry one
        metrics.recordDLQFailureRetried();

        // Assert
        assertEquals(3.0, metrics.getDLQFailuresCapturedCount());
        assertEquals(1.0, metrics.getDLQFailuresRetriedCount());
    }

    @Test
    @DisplayName("metrics_dlq_sync_cycle")
    void testMetricsDLQSyncCycle() {
        // Arrange - Simulate multiple sync attempts with some errors
        // Act
        metrics.recordDLQSyncAttempt();
        metrics.recordDLQSyncAttempt();
        metrics.recordDLQSyncAttempt();

        metrics.recordDLQSyncError();

        // Assert
        assertEquals(3.0, metrics.getDLQSyncAttemptsCount());
        assertEquals(1.0, metrics.getDLQSyncErrorsCount());
    }

    // Timer Tests

    @Test
    @DisplayName("metrics_search_execution_timer")
    void testMetricsSearchExecutionTimer() {
        // Act
        Timer.Sample sample1 = metrics.startSearchExecutionTimer();
        sleepMillis(10);
        metrics.recordSearchExecutionTime(sample1);

        Timer.Sample sample2 = metrics.startSearchExecutionTimer();
        sleepMillis(20);
        metrics.recordSearchExecutionTime(sample2);

        // Assert - Verify timer recorded both samples
        // (Detailed timer assertions would need access to the Timer object itself)
    }

    @Test
    @DisplayName("metrics_result_submission_timer")
    void testMetricsResultSubmissionTimer() {
        // Act
        Timer.Sample sample = metrics.startResultSubmissionTimer();
        sleepMillis(5);
        metrics.recordResultSubmissionTime(sample);

        // Assert - Timer should have recorded the time
    }

    // Helper

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
