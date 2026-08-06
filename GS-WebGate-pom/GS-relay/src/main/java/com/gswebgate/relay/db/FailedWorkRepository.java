package com.gswebgate.relay.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing failed work items (dead-letter queue).
 */
@Repository
public interface FailedWorkRepository extends JpaRepository<FailedWork, String> {
    
    /**
     * Find all failed work items with attempt count less than max retries.
     */
    @Query("SELECT f FROM FailedWork f WHERE f.attemptCount < :maxRetries ORDER BY f.lastFailureTime ASC")
    List<FailedWork> findRetryable(int maxRetries);
    
    /**
     * Find all failed work items for a specific failure reason.
     */
    List<FailedWork> findByFailureReason(FailedWork.FailureReason reason);
    
    /**
     * Check if a message ID exists in failed work queue.
     */
    Optional<FailedWork> findByMessageId(String messageId);
}
