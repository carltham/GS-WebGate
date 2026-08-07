package com.gswebgate.relay;

import com.gswebgate.relay.contract.DLQSyncRequest;
import com.gswebgate.relay.contract.SearchResult;
import com.gswebgate.relay.contract.WorkItemRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Phase 6: Validation Tests")
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // WorkItemRequest Validation Tests

    @Test
    @DisplayName("validation_rejects_null_question")
    void testValidationRejectsNullQuestion() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest();
        request.setQuestion(null);

        // Act & Assert
        mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.question").exists());
    }

    @Test
    @DisplayName("validation_rejects_blank_question")
    void testValidationRejectsBlankQuestion() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest();
        request.setQuestion("   ");

        // Act & Assert
        mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_rejects_oversized_question")
    void testValidationRejectsOversizedQuestion() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest();
        request.setQuestion("A".repeat(1001)); // Exceeds 1000 char limit

        // Act & Assert
        mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.question").exists());
    }

    @Test
    @DisplayName("validation_rejects_oversized_context")
    void testValidationRejectsOversizedContext() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest();
        request.setQuestion("What is 2+2?");
        request.setContext("A".repeat(5001)); // Exceeds 5000 char limit

        // Act & Assert
        mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_accepts_valid_work_item_request")
    void testValidationAcceptsValidWorkItemRequest() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest("What is the capital of France?");

        // Act & Assert
        mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated());
    }

    // SearchResult Validation Tests

    @Test
    @DisplayName("validation_rejects_null_message_id_in_result")
    void testValidationRejectsNullMessageIdInResult() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setMessageId(null);
        result.setAnswerFound(true);
        result.setAnswer("Paris");
        result.setConfidence(0.95);

        // Act & Assert
        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_rejects_invalid_confidence_too_high")
    void testValidationRejectsInvalidConfidenceTooHigh() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setMessageId("msg-001");
        result.setAnswerFound(true);
        result.setAnswer("Paris");
        result.setConfidence(1.5); // Exceeds 1.0

        // Act & Assert
        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confidence").exists());
    }

    @Test
    @DisplayName("validation_rejects_invalid_confidence_negative")
    void testValidationRejectsInvalidConfidenceNegative() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setMessageId("msg-001");
        result.setAnswerFound(true);
        result.setAnswer("Paris");
        result.setConfidence(-0.1); // Below 0.0

        // Act & Assert
        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_rejects_negative_processing_time")
    void testValidationRejectsNegativeProcessingTime() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setMessageId("msg-001");
        result.setAnswerFound(true);
        result.setAnswer("Paris");
        result.setConfidence(0.95);
        result.setProcessingTimeMs(-100);

        // Act & Assert
        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_accepts_valid_search_result")
    void testValidationAcceptsValidSearchResult() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setMessageId("msg-001");
        result.setAnswerFound(true);
        result.setAnswer("Paris");
        result.setConfidence(0.95);
        result.setProcessingTimeMs(150);

        // Act & Assert
        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        )
                .andExpect(status().isCreated());
    }

    // DLQSyncRequest Validation Tests

    @Test
    @DisplayName("validation_rejects_null_message_id_in_dlq")
    void testValidationRejectsNullMessageIdInDLQ() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest();
        request.setMessageId(null);
        request.setQuestion("Test");
        request.setErrorMessage("Error");
        request.setFailureReason("TIMEOUT");
        request.setAttemptCount(1);

        // Act & Assert
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_rejects_null_attempt_count")
    void testValidationRejectsNullAttemptCount() throws Exception {
        // Arrange
        String json = "{\"messageId\":\"msg-001\",\"question\":\"Test\",\"errorMessage\":\"Error\",\"failureReason\":\"TIMEOUT\",\"attemptCount\":null}";

        // Act & Assert
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_rejects_invalid_attempt_count_zero")
    void testValidationRejectsInvalidAttemptCountZero() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest();
        request.setMessageId("msg-001");
        request.setQuestion("Test");
        request.setErrorMessage("Error");
        request.setFailureReason("TIMEOUT");
        request.setAttemptCount(0); // Must be >= 1

        // Act & Assert
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validation_accepts_valid_dlq_sync_request")
    void testValidationAcceptsValidDLQSyncRequest() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-001",
                "What is 2+2?",
                "math",
                "calculator",
                "compute",
                "Timeout after 5s",
                "TIMEOUT",
                1
        );

        // Act & Assert
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("validation_rejects_oversized_error_message")
    void testValidationRejectsOversizedErrorMessage() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest();
        request.setMessageId("msg-001");
        request.setQuestion("Test");
        request.setErrorMessage("A".repeat(1001)); // Exceeds 1000 chars
        request.setFailureReason("NETWORK_ERROR");
        request.setAttemptCount(1);

        // Act & Assert
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }
}
