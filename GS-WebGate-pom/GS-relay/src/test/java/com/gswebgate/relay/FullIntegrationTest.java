package com.gswebgate.relay;

import com.gswebgate.relay.contract.SearchResult;
import com.gswebgate.relay.contract.WorkItemRequest;
import com.gswebgate.relay.contract.WorkItemResponse;
import com.gswebgate.relay.contract.PolledWork;
import com.gswebgate.relay.db.ResultRepository;
import com.gswebgate.relay.db.WorkItemRepository;
import com.gswebgate.searcher.SearchExecutor;
import com.gswebgate.searcher.RelayClient;
import com.gswebgate.searcher.SearcherWorker;
import com.gswebgate.searcher.SecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3 Integration Test: Full workflow combining relay and searcher
 *
 * This test verifies the complete cycle:
 * 1. Client submits work to relay
 * 2. Searcher polls for work from relay
 * 3. Searcher executes search
 * 4. Searcher submits result back to relay
 * 5. Client retrieves result from relay
 *
 * Uses real HTTP communication (TestRestTemplate) with embedded relay server,
 * and directly instantiated searcher components.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FullIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private ResultRepository resultRepository;

    private SearcherWorker searcherWorker;
    private String relayBaseUrl;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        workItemRepository.deleteAll();
        resultRepository.deleteAll();

        // Get relay base URL from test environment
        relayBaseUrl = restTemplate.getRootUri();

        // Create searcher components for integration
        RestTemplate searcherRestTemplate = new RestTemplate();
        RelayClient relayClient = new RelayClient(searcherRestTemplate, relayBaseUrl);
        SearchExecutor searchExecutor = new SearchExecutor();
        
        // Create security validator (will allow localhost as private)
        SecurityValidator securityValidator = new SecurityValidator();
        
        // Create searcher worker with explicit retry config and security validator
        searcherWorker = new SearcherWorker(relayClient, searchExecutor, 3, 3, 5000, 30000, securityValidator);
    }

    @Test
    void testFullWorkflowCapitalQuestion() {
        // ===== STEP 1: CLIENT SUBMITS WORK =====
        WorkItemRequest request = new WorkItemRequest(
            "What is the capital of France?",
            "geography",
            "education",
            "search"
        );

        ResponseEntity<WorkItemResponse> submitResponse = restTemplate.postForEntity(
            "/messages",
            request,
            WorkItemResponse.class
        );

        assertEquals(HttpStatus.CREATED, submitResponse.getStatusCode());
        assertNotNull(submitResponse.getBody());
        String messageId = submitResponse.getBody().getMessageId();
        assertNotNull(messageId);
        System.out.println("✓ Work submitted with message ID: " + messageId);

        // ===== STEP 2: SEARCHER POLLS AND PROCESSES =====
        boolean processed = searcherWorker.processNextWork();
        assertTrue(processed, "Searcher should have processed one work item");
        System.out.println("✓ Searcher processed work");

        // ===== STEP 3: CLIENT RETRIEVES RESULT =====
        ResponseEntity<SearchResult> resultResponse = restTemplate.getForEntity(
            "/messages/results/{messageId}",
            SearchResult.class,
            messageId
        );

        assertEquals(HttpStatus.OK, resultResponse.getStatusCode());
        assertNotNull(resultResponse.getBody());
        SearchResult result = resultResponse.getBody();
        System.out.println("✓ Result retrieved: " + result.getAnswer());

        // ===== VERIFY RESULT CONTENT =====
        assertEquals(messageId, result.getMessageId());
        System.out.println("✓ Result retrieved - answerFound: " + result.isAnswerFound() + 
                          ", answer: " + result.getAnswer() + 
                          ", confidence: " + result.getConfidence());
        assertTrue(result.isAnswerFound(), "Result should have answerFound=true, got: " + result);
        assertEquals("Paris is the capital of France", result.getAnswer());
        assertTrue(result.getConfidence() > 0.9);
        assertNotNull(result.getSources());
        assertTrue(result.getProcessingTimeMs() > 0);
    }

    @Test
    void testFullWorkflowPlanetQuestion() {
        // Submit planet question
        WorkItemRequest request = new WorkItemRequest(
            "What is the largest planet in our solar system?",
            "astronomy",
            "education",
            "search"
        );

        ResponseEntity<WorkItemResponse> submitResponse = restTemplate.postForEntity(
            "/messages",
            request,
            WorkItemResponse.class
        );

        assertEquals(HttpStatus.CREATED, submitResponse.getStatusCode());
        String messageId = submitResponse.getBody().getMessageId();

        // Searcher processes
        boolean processed = searcherWorker.processNextWork();
        assertTrue(processed);

        // Client retrieves
        ResponseEntity<SearchResult> resultResponse = restTemplate.getForEntity(
            "/messages/results/{messageId}",
            SearchResult.class,
            messageId
        );

        assertEquals(HttpStatus.OK, resultResponse.getStatusCode());
        SearchResult result = resultResponse.getBody();
        assertEquals("Jupiter is the largest planet in our solar system", result.getAnswer());
        assertTrue(result.isAnswerFound());
    }

    @Test
    void testFullWorkflowUnknownQuestion() {
        // Submit unknown question
        WorkItemRequest request = new WorkItemRequest(
            "Tell me something about quantum entanglement",
            "physics",
            "research",
            "search"
        );

        ResponseEntity<WorkItemResponse> submitResponse = restTemplate.postForEntity(
            "/messages",
            request,
            WorkItemResponse.class
        );

        String messageId = submitResponse.getBody().getMessageId();

        // Searcher processes
        boolean processed = searcherWorker.processNextWork();
        assertTrue(processed);

        // Client retrieves
        ResponseEntity<SearchResult> resultResponse = restTemplate.getForEntity(
            "/messages/results/{messageId}",
            SearchResult.class,
            messageId
        );

        assertEquals(HttpStatus.OK, resultResponse.getStatusCode());
        SearchResult result = resultResponse.getBody();
        assertFalse(result.isAnswerFound());
        assertTrue(result.getConfidence() < 0.5);
    }

    @Test
    void testMultipleWorkItemsInSequence() {
        // Submit multiple work items
        String messageId1 = submitWorkAndGetId("What is the capital of France?");
        String messageId2 = submitWorkAndGetId("What is the largest planet?");
        String messageId3 = submitWorkAndGetId("What is the smallest planet?");

        System.out.println("✓ Submitted 3 work items");

        // Process all three
        assertTrue(searcherWorker.processNextWork());
        assertTrue(searcherWorker.processNextWork());
        assertTrue(searcherWorker.processNextWork());
        System.out.println("✓ Searcher processed all 3 items");

        // Retrieve all results
        SearchResult result1 = restTemplate.getForObject(
            "/messages/results/{id}",
            SearchResult.class,
            messageId1
        );
        SearchResult result2 = restTemplate.getForObject(
            "/messages/results/{id}",
            SearchResult.class,
            messageId2
        );
        SearchResult result3 = restTemplate.getForObject(
            "/messages/results/{id}",
            SearchResult.class,
            messageId3
        );

        // Verify correlation
        assertEquals(messageId1, result1.getMessageId());
        assertEquals(messageId2, result2.getMessageId());
        assertEquals(messageId3, result3.getMessageId());

        // Verify content
        assertEquals("Paris is the capital of France", result1.getAnswer());
        assertEquals("Jupiter is the largest planet in our solar system", result2.getAnswer());
        System.out.println("✓ All results correctly correlated by message ID");
    }

    @Test
    void testEmptyQueueHandling() {
        // Try to process when no work available
        boolean processed = searcherWorker.processNextWork();
        assertFalse(processed, "Should return false when queue is empty");
        System.out.println("✓ Empty queue handled gracefully");
    }

    @Test
    void testResultNotFoundBeforeProcessing() {
        // Try to retrieve result for non-existent message
        ResponseEntity<SearchResult> response = restTemplate.getForEntity(
            "/messages/results/{messageId}",
            SearchResult.class,
            "non-existent-id"
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        System.out.println("✓ Non-existent result returns 404");
    }

    // Helper method
    private String submitWorkAndGetId(String question) {
        WorkItemRequest request = new WorkItemRequest(question, "test", "test", "search");
        ResponseEntity<WorkItemResponse> response = restTemplate.postForEntity(
            "/messages",
            request,
            WorkItemResponse.class
        );
        return response.getBody().getMessageId();
    }
}
