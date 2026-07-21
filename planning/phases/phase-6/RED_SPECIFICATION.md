# Phase 6: RED Specification - Message Queue Infrastructure

**Phase:** 6 - Message Queue Server  
**Test-Driven Development:** RED Phase (Test Specifications)  
**Date:** 2026-07-20

---

## Overview

RED Phase test specifications for Message Queue (MQ) infrastructure. All tests must be written BEFORE implementation begins.

---

## Test Files to Create

```
src/test/java/com/noprobit/mq/
├── MQServerTest.java (Unit tests)
├── MQClientTest.java (Unit tests)
├── MQCommandsIT.java (Integration tests)
├── MQConcurrencyIT.java (Concurrency tests)
└── MQIntegrationModulesIT.java (Module integration tests)
```

---

## Test Specification 1: MQServer - Startup and Shutdown

**File:** `MQServerTest.java`

### Test 1.1: Server starts successfully
```
Given: MQ Server not running
When: Call MQServer.start()
Then: 
  - Server listening on port 9999
  - Server status = RUNNING
  - Accept incoming connections
```

### Test 1.2: Server shuts down successfully
```
Given: MQ Server running
When: Call MQServer.shutdown()
Then:
  - Server stops listening
  - Server status = STOPPED
  - All connections closed
  - In-memory queues cleared
```

### Test 1.3: Server handles port already in use
```
Given: Port 9999 already in use
When: Try to start MQServer
Then:
  - Throw PortUnavailableException
  - Server does not start
  - Clear error message
```

### Test 1.4: Server handles shutdown while processing
```
Given: MQServer running with active requests
When: Call MQServer.shutdown()
Then:
  - Wait for in-flight requests to complete
  - Gracefully close connections
  - Server stops
  - No data loss
```

### Test 1.5: Server rejects connections after shutdown
```
Given: MQServer was running, now shutdown
When: Try to connect
Then:
  - Connection refused
  - Server remains stopped
```

---

## Test Specification 2: MQClient - Connection Management

**File:** `MQClientTest.java`

### Test 2.1: Client connects to server
```
Given: MQServer running on port 9999
When: Create MQClient and call connect()
Then:
  - Connection established
  - Client status = CONNECTED
  - Ready to send commands
```

### Test 2.2: Client disconnects cleanly
```
Given: MQClient connected
When: Call client.disconnect()
Then:
  - Connection closed
  - Client status = DISCONNECTED
  - No lingering connections
```

### Test 2.3: Client reconnects after disconnect
```
Given: MQClient was connected, now disconnected
When: Call client.reconnect()
Then:
  - New connection established
  - Client status = CONNECTED
```

### Test 2.4: Client handles server unavailable
```
Given: MQServer not running
When: Create MQClient and call connect()
Then:
  - Throw ConnectionException
  - Clear error message
  - Client status = DISCONNECTED
```

### Test 2.5: Client connection pooling
```
Given: Multiple MQClient instances
When: All connect to same server
Then:
  - Server handles multiple connections
  - Each client independent
  - Server maintains connection list
```

### Test 2.6: Client timeout on no response
```
Given: MQClient connected to server
When: Send command and server doesn't respond within timeout
Then:
  - Throw TimeoutException
  - Connection still valid
  - Can retry command
```

---

## Test Specification 3: Commands - All 6 Command Types

**File:** `MQCommandsIT.java`

### Test 3.1: ENQUEUE_REQUEST command
```
Given: MQClient connected to server
When: Send enqueue_request with analysis request:
      {
        "command": "enqueue_request",
        "request": {
          "className": "UserController",
          "extendsClass": "BaseController",
          "filePath": "/src/..."
        }
      }
Then:
  - Request stored in server queue
  - Return: {"success": true, "requestId": "uuid"}
  - Request retrievable via dequeue_request
```

### Test 3.2: DEQUEUE_REQUEST command
```
Given: One or more requests in server queue
When: Send dequeue_request command
Then:
  - Return oldest request from queue
  - Request removed from queue
  - Return: {"success": true, "request": {...}}
  - Next dequeue returns next request
```

### Test 3.3: DEQUEUE_REQUEST on empty queue
```
Given: Queue is empty
When: Send dequeue_request
Then:
  - Return: {"success": false, "request": null}
  - No error
  - No blocking (immediate response)
```

### Test 3.4: ENQUEUE_RESPONSE command
```
Given: MQClient connected
When: Send enqueue_response with analysis result:
      {
        "command": "enqueue_response",
        "requestId": "uuid-123",
        "response": {
          "actualName": "UserController",
          "suggestedName": "UserController",
          "purpose": "CONTROLLER",
          "confidence": 0.95
        }
      }
Then:
  - Response stored associated with requestId
  - Return: {"success": true, "responseId": "uuid"}
  - Response retrievable via dequeue_response with requestId
```

