package com.gswebgate.searcher;

import com.gswebgate.searcher.contract.PolledWork;
import com.gswebgate.searcher.contract.SearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the searcher worker demonstrating complete flow:
 * 1. Poll relay for work
 * 2. Execute search
 * 3. Submit result back to relay
 */
@SpringBootTest
@AutoConfigureWebClient
@DisplayName("Searcher Worker End-to-End Flow Test")
class SearcherWorkerEndToEndTest {

    @Autowired
    private SearcherWorker searcherWorker;

    @Autowired
    private RelayClient relayClient;

    @Autowired
    private SearchExecutor searchExecutor;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;

    @Test
    @DisplayName("complete_searcher_workflow_with_relay_interaction")
    void testCompleteSearcherWorkflowWithRelayInteraction() throws Exception {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // Prepare test data
        String messageId = "test-msg-jupiter-12345";
        String question = "What is the largest planet in our solar system?";

        PolledWork polledWork = new PolledWork(messageId, question);
        polledWork.setContext("Solar system planets");
        polledWork.setMode("search");

        SearchResult expectedResult = new SearchResult(
                messageId,
                true,
                "Jupiter is the largest planet in our solar system",
                0.99
        );

        // ============ STEP 1: MOCK RELAY POLL RESPONSE ============
        mockServer
                .expect(MockRestRequestMatchers.requestTo("http://localhost:8080/messages/next-pending"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(
                        objectMapper.writeValueAsString(polledWork),
                        MediaType.APPLICATION_JSON
                ));

        // ============ STEP 2: MOCK RELAY RESULT SUBMISSION ============
        mockServer
                .expect(MockRestRequestMatchers.requestTo("http://localhost:8080/messages/results"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.CREATED));

        // ============ ACT: EXECUTE WORKER CYCLE ============
        boolean processed = searcherWorker.processNextWork();

        // ============ VERIFY: ============
        assertTrue(processed, "Worker should have processed the work");
        mockServer.verify();
    }

    @Test
    @DisplayName("searcher_finds_no_work_when_relay_empty")
    void testSearcherHandlesEmptyQueue() throws Exception {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // Mock relay returning 204 No Content
        mockServer
                .expect(MockRestRequestMatchers.requestTo("http://localhost:8080/messages/next-pending"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());

        // Act
        boolean processed = searcherWorker.processNextWork();

        // Assert
        assertFalse(processed, "Worker should return false when no work available");
        mockServer.verify();
    }

    @Test
    @DisplayName("search_executor_produces_correct_answer_for_capital_question")
    void testSearchExecutorCapitalQuestion() {
        // Arrange
        PolledWork polledWork = new PolledWork("msg-1", "What is the capital of France?");
        polledWork.setContext("European capitals");

        // Act
        SearchResult result = searchExecutor.execute(polledWork);

        // Assert
        assertEquals("msg-1", result.getMessageId());
        assertTrue(result.isAnswerFound());
        assertEquals("Paris is the capital of France", result.getAnswer());
        assertEquals(0.99, result.getConfidence());
        assertNotNull(result.getSources());
        assertTrue(result.getSources().size() > 0);
    }

    @Test
    @DisplayName("search_executor_produces_correct_answer_for_planet_question")
    void testSearchExecutorPlanetQuestion() {
        // Arrange
        PolledWork polledWork = new PolledWork("msg-2", "What is the largest planet in our solar system?");
        polledWork.setMode("search");

        // Act
        SearchResult result = searchExecutor.execute(polledWork);

        // Assert
        assertEquals("msg-2", result.getMessageId());
        assertTrue(result.isAnswerFound());
        assertEquals("Jupiter is the largest planet in our solar system", result.getAnswer());
        assertEquals(0.99, result.getConfidence());
        assertNotNull(result.getSources());
        assertTrue(result.getSources().size() > 0);
    }

    @Test
    @DisplayName("search_executor_handles_unknown_questions")
    void testSearchExecutorUnknownQuestion() {
        // Arrange
        PolledWork polledWork = new PolledWork("msg-3", "What is the meaning of life?");

        // Act
        SearchResult result = searchExecutor.execute(polledWork);

        // Assert
        assertEquals("msg-3", result.getMessageId());
        assertFalse(result.isAnswerFound());
        assertTrue(result.getConfidence() < 0.5);
    }
}
