package com.noprobit.webgate.relay.models;

import java.util.Objects;
import java.util.UUID;

public class QueueMessage {
    private final String id;
    private final String requestId;
    private final String payload;

    public QueueMessage(String requestId, String payload) {
        this.id = UUID.randomUUID().toString();
        this.requestId = requestId;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueMessage that = (QueueMessage) o;
        return Objects.equals(id, that.id)
                && Objects.equals(requestId, that.requestId)
                && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestId, payload);
    }
}
