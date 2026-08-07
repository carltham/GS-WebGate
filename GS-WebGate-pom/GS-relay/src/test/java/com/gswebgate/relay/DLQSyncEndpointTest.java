package com.gswebgate.relay;

import com.gswebgate.relay.contract.DLQSyncRequest;
import com.gswebgate.relay.db.FailedWork;
import com.gswebgate.relay.db.FailedWorkRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Phase 5: DLQ Sync Endpoint Tests")
class DLQSyncEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FailedWorkRepository failedWorkRepository;

    @BeforeEach
    void setUp() {
        failedWorkRepository.deleteAll();
    }

    @Test
    @DisplayName("sync_dlq_failure_creates_new_record")
    void testSyncDLQFailureCreatesNewRecord() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-001",
                "What is the capital of France?",
                "geography",
                "wikipedia",
                "search",
                "Connection timeout after 5000ms",
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
        Optional<FailedWork> created = failedWorkRepository.findByMessageId("msg-001");
        assertTrue(created.isPresent());
        FailedWork failedWork = created.get();
        assertEquals("msg-001", failedWork.getMessageId());
        assertEquals("What is the capital of France?", failedWork.getQuestion());
        assertEquals("TIMEOUT", failedWork.getFailureReason().name());
        assertEquals(1, failedWork.getAttemptCount());
    }

    @Test
    @DisplayName("sync_dlq_failure_updates_existing_record")
    void testSyncDLQFailureUpdatesExistingRecord() throws Exception {
        // Arrange
        // First sync
        DLQSyncRequest request1 = new DLQSyncRequest(
                "msg-002",
                "What is 2+2?",
                null,
                null,
                null,
                "Network error",
                "NETWORK_ERROR",
                1
        );

        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1))
        ).andExpect(status().isAccepted());

        // Second sync with higher attempt count
        DLQSyncRequest request2 = new DLQSyncRequest(
                "msg-002",
                "What is 2+2?",
                null,
                null,
                null,
                "Network error",
                "NETWORK_ERROR",
                2
        );

        // Act
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2))
        ).andExpect(status().isAccepted());

        // Assert
        Optional<FailedWork> updated = failedWorkRepository.findByMessageId("msg-002");
        assertTrue(updated.isPresent());
        assertEquals(2, updated.get().getAttemptCount());
    }

    @Test
    @DisplayName("sync_dlq_failure_with_search_execution_error")
    void testSyncDLQFailureWithSearchExecutionError() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-003",
                "Complex query",
                "context",
                "target",
                "mode",
                "Search executor crashed: NullPointerException",
                "SEARCH_EXECUTION_ERROR",
                3
        );

        // Act
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isAccepted());

        // Assert
        Optional<FailedWork> created = failedWorkRepository.findByMessageId("msg-003");
        assertTrue(created.isPresent());
        assertEquals(FailedWork.FailureReason.SEARCH_EXECUTION_ERROR, created.get().getFailureReason());
        assertEquals(3, created.get().getAttemptCount());
    }

    @Test
    @DisplayName("sync_dlq_failure_with_unknown_reason_defaults_to_unknown")
    void testSyncDLQFailureWithUnknownReasonDefaultsToUnknown() throws Exception {
        // Arrange
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-004",
                "Test query",
                null,
                null,
                null,
                "Some weird error",
                "INVALID_REASON",
                1
        );

        // Act
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isAccepted());

        // Assert
        Optional<FailedWork> created = failedWorkRepository.findByMessageId("msg-004");
        assertTrue(created.isPresent());
        assertEquals(FailedWork.FailureReason.UNKNOWN_ERROR, created.get().getFailureReason());
    }

    @Test
    @DisplayName("sync_multiple_dlq_failures")
    void testSyncMultipleDLQFailures() throws Exception {
        // Arrange & Act
        for (int i = 1; i <= 5; i++) {
            DLQSyncRequest request = new DLQSyncRequest(
                    "msg-" + i,
                    "Question " + i,
                    null,
                    null,
                    null,
                    "Error " + i,
                    "TIMEOUT",
                    1
            );

            mockMvc.perform(
                    post("/messages/dlq/failures")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isAccepted());
        }

        // Assert
        assertEquals(5, failedWorkRepository.count());
    }

    @Test
    @DisplayName("sync_dlq_failure_with_long_error_message")
    void testSyncDLQFailureWithLongErrorMessage() throws Exception {
        // Arrange
        String longError = "A".repeat(1000); // Max length
        DLQSyncRequest request = new DLQSyncRequest(
                "msg-005",
                "Test",
                null,
                null,
                null,
                longError,
                "NETWORK_ERROR",
                1
        );

        // Act
        mockMvc.perform(
                post("/messages/dlq/failures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isAccepted());

        // Assert
        Optional<FailedWork> created = failedWorkRepository.findByMessageId("msg-005");
        assertTrue(created.isPresent());
        assertEquals(longError, created.get().getErrorMessage());
    }
}
