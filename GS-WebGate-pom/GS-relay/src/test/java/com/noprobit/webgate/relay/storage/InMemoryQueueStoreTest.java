package com.noprobit.webgate.coordinator.storage;

import com.noprobit.webgate.coordinator.models.QueueMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("In-memory queue store - contract tests")
public class InMemoryQueueStoreTest {

    @Test
    @DisplayName("enqueue request returns a request-backed message")
    void enqueueRequestCreatesPendingMessage() {
        InMemoryQueueStore store = new InMemoryQueueStore();

        QueueMessage message = store.enqueueRequest("req-1", "payload-1");

        assertNotNull(message);
        assertEquals("req-1", message.getRequestId());
        assertEquals("payload-1", message.getPayload());
    }

    @Test
    @DisplayName("dequeue request returns the oldest pending message")
    void dequeueRequestReturnsOldestPendingMessage() {
        InMemoryQueueStore store = new InMemoryQueueStore();
        store.enqueueRequest("req-1", "payload-1");
        store.enqueueRequest("req-2", "payload-2");

        QueueMessage first = store.dequeueRequest();
        QueueMessage second = store.dequeueRequest();

        assertNotNull(first);
        assertEquals("req-1", first.getRequestId());
        assertNotNull(second);
        assertEquals("req-2", second.getRequestId());
    }

    @Test
    @DisplayName("enqueue response stores a correlated result")
    void enqueueResponseStoresResponseByRequestId() {
        InMemoryQueueStore store = new InMemoryQueueStore();

        QueueMessage response = store.enqueueResponse("req-1", "result-1");

        assertNotNull(response);
        assertEquals("req-1", response.getRequestId());
        assertTrue(store.hasResponse("req-1"));
    }

    @Test
    @DisplayName("dequeue response returns the right correlated response")
    void dequeueResponseReturnsCorrelatedResponse() {
        InMemoryQueueStore store = new InMemoryQueueStore();
        store.enqueueResponse("req-1", "result-1");

        QueueMessage response = store.dequeueResponse("req-1");

        assertNotNull(response);
        assertEquals("req-1", response.getRequestId());
        assertFalse(store.hasResponse("req-1"));
    }
}
