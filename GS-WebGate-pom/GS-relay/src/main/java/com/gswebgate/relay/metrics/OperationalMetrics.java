package com.gswebgate.relay.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Operational metrics for the relay system.
 * Tracks work item processing, DLQ operations, and sync performance.
 */
@Component
public class OperationalMetrics {

    // Work Item Counters
    private final Counter workItemsSubmitted;
    private final Counter workItemsCompleted;
    private final Counter workItemsFailed;

    // DLQ Counters
    private final Counter dlqFailuresReceived;
    private final Counter dlqFailuresSynced;
    private final Counter dlqSyncErrors;

    // Timers
    private final Timer workProcessingLatency;
    private final Timer dlqSyncLatency;

    public OperationalMetrics(MeterRegistry meterRegistry) {
        // Work Item Metrics
        this.workItemsSubmitted = Counter.builder("relay.workitems.submitted")
                .description("Total work items submitted to relay")
                .register(meterRegistry);

        this.workItemsCompleted = Counter.builder("relay.workitems.completed")
                .description("Total work items completed successfully")
                .register(meterRegistry);

        this.workItemsFailed = Counter.builder("relay.workitems.failed")
                .description("Total work items that failed processing")
                .register(meterRegistry);

        // DLQ Metrics
        this.dlqFailuresReceived = Counter.builder("relay.dlq.failures.received")
                .description("Total failure records received from searcher DLQ")
                .register(meterRegistry);

        this.dlqFailuresSynced = Counter.builder("relay.dlq.failures.synced")
                .description("Total failure records successfully synced")
                .register(meterRegistry);

        this.dlqSyncErrors = Counter.builder("relay.dlq.sync.errors")
                .description("Total errors during DLQ sync operations")
                .register(meterRegistry);

        // Latency Metrics
        this.workProcessingLatency = Timer.builder("relay.workitems.processing.time")
                .description("Time to process work items (milliseconds)")
                .register(meterRegistry);

        this.dlqSyncLatency = Timer.builder("relay.dlq.sync.time")
                .description("Time to sync failure records to relay (milliseconds)")
                .register(meterRegistry);
    }

    // Work Item Operations

    public void recordWorkItemSubmitted() {
        workItemsSubmitted.increment();
    }

    public void recordWorkItemCompleted() {
        workItemsCompleted.increment();
    }

    public void recordWorkItemFailed() {
        workItemsFailed.increment();
    }

    public Timer.Sample startWorkProcessingTimer() {
        return Timer.start();
    }

    public void recordWorkProcessingTime(Timer.Sample sample) {
        sample.stop(workProcessingLatency);
    }

    // DLQ Operations

    public void recordDLQFailureReceived() {
        dlqFailuresReceived.increment();
    }

    public void recordDLQFailureSynced() {
        dlqFailuresSynced.increment();
    }

    public void recordDLQSyncError() {
        dlqSyncErrors.increment();
    }

    public Timer.Sample startDLQSyncTimer() {
        return Timer.start();
    }

    public void recordDLQSyncTime(Timer.Sample sample) {
        sample.stop(dlqSyncLatency);
    }

    // Getters for testing

    public double getWorkItemsSubmittedCount() {
        return workItemsSubmitted.count();
    }

    public double getWorkItemsCompletedCount() {
        return workItemsCompleted.count();
    }

    public double getWorkItemsFailedCount() {
        return workItemsFailed.count();
    }

    public double getDLQFailuresReceivedCount() {
        return dlqFailuresReceived.count();
    }

    public double getDLQFailuresSyncedCount() {
        return dlqFailuresSynced.count();
    }

    public double getDLQSyncErrorsCount() {
        return dlqSyncErrors.count();
    }
}
