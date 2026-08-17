package com.gswebgate.relay;

import com.gswebgate.relay.contract.DLQSyncRequest;
import com.gswebgate.relay.contract.SearchResult;
import com.gswebgate.relay.contract.WorkItemRequest;
import com.gswebgate.relay.metrics.OperationalMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Phase 7: Relay Metrics Tests")
class MetricsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OperationalMetrics metrics;

    @BeforeEach
    void setUp() {
        // Metrics start at 0 for each test
    }

    // Work Item Submission Metrics

    @Test
    @DisplayName("metrics_records_work_item_submitted")
    void testMetricsRecordsWorkItemSubmitted() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest("What is 2+2?");

        // Act
        mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());

        // Assert
        assertEquals(1.0, metrics.getWorkItemsSubmittedCount());
    }

    @Test
    @DisplayName("metrics_records_multiple_work_item_submissions")
    void testMetricsRecordsMultipleWorkItemSubmissions() throws Exception {
        // Arrange
        WorkItemRequest request = new WorkItemRequest("Test question");

        // Act
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(
                    post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated());
        }

        // Assert
        assertEquals(3.0, metrics.getWorkItemsSubmittedCount());
    }

    // Result Storage Metrics

    @Test
    @DisplayName("metrics_records_work_item_completed")
    void testMetricsRecordsWorkItemCompleted() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setMessageId("msg-001");
        result.setAnswerFound(true);
        result.setAnswer("Test answer");
        result.setConfidence(0.95);

        // Act
        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result))
        ).andExpect(status().isCreated());

        // Assert
        assertEquals(1.0, metrics.getWorkItemsCompletedCount());
    }

    @Test
    @DisplayName("metrics_records_multiple_work_items_completed")
    void testMetricsRecordsMultipleWorkItemsCompleted() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setAnswerFound(true);
        result.setAnswer("Answer");
        result.setConfidence(0.95);

        // Act
        for (int i = 1; i <= 5; i++) {
            result.setMessageId("msg-" + i);
            mockMvc.perform(
                    post("/messages/results")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(result))
            ).andExpect(status().isCreated());
        }

        // Assert
        assertEquals(5.0, metrics.getWorkItemsCompletedCount());
    }

    // DLQ Sync Metrics

    @Test
    @DisplayName("metrics_records_dlq_failure_received")
    void testMetricsRecordsDLQFailureReceived() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-001",
                "What is pi?",
                "math",
                "calculator",
                "compute",
                "Timeout error",
                "TIMEOUT",
                1
        );

        // Act
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isAccepted());

        // Assert
        assertEquals(1.0, metrics.getDLQFailuresReceivedCount());
    }

    @Test
    @DisplayName("metrics_records_dlq_failure_synced")
    void testMetricsRecordsDLQFailureSynced() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-001",
                "What is pi?",
                "math",
                "calculator",
                "compute",
                "Timeout error",
                "TIMEOUT",
                1
        );

        // Act
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isAccepted());

        // Assert
        assertEquals(1.0, metrics.getDLQFailuresSyncedCount());
    }

    @Test
    @DisplayName("metrics_received_and_synced_counts_match")
    void testMetricsReceivedAndSyncedCountsMatch() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-001",
                "Test",
                null,
                null,
                null,
                "Error",
                "NETWORK_ERROR",
                1
        );

        // Act
        for (int i = 0; i < 3; i++) {
            request.setMessageId("msg-" + i);
            mockMvc.perform(
                    post("/messages/dlq/failures")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isAccepted());
        }

        // Assert
        assertEquals(3.0, metrics.getDLQFailuresReceivedCount());
        assertEquals(3.0, metrics.getDLQFailuresSyncedCount());
    }

    @Test
    @DisplayName("metrics_multiple_operations_tracked_independently")
    void testMetricsMultipleOperationsTrackedIndependently() throws Exception {
        // Arrange
        WorkItemRequest workRequest = new WorkItemRequest("What is 2+2?");
        SearchResult resultRequest = new SearchResult();
        resultRequest.setMessageId("msg-001");
        resultRequest.setAnswerFound(true);
        resultRequest.setAnswer("4");
        resultRequest.setConfidence(0.99);
        DLQSyncRequest dlqRequest = new DLQSyncRequest(
                "msg-fail",
                "Failed query",
                null,
                null,
                null,
                "Execution error",
                "SEARCH_EXECUTION_ERROR",
                1
        );

        // Act
        mockMvc.perform(
                post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workRequest))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/messages/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultRequest))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dlqRequest))
        ).andExpect(status().isAccepted());

        // Assert
        assertEquals(1.0, metrics.getWorkItemsSubmittedCount());
        assertEquals(1.0, metrics.getWorkItemsCompletedCount());
        assertEquals(1.0, metrics.getDLQFailuresReceivedCount());
        assertEquals(1.0, metrics.getDLQFailuresSyncedCount());
    }
}