### Test 3.5: DEQUEUE_RESPONSE command
```
Given: Response stored for requestId "uuid-123"
When: Send dequeue_response:
      {
        "command": "dequeue_response",
        "requestId": "uuid-123"
      }
Then:
  - Return response associated with requestId
  - Response removed from queue
  - Return: {"success": true, "response": {...}}
```

### Test 3.6: DEQUEUE_RESPONSE for non-existent request
```
Given: No response for requestId "xyz-999"
When: Send dequeue_response for "xyz-999"
Then:
  - Return: {"success": false, "response": null}
  - No error
```

### Test 3.7: HAS_RESPONSE command
```
Given: Response stored for requestId "uuid-123"
When: Send has_response:
      {
        "command": "has_response",
        "requestId": "uuid-123"
      }
Then:
  - Return: {"success": true, "hasResponse": true}
```

### Test 3.8: HAS_RESPONSE for non-existent request
```
Given: No response for requestId "xyz-999"
When: Send has_response for "xyz-999"
Then:
  - Return: {"success": true, "hasResponse": false}
```

### Test 3.9: STATS command
```
Given: Server running with requests/responses
When: Send stats command:
      {
        "command": "stats"
      }
Then:
  - Return: {
      "success": true,
      "stats": {
        "requestQueueSize": 5,
        "responseQueueSize": 3,
        "totalRequests": 25,
        "totalResponses": 20,
        "connectedClients": 2,
        "uptime": 3600000
      }
    }
```

### Test 3.10: Invalid command type
```
Given: MQClient connected
When: Send unknown command type: {"command": "invalid_command"}
Then:
  - Return: {"success": false, "error": "Unknown command"}
  - Connection remains valid
```

### Test 3.11: Malformed JSON command
```
Given: MQClient connected
When: Send invalid JSON: "{broken json"
Then:
  - Return: {"success": false, "error": "Invalid JSON"}
  - Connection remains valid
  - Server doesn't crash
```

### Test 3.12: Missing required fields
```
Given: MQClient connected
When: Send command missing required fields:
      {"command": "enqueue_request"}  (missing "request" field)
Then:
  - Return: {"success": false, "error": "Missing field: request"}
  - Connection valid
```

---

## Test Specification 4: Storage and Data Persistence

**File:** `MQCommandsIT.java`

### Test 4.1: Request survives server restart
```
Given: Request enqueued in server
When: Server shuts down and restarts
Then:
  - (Note: in-memory, so data lost - this test verifies that)
  - Queue is empty after restart
  - Expected behavior documented
```

### Test 4.2: Multiple requests ordered FIFO
```
Given: Empty queue
When: Enqueue request A, B, C in order
  And: Dequeue 3 times
Then:
  - First dequeue returns A
  - Second dequeue returns B
  - Third dequeue returns C
```

### Test 4.3: Response associated with correct request
```
Given: Multiple requests with IDs: req-1, req-2, req-3
When: Enqueue responses for req-2 and req-1 (out of order)
  And: Dequeue response for req-1
Then:
  - Returns response for req-1
  - Response for req-2 still available
  - No mixing of responses
```

### Test 4.4: Queue size accurate
```
Given: Empty queue
When: Enqueue 5 requests
Then:
  - stats.requestQueueSize = 5
  After: Dequeue 2 requests
Then:
  - stats.requestQueueSize = 3
```

---

## Test Specification 5: TTL and Auto-Cleanup

**File:** `MQCommandsIT.java`

### Test 5.1: Old responses auto-cleanup
```
Given: Response enqueued with TTL = 5 seconds
When: Wait 6 seconds
Then:
  - Response removed from queue
  - has_response returns false
  - dequeue_response returns null
```

### Test 5.2: TTL countdown
```
Given: Response with TTL = 3 seconds enqueued at T=0
When: Check at T=1 second
Then:
  - Response still in queue
  When: Check at T=4 seconds
Then:
  - Response removed
```

### Test 5.3: TTL doesn't affect requests
```
Given: Request enqueued (no TTL)
When: Wait 10 seconds
Then:
  - Request still in queue
  - No auto-cleanup for requests
```

### Test 5.4: TTL refresh on access
```
Given: Response with TTL = 5 seconds
When: Access response at T=3 seconds
Then:
  - (Depends on design - specify if TTL refreshes)
  - If yes: Response available until T=8
  - If no: Response removed at T=5
```

---

## Test Specification 6: Error Handling

**File:** `MQCommandsIT.java`

### Test 6.1: Server error response format
```
When: Any error occurs
Then: Return standard format:
      {
        "success": false,
        "error": "error message",
        "code": "ERROR_CODE"
      }
```

### Test 6.2: Client reconnect on connection loss
```
Given: MQClient connected
When: Server-side connection closes
Then:
  - Client detects disconnect
  - Automatic reconnect attempt
  - Retry command if enabled
```

