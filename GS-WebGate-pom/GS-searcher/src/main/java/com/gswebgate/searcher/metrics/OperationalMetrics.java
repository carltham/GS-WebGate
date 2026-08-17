package com.gswebgate.searcher.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Operational metrics for the searcher system.
 * Tracks work processing, failures, and DLQ operations.
 */
@Component
public class OperationalMetrics {

    // Work Processing Counters
    private final Counter workItemsPolled;
    private final Counter workItemsExecuted;
    private final Counter workItemsSucceeded;
    private final Counter workItemsFailed;
    private final Counter workItemsSubmitted;

    // Failure & Retry Counters
    private final Counter dlqFailuresCaptured;
    private final Counter dlqFailuresRetried;
    private final Counter dlqSyncAttempts;
    private final Counter dlqSyncErrors;

    // Timers
    private final Timer searchExecutionTime;
    private final Timer resultSubmissionTime;

    public OperationalMetrics(MeterRegistry meterRegistry) {
        // Work Processing Metrics
        this.workItemsPolled = Counter.builder("searcher.workitems.polled")
                .description("Total work items polled from relay")
                .register(meterRegistry);

        this.workItemsExecuted = Counter.builder("searcher.workitems.executed")
                .description("Total work items attempted execution")
                .register(meterRegistry);

        this.workItemsSucceeded = Counter.builder("searcher.workitems.succeeded")
                .description("Total work items completed successfully")
                .register(meterRegistry);

        this.workItemsFailed = Counter.builder("searcher.workitems.failed")
                .description("Total work items that failed execution")
                .register(meterRegistry);

        this.workItemsSubmitted = Counter.builder("searcher.workitems.submitted")
                .description("Total results submitted to relay")
                .register(meterRegistry);

        // DLQ & Failure Metrics
        this.dlqFailuresCaptured = Counter.builder("searcher.dlq.failures.captured")
                .description("Total failures captured to local DLQ")
                .register(meterRegistry);

        this.dlqFailuresRetried = Counter.builder("searcher.dlq.failures.retried")
                .description("Total failures retried after local capture")
                .register(meterRegistry);

        this.dlqSyncAttempts = Counter.builder("searcher.dlq.sync.attempts")
                .description("Total sync attempts to relay")
                .register(meterRegistry);

        this.dlqSyncErrors = Counter.builder("searcher.dlq.sync.errors")
                .description("Total errors during sync to relay")
                .register(meterRegistry);

        // Latency Metrics
        this.searchExecutionTime = Timer.builder("searcher.search.execution.time")
                .description("Time to execute search (milliseconds)")
                .register(meterRegistry);

        this.resultSubmissionTime = Timer.builder("searcher.result.submission.time")
                .description("Time to submit result to relay (milliseconds)")
                .register(meterRegistry);
    }

    // Work Processing Operations

    public void recordWorkItemPolled() {
        workItemsPolled.increment();
    }

    public void recordWorkItemExecuted() {
        workItemsExecuted.increment();
    }

    public void recordWorkItemSucceeded() {
        workItemsSucceeded.increment();
    }

    public void recordWorkItemFailed() {
        workItemsFailed.increment();
    }

    public void recordWorkItemSubmitted() {
        workItemsSubmitted.increment();
    }

    // DLQ & Failure Operations

    public void recordDLQFailureCaptured() {
        dlqFailuresCaptured.increment();
    }

    public void recordDLQFailureRetried() {
        dlqFailuresRetried.increment();
    }

    public void recordDLQSyncAttempt() {
        dlqSyncAttempts.increment();
    }

    public void recordDLQSyncError() {
        dlqSyncErrors.increment();
    }

    // Timer Operations

    public Timer.Sample startSearchExecutionTimer() {
        return Timer.start();
    }

    public void recordSearchExecutionTime(Timer.Sample sample) {
        sample.stop(searchExecutionTime);
    }

    public Timer.Sample startResultSubmissionTimer() {
        return Timer.start();
    }

    public void recordResultSubmissionTime(Timer.Sample sample) {
        sample.stop(resultSubmissionTime);
    }

    // Getters for testing

    public double getWorkItemsPolledCount() {
        return workItemsPolled.count();
    }

    public double getWorkItemsExecutedCount() {
        return workItemsExecuted.count();
    }

    public double getWorkItemsSucceededCount() {
        return workItemsSucceeded.count();
    }

    public double getWorkItemsFailedCount() {
        return workItemsFailed.count();
    }

    public double getWorkItemsSubmittedCount() {
        return workItemsSubmitted.count();
    }

    public double getDLQFailuresCapturedCount() {
        return dlqFailuresCaptured.count();
    }

    public double getDLQFailuresRetriedCount() {
        return dlqFailuresRetried.count();
    }

    public double getDLQSyncAttemptsCount() {
        return dlqSyncAttempts.count();
    }

    public double getDLQSyncErrorsCount() {
        return dlqSyncErrors.count();
    }
}
