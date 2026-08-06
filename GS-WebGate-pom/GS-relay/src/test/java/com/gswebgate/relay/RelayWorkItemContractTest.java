package com.gswebgate.relay;

import com.gswebgate.relay.contract.PolledWork;
import com.gswebgate.relay.contract.SearchResult;
import com.gswebgate.relay.contract.WorkItemRequest;
import com.gswebgate.relay.contract.WorkItemResponse;
import com.gswebgate.relay.db.ResultRepository;
import com.gswebgate.relay.db.WorkItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Phase 0: Relay Work Item Contract Tests")
class RelayWorkItemContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private ResultRepository resultRepository;

    @BeforeEach
    void setUp() {
        workItemRepository.deleteAll();
        resultRepository.deleteAll();
    }

    @Test
    @DisplayName("submit_work_returns_message_id")
    void testSubmitWorkReturnsMessageId() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest("What is the capital of France?");

        // Act
        MvcResult result = mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        WorkItemResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                WorkItemResponse.class
        );

        assertNotNull(response.getMessageId());
        assertFalse(response.getMessageId().isBlank());
    }

    @Test
    @DisplayName("fetch_pending_work_returns_next_pending_item")
    void testFetchPendingWorkReturnsNextPendingItem() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest("What is the capital of France?");
        MvcResult submitResult = mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andReturn();

        WorkItemResponse submitResponse = objectMapper.readValue(
                submitResult.getResponse().getContentAsString(),
                WorkItemResponse.class
        );

        // Act
        MvcResult fetchResult = mockMvc.perform(
                post("/messages/next-pending")
        )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        PolledWork fetchedWork = objectMapper.readValue(
                fetchResult.getResponse().getContentAsString(),
                PolledWork.class
        );

        assertNotNull(fetchedWork);
        assertNotNull(fetchedWork.getMessageId());
        assertEquals(request.getQuestion(), fetchedWork.getQuestion());
    }

    @Test
    @DisplayName("submit_result_is_linked_to_message_id")
    void testSubmitResultIsLinkedToMessageId() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest("What is the capital of France?");
        MvcResult submitResult = mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andReturn();

        WorkItemResponse submitResponse = objectMapper.readValue(
                submitResult.getResponse().getContentAsString(),
                WorkItemResponse.class
        );

        String messageId = submitResponse.getMessageId();

        // Act - Store a result
        SearchResult result = new SearchResult(
                messageId,
                true,
                "Paris is the capital of France",
                0.95
        );
        result.setSources(List.of("https://example.com"));
        result.setProcessingTimeMs(150);

        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        )
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        MvcResult retrieveResult = mockMvc.perform(
                get("/messages/results/{messageId}", messageId)
        )
                .andExpect(status().isOk())
                .andReturn();

        SearchResult retrievedResult = objectMapper.readValue(
                retrieveResult.getResponse().getContentAsString(),
                SearchResult.class
        );

        assertEquals(messageId, retrievedResult.getMessageId());
        assertTrue(retrievedResult.isAnswerFound());
        assertEquals("Paris is the capital of France", retrievedResult.getAnswer());
    }

    @Test
    @DisplayName("fetch_result_returns_correlated_result")
    void testFetchResultReturnsCorrelatedResult() throws Exception {
        // Arrange
        String messageId = "test-message-id-12345";

        SearchResult result = new SearchResult(
                messageId,
                true,
                "The answer is 42",
                0.98
        );
        result.setSources(List.of("https://example.com/source1", "https://example.com/source2"));
        result.setProcessingTimeMs(200);

        // Act - Store the result
        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        )
                .andExpect(status().isCreated())
                .andReturn();

        // Act - Retrieve the result
        MvcResult retrieveResult = mockMvc.perform(
                get("/messages/results/{messageId}", messageId)
        )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        SearchResult retrievedResult = objectMapper.readValue(
                retrieveResult.getResponse().getContentAsString(),
                SearchResult.class
        );

        assertEquals(messageId, retrievedResult.getMessageId());
        assertTrue(retrievedResult.isAnswerFound());
        assertEquals("The answer is 42", retrievedResult.getAnswer());
        assertEquals(0.98, retrievedResult.getConfidence());
        assertEquals(200, retrievedResult.getProcessingTimeMs());
        assertNotNull(retrievedResult.getSources());
        assertEquals(2, retrievedResult.getSources().size());
    }

    @Test
    @DisplayName("complete_end_to_end_workflow")
    void testCompleteEndToEndWorkflow() throws Exception {
        // ============ STEP 1: CLIENT SUBMITS WORK ============
        WorkItemRequest clientRequest = new WorkItemRequest(
                "What is the largest planet in our solar system?"
        );
        clientRequest.setContext("Solar system planets");
        clientRequest.setMode("search");

        MvcResult submitResult = mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest))
        )
                .andExpect(status().isCreated())
                .andReturn();

        WorkItemResponse submitResponse = objectMapper.readValue(
                submitResult.getResponse().getContentAsString(),
                WorkItemResponse.class
        );

        String messageId = submitResponse.getMessageId();
        assertNotNull(messageId);
        assertFalse(messageId.isBlank());

        // ============ STEP 2: SEARCHER POLLS FOR NEXT WORK ============
        MvcResult pollResult = mockMvc.perform(
                post("/messages/next-pending")
        )
                .andExpect(status().isOk())
                .andReturn();

        PolledWork polledWork = objectMapper.readValue(
                pollResult.getResponse().getContentAsString(),
                PolledWork.class
        );

        assertNotNull(polledWork);
        String polledMessageId = polledWork.getMessageId();
        assertEquals(clientRequest.getQuestion(), polledWork.getQuestion());
        assertEquals(clientRequest.getContext(), polledWork.getContext());
        assertEquals(clientRequest.getMode(), polledWork.getMode());

        // ============ STEP 3: SEARCHER EXECUTES AND PUBLISHES RESULT ============
        SearchResult executionResult = new SearchResult(
                polledMessageId,
                true,
                "Jupiter is the largest planet in our solar system",
                0.99
        );
        executionResult.setSources(List.of(
                "https://nasa.gov/planets/jupiter",
                "https://space.com/jupiter-facts"
        ));
        executionResult.setProcessingTimeMs(350);

        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executionResult))
        )
                .andExpect(status().isCreated())
                .andReturn();

        // ============ STEP 4: CLIENT RETRIEVES RESULT ============
        MvcResult retrieveResult = mockMvc.perform(
                get("/messages/results/{messageId}", polledMessageId)
        )
                .andExpect(status().isOk())
                .andReturn();

        SearchResult retrievedResult = objectMapper.readValue(
                retrieveResult.getResponse().getContentAsString(),
                SearchResult.class
        );

        // ============ VERIFY FULL FLOW ============
        assertEquals(messageId, retrievedResult.getMessageId());
        assertTrue(retrievedResult.isAnswerFound());
        assertEquals("Jupiter is the largest planet in our solar system", retrievedResult.getAnswer());
        assertEquals(0.99, retrievedResult.getConfidence());
        assertEquals(350, retrievedResult.getProcessingTimeMs());
        assertNotNull(retrievedResult.getSources());
        assertEquals(2, retrievedResult.getSources().size());
        assertTrue(retrievedResult.getSources().contains("https://nasa.gov/planets/jupiter"));
    }
}