### Test 6.3: Handle very large request
```
Given: MQClient connected
When: Enqueue very large request (>1MB)
Then:
  - Either: Accept and store
  - Or: Return size limit error
  - Spec: Decide max size
```

### Test 6.4: Handle rapid fire commands
```
Given: MQClient connected
When: Send 100 commands rapidly (no wait)
Then:
  - All processed
  - None lost
  - None corrupted
  - Server remains stable
```

---

## Test Specification 7: Concurrency

**File:** `MQConcurrencyIT.java`

### Test 7.1: Multiple clients concurrent write
```
Given: 3 MQClients connected
When: All send enqueue_request simultaneously
Then:
  - All succeed
  - All requests in queue
  - No data corruption
  - No race conditions
```

### Test 7.2: Multiple clients concurrent read
```
Given: Queue with 5 requests, 3 clients connected
When: All clients dequeue simultaneously
Then:
  - Each gets different request (or null)
  - No request returned twice
  - No data corruption
```

### Test 7.3: Concurrent read and write
```
Given: 2 clients, one writing, one reading
When: Simultaneously enqueue and dequeue
Then:
  - Both succeed
  - No deadlock
  - No data corruption
```

### Test 7.4: Thread safety of stats
```
Given: Server under concurrent load
When: Multiple clients query stats simultaneously
Then:
  - All get consistent stats
  - No race conditions
  - Stats accurate
```

### Test 7.5: Stress test: 1000 requests
```
Given: MQServer running
When: Send 1000 enqueue_request commands
Then:
  - All succeed
  - stats.requestQueueSize = 1000
  - Server remains stable
  - Performance acceptable (<5s)
```

---

## Test Specification 8: Integration with Modules

**File:** `MQIntegrationModulesIT.java`

### Test 8.1: JAR module sends request via MQ
```
Given: JAR module and MQ server running
When: JAR module enqueues analysis request
Then:
  - Request in MQ queue
  - Has correct format
  - Retrievable by other modules
```

### Test 8.2: WebGate reads request and sends response
```
Given: Request in MQ queue
When: WebGate client dequeues request
  And: Processes it
  And: Enqueues response with correct requestId
Then:
  - Response associated with request
  - UI module can retrieve it
  - Response has correct format
```

### Test 8.3: UI module retrieves response
```
Given: Response in MQ queue for requestId "uuid-123"
When: UI module calls has_response("uuid-123")
Then:
  - Returns true
  When: UI module calls dequeue_response("uuid-123")
Then:
  - Returns response
  - Can use immediately
```

### Test 8.4: End-to-end request/response cycle
```
Given: All modules running (UI, JAR, WebGate, MQ)
When: UI enqueues analysis request
  And: JAR dequeues and processes
  And: WebGate enqueues response
  And: UI retrieves response
Then:
  - Response matches request
  - No data loss
  - Timing acceptable (<5s)
```

---

## Test Specification 9: Protocol and Format

**File:** `MQCommandsIT.java`

### Test 9.1: JSON over TCP protocol
```
Protocol: JSON over TCP
Port: 9999
Format: One JSON object per line, terminated with \n

Example:
{"command":"enqueue_request","request":{...}}\n
```

### Test 9.2: Request format validation
```
Valid request:
{
  "command": "enqueue_request",
  "request": {
    "className": "string",
    "extendsClass": "string (optional)",
    "filePath": "string",
    "methods": ["string"],
    "imports": ["string"]
  }
}
```

### Test 9.3: Response format validation
```
Valid response:
{
  "command": "enqueue_response",
  "requestId": "uuid",
  "response": {
    "actualName": "string",
    "suggestedName": "string",
    "purpose": "string",
    "confidence": "number (0.0-1.0)"
  }
}
```

### Test 9.4: Error response format
```
{
  "success": false,
  "error": "error description",
  "code": "ERROR_CODE"
}
```

---

## Test Execution

### Command to Run Unit Tests
```bash
mvn test
```

### Command to Run Integration Tests
```bash
mvn test -P integration
```

### Command to Run All Tests
```bash
mvn test -P all-tests
```

---

## Expected Coverage

- **Unit Tests:** 80%+ code coverage
- **Integration Tests:** 90%+ coverage of command paths
- **Concurrency Tests:** All race condition scenarios
- **Module Integration:** Full end-to-end workflows

---

## Acceptance Criteria

All tests written BEFORE implementation:
- [ ] MQServerTest.java: 5 tests
- [ ] MQClientTest.java: 6 tests
- [ ] MQCommandsIT.java: 30+ tests
- [ ] MQConcurrencyIT.java: 5 tests
- [ ] MQIntegrationModulesIT.java: 4 tests
- [ ] **Total: 50+ tests** (RED phase)

All tests must FAIL before implementation (TDD principle).

---

## Next Phase

→ **GREEN Phase:** Implement MQServer, MQClient, all commands to make tests pass

---

**Status:** RED Specification Complete. Ready for implementation.
