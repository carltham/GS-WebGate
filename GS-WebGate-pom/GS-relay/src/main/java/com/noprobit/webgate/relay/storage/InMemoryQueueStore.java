package com.noprobit.webgate.relay.storage;

import com.noprobit.webgate.relay.models.QueueMessage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryQueueStore {
    private final Deque<QueueMessage> pendingRequests = new ArrayDeque<>();
    private final Map<String, QueueMessage> responsesByRequestId = new LinkedHashMap<>();

    public QueueMessage enqueueRequest(String requestId, String payload) {
        QueueMessage message = new QueueMessage(requestId, payload);
        pendingRequests.addLast(message);
        return message;
    }

    public QueueMessage dequeueRequest() {
        return pendingRequests.pollFirst();
    }

    public QueueMessage enqueueResponse(String requestId, String payload) {
        QueueMessage message = new QueueMessage(requestId, payload);
        responsesByRequestId.put(requestId, message);
        return message;
    }

    public QueueMessage dequeueResponse(String requestId) {
        return responsesByRequestId.remove(requestId);
    }

    public boolean hasResponse(String requestId) {
        return responsesByRequestId.containsKey(requestId);
    }
}
