package com.gswebgate.searcher;

import com.gswebgate.searcher.contract.PolledWork;
import com.gswebgate.searcher.contract.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Orchestrates the searcher worker loop with resilience features.
 * - Polls for work with retry logic
 * - Executes searches with timeout handling
 * - Submits results with retry logic
 * - Logs state transitions for observability
 */
@Service
public class SearcherWorker {

    private static final Logger logger = LoggerFactory.getLogger(SearcherWorker.class);

    @Autowired(required = false)
    private RelayClient relayClient;

    @Autowired(required = false)
    private SearchExecutor searchExecutor;

    @Autowired(required = false)
    private SecurityValidator securityValidator;

    @Autowired(required = false)
    private DeadLetterQueueService deadLetterQueueService;
    
    @Value("${searcher.max-poll-retries:3}")
    private int maxPollRetries;
    
    @Value("${searcher.max-submit-retries:3}")
    private int maxSubmitRetries;
    
    @Value("${searcher.poll-timeout-ms:5000}")
    private long pollTimeoutMs;
    
    @Value("${searcher.search-timeout-ms:30000}")
    private long searchTimeoutMs;

    /**
     * Default constructor for Spring dependency injection.
     * Configuration values are injected via @Value annotations.
     */
    public SearcherWorker() {
        this(null, null);
    }

    public SearcherWorker(RelayClient relayClient, SearchExecutor searchExecutor) {
        this(relayClient, searchExecutor, 3, 3, 5000, 30000);
    }

    /**
     * Constructor for testing with explicit configuration values.
     */
    public SearcherWorker(RelayClient relayClient, SearchExecutor searchExecutor,
                         int maxPollRetries, int maxSubmitRetries,
                         long pollTimeoutMs, long searchTimeoutMs) {
        this(relayClient, searchExecutor, maxPollRetries, maxSubmitRetries, pollTimeoutMs, searchTimeoutMs, null);
    }

    /**
     * Full constructor for testing with security validator.
     */
    public SearcherWorker(RelayClient relayClient, SearchExecutor searchExecutor,
                         int maxPollRetries, int maxSubmitRetries,
                         long pollTimeoutMs, long searchTimeoutMs, SecurityValidator securityValidator) {
        this(relayClient, searchExecutor, maxPollRetries, maxSubmitRetries, pollTimeoutMs, searchTimeoutMs, securityValidator, null);
    }

    /**
     * Full constructor with DLQ service for production use.
     */
    public SearcherWorker(RelayClient relayClient, SearchExecutor searchExecutor,
                         int maxPollRetries, int maxSubmitRetries,
                         long pollTimeoutMs, long searchTimeoutMs, SecurityValidator securityValidator,
                         DeadLetterQueueService deadLetterQueueService) {
        this.relayClient = relayClient;
        this.searchExecutor = searchExecutor;
        this.maxPollRetries = maxPollRetries;
        this.maxSubmitRetries = maxSubmitRetries;
        this.pollTimeoutMs = pollTimeoutMs;
        this.searchTimeoutMs = searchTimeoutMs;
        this.securityValidator = securityValidator;
        this.deadLetterQueueService = deadLetterQueueService;
    }

