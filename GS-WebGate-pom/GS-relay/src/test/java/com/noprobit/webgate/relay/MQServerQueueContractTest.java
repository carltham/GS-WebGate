package com.noprobit.webgate.relay;

import com.noprobit.webgate.relay.models.QueueMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MQ server queue contract")
public class MQServerQueueContractTest {

    @Test
    @DisplayName("server exposes the request/response queue contract")
    void serverExposesQueueContract() {
        MQServer server = new MQServer(0);

        QueueMessage request = server.enqueueRequest("req-1", "payload-1");
        QueueMessage dequeued = server.dequeueRequest();
        QueueMessage response = server.enqueueResponse("req-1", "result-1");

        assertNotNull(request);
        assertEquals("req-1", dequeued.getRequestId());
        assertEquals("payload-1", dequeued.getPayload());
        assertEquals("req-1", response.getRequestId());
        assertTrue(server.hasResponse("req-1"));
        assertNotNull(server.dequeueResponse("req-1"));
    }
}
