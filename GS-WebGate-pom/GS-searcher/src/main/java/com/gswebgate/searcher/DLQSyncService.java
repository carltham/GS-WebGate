package com.gswebgate.searcher;

import com.gswebgate.searcher.contract.DLQSyncRequest;
import com.gswebgate.searcher.db.FailureRecord;
import com.gswebgate.searcher.db.FailureRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * Syncs failures from local searcher DLQ to relay's FailedWork table.
 * Runs on a configurable schedule (default: every 5 minutes).
 * 
 * Purpose:
 * - Local DLQ: Resilience (searcher tracks own failures independently)
 * - Relay FailedWork: System observability (centralized failure tracking)
 */
@Service
public class DLQSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DLQSyncService.class);

    @Autowired(required = false)
    private FailureRecordRepository repository;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Value("${searcher.dlq-sync-enabled:true}")
    private boolean syncEnabled;

    @Value("${searcher.dlq-sync-batch-size:10}")
    private int batchSize;

    @Value("${relay.base.url:http://localhost:8080}")
    private String relayBaseUrl;

    /**
     * Sync local DLQ to relay (runs every 5 minutes).
     * Only syncs unsync'd records and stops if relay is unreachable.
     */
    @Scheduled(fixedDelayString = "${searcher.dlq-sync-interval-ms:300000}")
    public void syncToRelay() {
        if (!syncEnabled || repository == null || restTemplate == null) {
            return;
        }

        try {
            logger.debug("DLQ Sync: Starting sync cycle");
            List<FailureRecord> toSync = repository.findRetryable(Integer.MAX_VALUE);
            
            if (toSync.isEmpty()) {
                logger.debug("DLQ Sync: No items to sync");
                return;
            }

            logger.info("DLQ Sync: Found {} items to sync", toSync.size());
            
            int synced = 0;
            for (FailureRecord record : toSync) {
                try {
                    syncSingleRecord(record);
                    synced++;
                    if (synced >= batchSize) {
                        logger.info("DLQ Sync: Synced {} items in this batch", synced);
                        break;
                    }
                } catch (Exception e) {
                    logger.warn("DLQ Sync: Failed to sync record {}: {}", 
                               record.getMessageId(), e.getMessage());
                }
            }

            logger.info("DLQ Sync: Completed sync of {} items", synced);

        } catch (Exception e) {
            logger.error("DLQ Sync: Sync cycle failed: {}", e.getMessage());
        }
    }

    /**
     * Sync a single failure record to relay.
     */
    private void syncSingleRecord(FailureRecord record) throws Exception {
        String syncUrl = relayBaseUrl + "/messages/dlq/failures";
        
        DLQSyncRequest request = new DLQSyncRequest(
            record.getMessageId(),
            record.getQuestion(),
            record.getContext(),
            record.getTarget(),
            record.getMode(),
            record.getErrorMessage(),
            record.getFailureReason().name(),
            record.getAttemptCount()
        );

        restTemplate.postForObject(syncUrl, request, Void.class);
        logger.debug("DLQ Sync: Synced record {}", record.getMessageId());
    }

    /**
     * Get sync statistics.
     */
    public SyncStats getSyncStats() {
        if (repository == null) {
            return new SyncStats(0, 0, false);
        }

        long total = repository.count();
        long retryable = repository.findRetryable(Integer.MAX_VALUE).size();
        return new SyncStats(total, retryable, syncEnabled);
    }

    /**
     * Sync statistics DTO.
     */
    public static class SyncStats {
        public long totalFailures;
        public long retryableItems;
        public boolean syncEnabled;

        public SyncStats(long totalFailures, long retryableItems, boolean syncEnabled) {
            this.totalFailures = totalFailures;
            this.retryableItems = retryableItems;
            this.syncEnabled = syncEnabled;
        }
    }
}