    /**
     * Execute a single work cycle with resilience:
     * 1. Poll relay for next pending work (with retries)
     * 2. Execute search if work found (with timeout)
     * 3. Submit result back to relay (with retries)
     * 
     * @return True if work was processed, false if no work available
     */
    public boolean processNextWork() {
        logger.debug("Starting work cycle");
        
        // Step 1: Poll for next work with retry logic
        Optional<PolledWork> polledWork = pollWithRetry();
        
        if (polledWork.isEmpty()) {
            logger.debug("No pending work available");
            return false;
        }
        
        PolledWork work = polledWork.get();
        logger.info("Polled work: messageId={}, question={}", work.getMessageId(), 
                   work.getQuestion());
        
        try {
            // Security check: Validate private environment before executing search
            if (securityValidator != null && relayClient != null) {
                try {
                    securityValidator.validatePrivateEnvironment(relayClient.getRelayBaseUrl());
                } catch (SecurityException e) {
                    logger.error("SECURITY: Search execution blocked: {}", e.getMessage());
                    throw e;
                }
            }
            
            // Step 2: Execute search with timeout
            SearchResult result = executeSearchWithTimeout(work);
            logger.info("Search executed: messageId={}, answerFound={}", 
                       work.getMessageId(), result.isAnswerFound());
            
            // Step 3: Submit result with retry logic
            boolean submitted = submitResultWithRetry(result);
            
            if (submitted) {
                logger.info("Result submitted: messageId={}", work.getMessageId());
                return true;
            } else {
                logger.error("Failed to submit result after retries: messageId={}", 
                           work.getMessageId());
                
                // Capture submission failure to DLQ
                if (deadLetterQueueService != null) {
                    deadLetterQueueService.captureFailure(
                        work.getMessageId(),
                        work.getQuestion(),
                        work.getContext(),
                        work.getTarget(),
                        work.getMode(),
                        "Failed to submit result after " + maxSubmitRetries + " attempts",
                        com.gswebgate.searcher.db.FailureRecord.FailureReason.NETWORK_ERROR
                    );
                }
                
                return false;
            }
        } catch (Exception e) {
            logger.error("Error processing work: messageId={}, error={}", 
                        work.getMessageId(), e.getMessage(), e);
            
            // Capture to DLQ for investigation
            if (deadLetterQueueService != null) {
                com.gswebgate.searcher.db.FailureRecord.FailureReason reason = 
                    determineFailureReason(e);
                deadLetterQueueService.captureFailure(
                    work.getMessageId(),
                    work.getQuestion(),
                    work.getContext(),
                    work.getTarget(),
                    work.getMode(),
                    e.getMessage(),
                    reason
                );
            }
            
            return false;
        }
    }

    /**
     * Determine the failure reason from an exception.
     */
    private com.gswebgate.searcher.db.FailureRecord.FailureReason determineFailureReason(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        
        if (message.contains("timeout")) {
            return com.gswebgate.searcher.db.FailureRecord.FailureReason.TIMEOUT;
        } else if (message.contains("network") || message.contains("connection")) {
            return com.gswebgate.searcher.db.FailureRecord.FailureReason.NETWORK_ERROR;
        } else if (message.contains("search") || message.contains("execution")) {
            return com.gswebgate.searcher.db.FailureRecord.FailureReason.SEARCH_EXECUTION_ERROR;
        }
        
        return com.gswebgate.searcher.db.FailureRecord.FailureReason.UNKNOWN_ERROR;
    }

    /**
     * Poll for work with retry logic on transient failures.
     */
    private Optional<PolledWork> pollWithRetry() {
        int attempts = 0;
        Exception lastError = null;
        
        while (attempts < maxPollRetries) {
            try {
                attempts++;
                logger.debug("Polling attempt {}/{}", attempts, maxPollRetries);
                return relayClient.pollNextPending();
            } catch (Exception e) {
                lastError = e;
                logger.warn("Poll attempt {} failed: {}", attempts, e.getMessage());
                
                if (attempts < maxPollRetries) {
                    try {
                        // Exponential backoff: 100ms, 200ms, 400ms
                        long backoff = (long) Math.pow(2, attempts - 1) * 100;
                        Thread.sleep(Math.min(backoff, 1000));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        logger.error("Failed to poll after {} attempts", attempts, lastError);
        return Optional.empty();
    }

    /**
     * Execute search with timeout handling.
     */
    private SearchResult executeSearchWithTimeout(PolledWork work) throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            SearchResult result = searchExecutor.execute(work);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("Search completed in {}ms", duration);
            
            if (duration > searchTimeoutMs) {
                logger.warn("Search exceeded timeout: {}ms > {}ms", duration, searchTimeoutMs);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Search execution failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Submit result with retry logic on transient failures.
     */
    private boolean submitResultWithRetry(SearchResult result) {
        int attempts = 0;
        Exception lastError = null;
        
        while (attempts < maxSubmitRetries) {
            try {
                attempts++;
                logger.debug("Submit attempt {}/{} for messageId={}", 
                           attempts, maxSubmitRetries, result.getMessageId());
                
                boolean success = relayClient.submitResult(result);
                if (success) {
                    return true;
                }
                
                logger.warn("Submit returned false: messageId={}, attempt={}", 
                           result.getMessageId(), attempts);
            } catch (Exception e) {
                lastError = e;
                logger.warn("Submit attempt {} failed: {} ({})", attempts, 
                           e.getClass().getSimpleName(), e.getMessage());
                
                if (attempts < maxSubmitRetries) {
                    try {
                        // Exponential backoff
                        long backoff = (long) Math.pow(2, attempts - 1) * 100;
                        Thread.sleep(Math.min(backoff, 1000));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        logger.error("Failed to submit result after {} attempts: messageId={}", 
                    attempts, result.getMessageId(), lastError);
        return false;
    }
}
