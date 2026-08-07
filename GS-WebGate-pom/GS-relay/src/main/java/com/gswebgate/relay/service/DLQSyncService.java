package com.gswebgate.relay.service;

import com.gswebgate.relay.contract.DLQSyncRequest;
import com.gswebgate.relay.db.FailedWork;
import com.gswebgate.relay.db.FailedWorkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for handling dead-letter queue sync requests from searcher.
 * Receives failed work items from searcher's local DLQ and persists them
 * in relay's centralized failure tracking system.
 */
@Service
public class DLQSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(DLQSyncService.class);
    
    private final FailedWorkRepository failedWorkRepository;
    
    public DLQSyncService(FailedWorkRepository failedWorkRepository) {
        this.failedWorkRepository = failedWorkRepository;
    }
    
    /**
     * Sync a failed work item from searcher's DLQ.
     * Creates new record if doesn't exist, updates existing record with latest attempt count.
     *
     * @param request The DLQ sync request from searcher
     */
    public void syncFailureFromSearcher(DLQSyncRequest request) {
        try {
            Optional<FailedWork> existing = failedWorkRepository.findByMessageId(request.getMessageId());
            
            if (existing.isPresent()) {
                // Update existing record
                FailedWork failedWork = existing.get();
                failedWork.setAttemptCount(request.getAttemptCount());
                failedWork.setLastFailureTime(LocalDateTime.now());
                failedWorkRepository.save(failedWork);
                logger.debug("Updated failed work record for messageId: {}, attemptCount: {}", 
                    request.getMessageId(), request.getAttemptCount());
            } else {
                // Create new record
                FailedWork.FailureReason reason = parseFailureReason(request.getFailureReason());
                FailedWork failedWork = new FailedWork(
                    request.getMessageId(),
                    request.getQuestion(),
                    request.getContext(),
                    request.getTarget(),
                    request.getMode(),
                    request.getErrorMessage(),
                    reason
                );
                failedWork.setAttemptCount(request.getAttemptCount());
                failedWorkRepository.save(failedWork);
                logger.debug("Created new failed work record for messageId: {}, reason: {}", 
                    request.getMessageId(), reason);
            }
        } catch (Exception e) {
            logger.error("Failed to sync failure record from searcher for messageId: {}", 
                request.getMessageId(), e);
            throw new RuntimeException("DLQ sync failed for messageId: " + request.getMessageId(), e);
        }
    }
    
    /**
     * Parse string failure reason to enum.
     */
    private FailedWork.FailureReason parseFailureReason(String reason) {
        try {
            return FailedWork.FailureReason.valueOf(reason);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown failure reason: {}, defaulting to UNKNOWN_ERROR", reason);
            return FailedWork.FailureReason.UNKNOWN_ERROR;
        }
    }
}
