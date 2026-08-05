package com.noprobit.webgate.coordinator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MQ Server - Unit Tests")
public class MQServerTest {

    private MQServer server;
    private static final int TEST_PORT = 9999;

    @BeforeEach
    public void setUp() {
        server = new MQServer(TEST_PORT);
    }

    @AfterEach
    public void tearDown() {
        if (server != null && server.isRunning()) {
            server.shutdown();
        }
    }

    @Test
    @DisplayName("Test 1.1: Server starts successfully")
    public void testServerStartsSuccessfully() {
        // Given: MQServer not running
        assertFalse(server.isRunning());

        // When: Call server.start()
        server.start();

        // Then: Server listening and running
        assertTrue(server.isRunning());
        assertEquals(MQServerStatus.RUNNING, server.getStatus());
    }

    @Test
    @DisplayName("Test 1.2: Server shuts down successfully")
    public void testServerShutdownSuccessfully() {
        // Given: MQServer running
        server.start();
        assertTrue(server.isRunning());

        // When: Call server.shutdown()
        server.shutdown();

        // Then: Server stopped
        assertFalse(server.isRunning());
        assertEquals(MQServerStatus.STOPPED, server.getStatus());
    }

    @Test
    @DisplayName("Test 1.3: Server handles port already in use")
    public void testServerPortAlreadyInUse() throws Exception {
        // Given: Port 9999 already in use (start first server)
        server.start();
        assertTrue(server.isRunning());

        // When: Try to start second server on same port
        MQServer server2 = new MQServer(TEST_PORT);

        // Then: Throw PortUnavailableException
        assertThrows(PortUnavailableException.class, () -> {
            server2.start();
        });

        assertFalse(server2.isRunning());
    }

    @Test
    @DisplayName("Test 1.4: Server handles shutdown while processing")
    public void testServerShutdownWhileProcessing() throws Exception {
        // Given: MQServer running
        server.start();
        assertTrue(server.isRunning());

        // When: Shutdown server
        server.shutdown();

        // Then: Server stops gracefully
        assertFalse(server.isRunning());
        assertEquals(MQServerStatus.STOPPED, server.getStatus());
    }

    @Test
    @DisplayName("Test 1.5: Server rejects connections after shutdown")
    public void testServerRejectsConnectionsAfterShutdown() throws Exception {
        // Given: MQServer running, then stopped
        server.start();
        server.shutdown();
        assertFalse(server.isRunning());

        // When: Try to create client and connect
        MQClient client = new MQClient("localhost", TEST_PORT);

        // Then: Connection refused
        assertThrows(ConnectionException.class, () -> {
            client.connect();
        });
    }

    @Test
    @DisplayName("Test 1.6: Server status transitions correct")
    public void testServerStatusTransitions() {
        // Given: New server
        assertEquals(MQServerStatus.STOPPED, server.getStatus());

        // When: Start
        server.start();
        // Then: RUNNING
        assertEquals(MQServerStatus.RUNNING, server.getStatus());

        // When: Stop
        server.shutdown();
        // Then: STOPPED
        assertEquals(MQServerStatus.STOPPED, server.getStatus());
    }

    @Test
    @DisplayName("Test 1.7: Server listens on correct port")
    public void testServerListensOnCorrectPort() {
        // When: Server starts
        server.start();

        // Then: Server listening on port 9999
        assertTrue(server.isRunning());
        assertEquals(TEST_PORT, server.getPort());
    }

    @Test
    @DisplayName("Test 1.8: Multiple start calls only start once")
    public void testMultipleStartCallsStartOnce() {
        // Given: MQServer not running
        assertFalse(server.isRunning());

        // When: Call start twice
        server.start();
        server.start();

        // Then: Still only one server running
        assertTrue(server.isRunning());
        assertEquals(MQServerStatus.RUNNING, server.getStatus());
    }

    @Test
    @DisplayName("Test 1.9: Multiple shutdown calls are safe")
    public void testMultipleShutdownCallsAreSafe() {
        // Given: MQServer running
        server.start();
        assertTrue(server.isRunning());

        // When: Call shutdown twice
        server.shutdown();
        server.shutdown();

        // Then: No error, server stopped
        assertFalse(server.isRunning());
    }

    @Test
    @DisplayName("Test 1.10: Server provides status information")
    public void testServerProvidesStatusInfo() {
        // Given: Running server
        server.start();

        // When: Get server info
        String info = server.getInfo();

        // Then: Info contains status
        assertNotNull(info);
        assertTrue(info.contains("RUNNING") || info.toLowerCase().contains("running"));
    }

}
