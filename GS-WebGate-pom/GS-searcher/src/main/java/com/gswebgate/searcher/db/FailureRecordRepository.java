package com.gswebgate.searcher.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for FailureRecord persistence.
 */
@Repository
public interface FailureRecordRepository extends JpaRepository<FailureRecord, String> {

    /**
     * Find a failure record by message ID.
     */
    Optional<FailureRecord> findByMessageId(String messageId);

    /**
     * Find all failure records that can be retried (attempts < maxAttempts).
     */
    @Query("SELECT f FROM FailureRecord f WHERE f.attemptCount < :maxAttempts ORDER BY f.lastFailureTime ASC")
    List<FailureRecord> findRetryable(@Param("maxAttempts") int maxAttempts);

    /**
     * Find all failure records with a specific failure reason.
     */
    List<FailureRecord> findByFailureReason(FailureRecord.FailureReason reason);

    /**
     * Count total failures.
     */
    long countByFailureReason(FailureRecord.FailureReason reason);
}
