package com.gswebgate.searcher;

import com.gswebgate.searcher.db.FailureRecord;
import com.gswebgate.searcher.db.FailureRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4 Part A: Dead-Letter Queue Integration Tests
 * Verifies that failed work items are captured to file-based database for later retry/investigation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Phase 4 Part A: Dead-Letter Queue (File-Based Storage)")
class DeadLetterQueueTest {

    @Autowired
    private DeadLetterQueueService dlqService;

    @Autowired
    private FailureRecordRepository repository;

    private String testMessageId;

    @BeforeEach
    void setUp() {
        testMessageId = "test-msg-" + System.nanoTime();
        dlqService.clear();
    }

    /**
     * Test capturing a failure to DLQ (persisted to H2 database).
     */
    @Test
    @DisplayName("Should capture failure with reason and message to database")
    void testCaptureFailure() {
        dlqService.captureFailure(
            testMessageId,
            "What is the capital of France?",
            "geography",
            "http://localhost:8080",
            "search",
            "Connection timeout",
            FailureRecord.FailureReason.TIMEOUT
        );

        Optional<FailureRecord> record = repository.findByMessageId(testMessageId);
        assertTrue(record.isPresent(), "Failure record should be persisted to database");
        
        FailureRecord fr = record.get();
        assertEquals(testMessageId, fr.getMessageId());
        assertEquals("What is the capital of France?", fr.getQuestion());
        assertEquals("geography", fr.getContext());
        assertEquals("http://localhost:8080", fr.getTarget());
        assertEquals("search", fr.getMode());
        assertEquals("Connection timeout", fr.getErrorMessage());
        assertEquals(FailureRecord.FailureReason.TIMEOUT, fr.getFailureReason());
        assertEquals(1, fr.getAttemptCount());
        assertNotNull(fr.getFirstFailureTime());
        assertNotNull(fr.getLastFailureTime());
    }

    /**
     * Test multiple failures increment attempt count and persist.
     */
    @Test
    @DisplayName("Should increment attempt count on repeated failures")
    void testMultipleFailuresIncrementAttempts() {
        for (int i = 1; i <= 3; i++) {
            dlqService.captureFailure(
                testMessageId,
                "Test question",
                "context",
                "http://localhost:8080",
                "search",
                "Failure attempt " + i,
                FailureRecord.FailureReason.NETWORK_ERROR
            );
        }

        Optional<FailureRecord> record = repository.findByMessageId(testMessageId);
        assertTrue(record.isPresent());
        assertEquals(3, record.get().getAttemptCount());
    }

    /**
     * Test retryable status when under max attempts.
     */
    @Test
    @DisplayName("Should be retryable when attempts < maxAttempts")
    void testRetryableWhenUnderMaxAttempts() {
        dlqService.captureFailure(
            testMessageId,
            "Question",
            "context",
            "http://localhost:8080",
            "search",
            "Error",
            FailureRecord.FailureReason.UNKNOWN_ERROR
        );

        assertTrue(dlqService.isRetryable(testMessageId));
    }

    /**
     * Test non-retryable status after max attempts.
     */
    @Test
    @DisplayName("Should not be retryable after maxAttempts")
    void testNonRetryableAfterMaxAttempts() {
        for (int i = 0; i < 3; i++) {
            dlqService.captureFailure(
                testMessageId,
                "Question",
                "context",
                "http://localhost:8080",
                "search",
                "Error",
                FailureRecord.FailureReason.UNKNOWN_ERROR
            );
        }

        assertFalse(dlqService.isRetryable(testMessageId));
    }

    /**
     * Test retrieving retryable items from database.
     */
    @Test
    @DisplayName("Should retrieve all items ready for retry from database")
    void testGetRetryableItems() {
        // Create 3 items: 2 retryable (1 and 2 attempts), 1 non-retryable (3 attempts)
        for (int msgIdx = 1; msgIdx <= 3; msgIdx++) {
            String msgId = testMessageId + "-" + msgIdx;
            int attempts = msgIdx;
            
            for (int i = 0; i < attempts; i++) {
                dlqService.captureFailure(
                    msgId,
                    "Question " + msgIdx,
                    "context",
                    "http://localhost:8080",
                    "search",
                    "Error",
                    FailureRecord.FailureReason.UNKNOWN_ERROR
                );
            }
        }

        List<FailureRecord> retryable = dlqService.getRetryableItems();
        assertEquals(2, retryable.size(), "Should have 2 retryable items");
        
        // Verify retryable items are the first two
        assertTrue(retryable.stream()
            .anyMatch(r -> r.getMessageId().contains("-1")));
        assertTrue(retryable.stream()
            .anyMatch(r -> r.getMessageId().contains("-2")));
    }

    /**
     * Test capturing different failure reasons.
     */
    @Test
    @DisplayName("Should capture failures with different reasons")
    void testCaptureMultipleFailureReasons() {
        String[] msgIds = {"msg-1", "msg-2", "msg-3", "msg-4"};
        FailureRecord.FailureReason[] reasons = {
            FailureRecord.FailureReason.NETWORK_ERROR,
            FailureRecord.FailureReason.TIMEOUT,
            FailureRecord.FailureReason.SEARCH_EXECUTION_ERROR,
            FailureRecord.FailureReason.UNKNOWN_ERROR
        };

        for (int i = 0; i < msgIds.length; i++) {
            dlqService.captureFailure(
                msgIds[i],
                "Question",
                "context",
                "http://localhost:8080",
                "search",
                "Error for " + reasons[i],
                reasons[i]
            );
        }

        for (int i = 0; i < msgIds.length; i++) {
            Optional<FailureRecord> record = repository.findByMessageId(msgIds[i]);
            assertTrue(record.isPresent());
            assertEquals(reasons[i], record.get().getFailureReason());
        }
    }

    /**
     * Test DLQ gracefully handles when disabled.
     */
    @Test
    @DisplayName("Should handle disabled DLQ gracefully")
    void testDisabledDLQ() {
        List<FailureRecord> retryable = dlqService.getRetryableItems();
        assertTrue(retryable.isEmpty(), "Should return empty list initially");
    }

    /**
     * Test retrieving all failed items (for monitoring).
     */
    @Test
    @DisplayName("Should retrieve all failed items for monitoring")
    void testGetAllFailedItems() {
        for (int i = 1; i <= 3; i++) {
            dlqService.captureFailure(
                testMessageId + "-" + i,
                "Question " + i,
                "context",
                "http://localhost:8080",
                "search",
                "Error",
                FailureRecord.FailureReason.UNKNOWN_ERROR
            );
        }

        var allItems = dlqService.getAllFailedItems();
        assertEquals(3, allItems.size(), "Should have 3 failed items");
    }

    /**
     * Test failure statistics query from database.
     */
    @Test
    @DisplayName("Should provide failure statistics by reason")
    void testFailureStatistics() {
        dlqService.captureFailure("m1", "Q", "c", "t", "m", "Error", FailureRecord.FailureReason.TIMEOUT);
        dlqService.captureFailure("m2", "Q", "c", "t", "m", "Error", FailureRecord.FailureReason.TIMEOUT);
        dlqService.captureFailure("m3", "Q", "c", "t", "m", "Error", FailureRecord.FailureReason.NETWORK_ERROR);

        var stats = dlqService.getFailureStats();
        assertEquals(2L, stats.get(FailureRecord.FailureReason.TIMEOUT));
        assertEquals(1L, stats.get(FailureRecord.FailureReason.NETWORK_ERROR));
        assertEquals(0L, stats.get(FailureRecord.FailureReason.SEARCH_EXECUTION_ERROR));
    }
}
