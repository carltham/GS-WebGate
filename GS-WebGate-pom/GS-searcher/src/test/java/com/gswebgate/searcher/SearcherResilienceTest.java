package com.gswebgate.searcher;

import com.gswebgate.searcher.contract.PolledWork;
import com.gswebgate.searcher.contract.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Phase 3 Part B: Resilience tests for SearcherWorker.
 * Verifies retry logic, timeout handling, and error recovery.
 */
@SpringBootTest
@DisplayName("Phase 3 Part B: Searcher Resilience & Observability")
class SearcherResilienceTest {

    @Autowired
    private SearcherWorker searcherWorker;

    @MockBean
    private RelayClient relayClient;

    @MockBean
    private SearchExecutor searchExecutor;

    private PolledWork testWork;
    private SearchResult testResult;

    @BeforeEach
    void setUp() {
        testWork = new PolledWork();
        testWork.setMessageId("test-msg-123");
        testWork.setQuestion("What is 2+2?");
        testWork.setContext("math");

        testResult = new SearchResult();
        testResult.setMessageId("test-msg-123");
        testResult.setAnswerFound(true);
        testResult.setAnswer("4");
        testResult.setConfidence(0.99);
        
        // Mock RelayClient to return a localhost URL for security validation
        when(relayClient.getRelayBaseUrl()).thenReturn("http://localhost:8080");
    }

    @Test
    @DisplayName("Should retry polling on network failure")
    void testPollRetryOnNetworkFailure() {
        // First two calls fail, third succeeds
        when(relayClient.pollNextPending())
            .thenThrow(new RuntimeException("Network error"))
            .thenThrow(new RuntimeException("Network error"))
            .thenReturn(Optional.of(testWork));

        when(searchExecutor.execute(any())).thenReturn(testResult);
        when(relayClient.submitResult(any())).thenReturn(true);

        boolean processed = searcherWorker.processNextWork();

        assertTrue(processed, "Should succeed after retries");
        verify(relayClient, times(3)).pollNextPending();
        System.out.println("✓ Retry on poll failure");
    }

    @Test
    @DisplayName("Should give up after max poll retries")
    void testPollGiveUpAfterMaxRetries() {
        // All retries fail
        when(relayClient.pollNextPending())
            .thenThrow(new RuntimeException("Network error"));

        boolean processed = searcherWorker.processNextWork();

        assertFalse(processed, "Should return false after max retries");
        verify(relayClient, atLeast(3)).pollNextPending();
        System.out.println("✓ Give up after max retries");
    }

    @Test
    @DisplayName("Should retry submitting result on failure")
    void testSubmitRetryOnFailure() {
        when(relayClient.pollNextPending()).thenReturn(Optional.of(testWork));
        when(searchExecutor.execute(any())).thenReturn(testResult);

        // First two calls fail, third succeeds
        when(relayClient.submitResult(any()))
            .thenReturn(false)
            .thenReturn(false)
            .thenReturn(true);

        boolean processed = searcherWorker.processNextWork();

        assertTrue(processed, "Should succeed after result submission retries");
        verify(relayClient, times(3)).submitResult(testResult);
        System.out.println("✓ Retry on submit failure");
    }

    @Test
    @DisplayName("Should give up submitting after max retries")
    void testSubmitGiveUpAfterMaxRetries() {
        when(relayClient.pollNextPending()).thenReturn(Optional.of(testWork));
        when(searchExecutor.execute(any())).thenReturn(testResult);
        when(relayClient.submitResult(any())).thenReturn(false);

        boolean processed = searcherWorker.processNextWork();

        assertFalse(processed, "Should return false after max submit retries");
        verify(relayClient, atLeast(3)).submitResult(testResult);
        System.out.println("✓ Give up after max submit retries");
    }

    @Test
    @DisplayName("Should handle search execution errors gracefully")
    void testHandleSearchExecutionError() {
        when(relayClient.pollNextPending()).thenReturn(Optional.of(testWork));
        when(searchExecutor.execute(any()))
            .thenThrow(new RuntimeException("Search service unavailable"));

        boolean processed = searcherWorker.processNextWork();

        assertFalse(processed, "Should handle search errors gracefully");
        verify(relayClient, never()).submitResult(any());
        System.out.println("✓ Handle search execution error");
    }

    @Test
    @DisplayName("Should log state transitions for observability")
    void testStateTransitionsAreLogged() {
        when(relayClient.pollNextPending()).thenReturn(Optional.of(testWork));
        when(searchExecutor.execute(any())).thenReturn(testResult);
        when(relayClient.submitResult(any())).thenReturn(true);

        // This test verifies that no exceptions are thrown during logging
        // Actual log output verification would require log capture
        assertDoesNotThrow(() -> searcherWorker.processNextWork());
        System.out.println("✓ State transitions logged without errors");
    }

    @Test
    @DisplayName("Should handle empty queue gracefully")
    void testHandleEmptyQueue() {
        when(relayClient.pollNextPending()).thenReturn(Optional.empty());

        boolean processed = searcherWorker.processNextWork();

        assertFalse(processed);
        verify(relayClient, never()).submitResult(any());
        System.out.println("✓ Empty queue handled gracefully");
    }

    @Test
    @DisplayName("Should complete full cycle on first attempt when healthy")
    void testFullCycleNoRetries() {
        when(relayClient.pollNextPending()).thenReturn(Optional.of(testWork));
        when(searchExecutor.execute(any())).thenReturn(testResult);
        when(relayClient.submitResult(any())).thenReturn(true);

        boolean processed = searcherWorker.processNextWork();

        assertTrue(processed);
        verify(relayClient, times(1)).pollNextPending();
        verify(searchExecutor, times(1)).execute(testWork);
        verify(relayClient, times(1)).submitResult(testResult);
        System.out.println("✓ Full cycle without retries");
    }
}
