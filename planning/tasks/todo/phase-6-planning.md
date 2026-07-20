# Phase 6: Task 1 - Planning & Setup

**Phase:** 6 - Message Queue Infrastructure  
**Task:** 6.1 - Planning & Setup  
**Estimated Hours:** 3  
**Status:** ⬜ Not Started

---

## Overview

Plan and set up Phase 6 (Message Queue) implementation. Define requirements, architecture, test specifications, and directory structure.

---

## Checklist

- [ ] Review existing MQ_ARCHITECTURE.md documentation
- [ ] Define Phase 6 scope and requirements
- [ ] Design MQ server architecture
- [ ] Design MQ client API
- [ ] Identify test specifications needed
- [ ] Create test specification document (RED_SPECIFICATION.md)
- [ ] Create directory structure for Phase 6
- [ ] Set up Maven module (TextAnalyser-mq)
- [ ] Plan integration with existing modules

---

## Details

### Phase 6 Overview: Message Queue Infrastructure

**Purpose:** Implement decoupled communication between jar and webgate modules via a central Message Queue Server.

**Rationale:**
- Current: Direct HTTP calls between modules
- Problem: Tight coupling, single point of failure
- Solution: Asynchronous message-based communication

### MQ Server Architecture

**Technology:** TCP-based JSON protocol (no external dependencies)

**Components:**
1. **MQ Server** - Central message broker
   - Listens on TCP port 9999
   - In-memory storage
   - TTL-based auto-cleanup
   - JSON over TCP protocol

2. **MQ Client** - Library for connecting to MQ Server
   - Used by jar and webgate modules
   - Connection pooling
   - Request/response handling
   - Error handling and retries

3. **MQ Commands** - 6 command types
   - `enqueue_request` - Add analysis request
   - `dequeue_request` - Retrieve analysis request
   - `enqueue_response` - Store analysis result
   - `dequeue_response` - Retrieve analysis result
   - `has_response` - Check if result ready
   - `stats` - Get queue statistics

### Deployment Architecture

```
UI Module (localhost:UI_PORT)
  ├─→ MQ Client
      └─→ MQ Server (localhost:9999)
          ├─→ Request Queue
          ├─→ Response Queue
          └─→ Stats

JAR Module (localhost:8081)
  ├─→ MQ Client
      └─→ MQ Server (localhost:9999)

WebGate Module (localhost:8080)
  ├─→ MQ Client
      └─→ MQ Server (localhost:9999)
```

### Directory Structure to Create

```
TextAnalyser-pom/
└── TextAnalyser-mq/
    ├── pom.xml (Maven configuration)
    ├── src/
    │   ├── main/java/com/noprobit/mq/
    │   │   ├── MQServer.java (TCP server)
    │   │   ├── MQClient.java (TCP client)
    │   │   ├── commands/
    │   │   │   ├── EnqueueRequestCommand.java
    │   │   │   ├── DequeueRequestCommand.java
    │   │   │   ├── EnqueueResponseCommand.java
    │   │   │   ├── DequeueResponseCommand.java
    │   │   │   ├── HasResponseCommand.java
    │   │   │   └── StatsCommand.java
    │   │   ├── models/
    │   │   │   ├── Message.java
    │   │   │   ├── Request.java
    │   │   │   ├── Response.java
    │   │   │   └── MQStats.java
    │   │   └── storage/
    │   │       ├── InMemoryQueue.java
    │   │       └── TTLManager.java
    │   └── test/java/com/noprobit/mq/
    │       ├── MQServerTest.java
    │       ├── MQClientTest.java
    │       └── MQCommandsIT.java
    └── README.md (MQ module documentation)
```

### Test Specifications (to be created)

**RED_SPECIFICATION.md** should define:
- Server startup/shutdown tests
- Client connection tests
- Command parsing tests (6 command types)
- Request/response storage tests
- TTL cleanup tests
- Error handling tests
- Concurrency tests
- Integration with jar/webgate modules

### Integration Strategy

**Phase 6a: MQ Server Implementation**
- Build TCP server
- Implement in-memory storage
- Implement 6 commands
- Write unit tests

**Phase 6b: MQ Client Implementation**
- Build TCP client library
- Connection management
- Request/response handling
- Error handling and retries

**Phase 6c: Module Integration**
- Update jar module to use MQ (instead of direct HTTP)
- Update webgate to use MQ if needed
- Write integration tests
- Performance testing

---

## Acceptance Criteria

- [ ] Phase 6 requirements clearly defined
- [ ] MQ architecture documented
- [ ] Directory structure planned
- [ ] Test specifications created (RED_SPECIFICATION.md)
- [ ] Maven module ready
- [ ] Team aligned on approach
- [ ] Ready to begin RED phase (writing tests)

---

## References

- `/TextAnalyser/architecture/MQ_ARCHITECTURE.md` - Planned MQ architecture
- `/planning/phases/phase-6/` (to be created)
- Current JAR module HTTP server implementation
- WebGate Spring Boot implementation

---

## Next Step

→ Create **6.2-write-tests-red.md** in phase-6/ folder
