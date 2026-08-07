package com.gswebgate.searcher;

import com.gswebgate.searcher.db.FailureRecord;
import com.gswebgate.searcher.db.FailureRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Dead-letter queue service using file-based H2 database.
 * Captures and manages failed work items for monitoring and retry logic.
 * 
 * Persistence: Failures are stored in the searcher's local H2 database,
 * allowing them to survive searcher restarts and be queried for retry/investigation.
 */
@Service
public class DeadLetterQueueService {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueService.class);

    @Autowired(required = false)
    private FailureRecordRepository repository;

    @Value("${searcher.dlq-enabled:true}")
    private boolean dlqEnabled;

    @Value("${searcher.dlq-max-attempts:3}")
    private int maxAttempts;

    /**
     * Capture a failed work item to the DLQ.
     */
    public void captureFailure(String messageId, String question, String context,
                               String target, String mode, String errorMessage,
                               FailureRecord.FailureReason reason) {
        if (!dlqEnabled || repository == null) {
            logger.debug("DLQ disabled or repository unavailable, skipping capture");
            return;
        }

        try {
            FailureRecord record = repository.findByMessageId(messageId)
                .orElseGet(() -> new FailureRecord(messageId, question, context, target, mode));

            record.setErrorMessage(errorMessage);
            record.setFailureReason(reason);
            record.setAttemptCount(record.getAttemptCount() + 1);
            record.setLastFailureTime(LocalDateTime.now());

            repository.save(record);
            logger.info("DLQ: Captured failure - {}", record);

        } catch (Exception e) {
            logger.error("DLQ: Failed to capture failure for messageId={}: {}", 
                        messageId, e.getMessage());
        }
    }

    /**
     * Check if a failed work item should be retried.
     */
    public boolean isRetryable(String messageId) {
        if (!dlqEnabled || repository == null) {
            return false;
        }

        try {
            return repository.findByMessageId(messageId)
                .map(fw -> fw.getAttemptCount() < maxAttempts)
                .orElse(false);

        } catch (Exception e) {
            logger.error("DLQ: Error checking retry eligibility for messageId={}: {}", 
                        messageId, e.getMessage());
            return false;
        }
    }

    /**
     * Get all failed items ready for retry.
     */
    public List<FailureRecord> getRetryableItems() {
        if (!dlqEnabled || repository == null) {
            return Collections.emptyList();
        }

        try {
            var items = repository.findRetryable(maxAttempts);
            logger.info("DLQ: Found {} items ready for retry (maxAttempts={})", items.size(), maxAttempts);
            return items;

        } catch (Exception e) {
            logger.error("DLQ: Error retrieving retryable items: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get all failed items (for monitoring/debugging).
     */
    public List<FailureRecord> getAllFailedItems() {
        if (!dlqEnabled || repository == null) {
            return Collections.emptyList();
        }

        try {
            return repository.findAll();
        } catch (Exception e) {
            logger.error("DLQ: Error retrieving all failed items: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get a specific failed item record.
     */
    public Optional<FailureRecord> getFailureRecord(String messageId) {
        if (!dlqEnabled || repository == null) {
            return Optional.empty();
        }

        try {
            return repository.findByMessageId(messageId);
        } catch (Exception e) {
            logger.error("DLQ: Error retrieving record for messageId={}: {}", messageId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get failure statistics.
     */
    public Map<FailureRecord.FailureReason, Long> getFailureStats() {
        if (!dlqEnabled || repository == null) {
            return Collections.emptyMap();
        }

        try {
            Map<FailureRecord.FailureReason, Long> stats = new HashMap<>();
            for (FailureRecord.FailureReason reason : FailureRecord.FailureReason.values()) {
                stats.put(reason, repository.countByFailureReason(reason));
            }
            return stats;
        } catch (Exception e) {
            logger.error("DLQ: Error retrieving failure stats: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Clear all DLQ records (useful for testing).
     */
    public void clear() {
        if (repository != null) {
            repository.deleteAll();
            logger.debug("DLQ cleared");
        }
    }

    /**
     * Get current DLQ size.
     */
    public long size() {
        if (repository == null) {
            return 0;
        }
        return repository.count();
    }
}
