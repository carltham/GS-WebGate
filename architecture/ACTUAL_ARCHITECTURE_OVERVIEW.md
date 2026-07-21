# WebGate & MQ: Architecture Overview

**Version:** 1.0 (WebGate/MQ focused)  
**Last Updated:** 2026-07-20  
**Status:** Production-Ready (WebGate Phase 2), Planned (MQ Phase 6+)

---

## What WebGate & MQ Are

**WebGate** is a **standalone internet search gateway service** that:
- Performs safe internet searches via DuckDuckGo API
- Verifies class purposes detected by the JAR analysis module
- Answers generic questions with confidence scoring
- Communicates via REST API (synchronous, direct)
- Runs on local computers (behind NAT, no incoming connections needed)
- Provides optional verification to enhance analysis confidence

**MQ (Message Queue)** is a **planned infrastructure component** that will:
- Enable asynchronous communication between JAR and WebGate
- Run on-site (data center) to receive requests from JAR
- Store requests/responses with TTL-based auto-cleanup
- Allow WebGate to poll for work (no incoming connections)
- Support multiple WebGate instances for load distribution
- Planned for Phase 6+

---

## Architecture Diagram

### Current (Phase 0-5): REST-Based Communication

```
┌─────────────────────────────────────┐
│    On-Site Data Center              │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  JAR Module (port 8081)      │  │
│  │  - Analysis engine           │  │
│  │  - Detects class purposes    │  │
│  │  - (optional) Calls WebGate  │  │
│  └────────────────┬─────────────┘  │
│                   │                 │
│                   │ HTTP REST       │
│                   │ (optional)      │
└───────────────────┼─────────────────┘
                    │
            Internet/VPN (optional)
                    │
┌───────────────────┼─────────────────┐
│  Local Computer   │                 │
│  (Behind NAT)     ↓                 │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  WebGate (port 8080)         │  │
│  │  - Receives REST requests    │  │
│  │  - Calls DuckDuckGo          │  │
│  │  - Returns confidence scores │  │
│  └──────────────┬───────────────┘  │
│                 │                   │
│                 │ HTTPS             │
│                 ↓                   │
│          DuckDuckGo API             │
└─────────────────────────────────────┘
```

### Planned (Phase 6+): Message Queue Communication

```
┌─────────────────────────────────────┐
│    On-Site Data Center              │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  JAR Module (port 8081)      │  │
│  │  - Enqueues requests to MQ   │  │
│  └────────────────┬─────────────┘  │
│                   │                 │
│                   │ TCP (port 7000) │
│                   ↓                 │
│  ┌──────────────────────────────┐  │
│  │  MQ Server (port 7000)       │  │
│  │  - Request queue (FIFO)      │  │
│  │  - Response storage (HashMap)│  │
│  │  - Auto-cleanup (30s TTL)    │  │
│  └────────────────┬─────────────┘  │
│                   │                 │
│                   │ TCP Connection  │
└───────────────────┼─────────────────┘
                    │
            Internet/VPN
                    │
┌───────────────────┼─────────────────┐
│  Local Computer   │                 │
│  (Behind NAT)     ↓                 │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  WebGate (port 8080)         │  │
│  │  - Polls MQ (every 500ms)    │  │
│  │  - Dequeues requests         │  │
│  │  - Processes via DuckDuckGo  │  │
│  │  - Enqueues responses        │  │
│  └──────────────┬───────────────┘  │
│                 │                   │
│                 │ HTTPS             │
│                 ↓                   │
│          DuckDuckGo API             │
└─────────────────────────────────────┘
```

---

## Key Components

### WebGate Module

**Location:** `/TextAnalyser-pom/TextAnalyser-webgate/`  
**Technology:** Spring Boot 2.7.14, Java 11  
**Port:** 8080  
**External Dependency:** DuckDuckGo (public, no auth required)

**Responsibilities:**
- Accept REST requests for search/verification
- Query DuckDuckGo API over HTTPS
- Calculate confidence scores
- Parse and format responses
- Return answers with source attribution

**REST Endpoints:**
```
POST /webgate/api/verify-purpose    - Verify detected class purpose
POST /webgate/api/query              - Answer generic questions
GET  /webgate/health                 - Health check
```

### MQ Module (Phase 6+)

**Location:** `/TextAnalyser-pom/TextAnalyser-mq/`  
**Technology:** Java 11, TCP server  
**Port:** 7000  
**No External Dependencies**

**Responsibilities:**
- Accept TCP connections from JAR and WebGate
- Manage FIFO request queue
- Store responses by requestId
- Handle command-based protocol (JSON over TCP)
- Auto-cleanup expired messages (30s TTL)
- Print statistics every 5 seconds

**Commands Supported:**
```
enqueue_request   - JAR: add search request to queue
dequeue_request   - WebGate: get next request
enqueue_response  - WebGate: store result in MQ
dequeue_response  - JAR: retrieve result
has_response      - JAR: check if result ready
stats             - Get queue statistics
```

---

## Communication Patterns

### Pattern 1: Synchronous REST (Current)

```
Timeline: JAR requests verification
  
  T=0ms   JAR: "Verify that UserController is a CONTROLLER"
          └─→ POST to http://localhost:8080/webgate/api/verify-purpose
          
  T=50ms  WebGate: Receives request
          └─→ Calls DuckDuckGo API
          
  T=300ms WebGate: "Verified, confidence 0.92"
          └─→ Returns to JAR
          
  T=350ms JAR: Updates confidence score
          └─→ Continues analysis
```

**Characteristics:**
- Synchronous (JAR waits for response)
- Direct connection (must be reachable)
- Simple protocol (HTTP/REST)
- Low latency (<500ms typical)
- Gracefully degrades if WebGate unavailable

### Pattern 2: Asynchronous Polling (Planned)

```
Timeline: JAR enqueues verification

  T=0ms   JAR: "Need verification for UserController"
          └─→ Enqueue to MQ (TCP)
          └─→ Returns immediately
          
  T=100ms WebGate: Polls MQ
          └─→ Dequeue request
          └─→ Calls DuckDuckGo API
          
  T=350ms WebGate: Completes search
          └─→ Enqueue response to MQ
          
  T=400ms JAR: Polls MQ for response
          └─→ Has response? Yes
          └─→ Dequeue response
          
  T=450ms JAR: Updates confidence
          └─→ Continues analysis
```

**Characteristics:**
- Asynchronous (JAR doesn't wait)
- Decoupled (services independent)
- Polling-based (no incoming connections)
- Handles temporary outages gracefully
- Supports multiple WebGate instances

---

## Deployment Scenarios

### Development (All Local)

```bash
Laptop:
├─ MQ Server (port 7000) - optional, for MQ testing
├─ JAR Module (port 8081) - mvn -pl TextAnalyser-jar spring-boot:run
├─ WebGate (port 8080) - mvn -pl TextAnalyser-webgate spring-boot:run
└─ UI (GUI) - mvn -pl TextAnalyser-UI-swing exec:java
```

### Production (Distributed)

```
On-Site Data Center:
├─ MQ Server (port 7000) - TCP server for message queue
├─ JAR Module (port 8081) - Analysis engine

Local Computer (Behind NAT):
└─ WebGate (port 8080) - Can be multiple instances

Internet:
└─ DuckDuckGo (api.duckduckgo.com) - Public API, no auth
```

---

## Design Principles

### 1. No Direct Internet Calls from Server
- JAR module NEVER calls DuckDuckGo directly
- WebGate ALWAYS handles internet communication
- Prevents server IP from being blocked

### 2. Works Behind NAT
- WebGate uses polling (no incoming connections needed)
- No firewall rules required
- Portable to any network

### 3. Graceful Degradation
- Analysis works without WebGate (local results only)
- Verification is enhancement, not requirement
- Missing MQ doesn't prevent REST-based verification

### 4. Simple Protocols
- REST: Standard HTTP/JSON
- MQ: TCP/JSON (easy to debug)
- No complex serialization

### 5. Confidence Scoring
- All results include confidence (0.0-1.0)
- Allows consumers to make informed decisions
- Transparent quality metrics

---

## Key Metrics

### WebGate Performance
- Response time: <500ms typical
- DuckDuckGo timeout: 10 seconds default (configurable)
- Answer types supported: instant, abstract, related topics
- Confidence scoring: 0.95 (direct) → 0.20 (no match)

### MQ Performance
- Message latency: <1ms
- Throughput: ~1000 msg/sec (per DuckDuckGo limits)
- Storage: In-memory, ~10-20MB per 1000 messages
- Cleanup: Automatic (30-second TTL)

---

## Configuration

### WebGate Settings

```yaml
server.port: 8080
server.servlet.context-path: /webgate
search.duckduckgo.enabled: true
search.duckduckgo.timeout-ms: 10000
search.duckduckgo.endpoint: https://api.duckduckgo.com/
```

### MQ Settings

```yaml
mq.host: on-site-server.example.com
mq.port: 7000
mq.connection-timeout-ms: 10000
mq.read-timeout-ms: 30000
```

---

## Testing Strategy

### Unit Tests
- DuckDuckGo response parsing
- Confidence calculation
- JSON serialization/deserialization
- TCP protocol handling

### Layer Tests
- REST endpoint handling
- MQ command processing
- Client-server communication

### Integration Tests
- End-to-end with real DuckDuckGo API
- MQ request/response cycle
- Network timeout scenarios
- Multiple client coordination

---

## Summary

**WebGate & MQ provide:**

| Aspect | Benefit |
|--------|---------|
| **Safety** | No direct internet calls from server |
| **Reliability** | Works without WebGate (graceful degradation) |
| **Portability** | Works behind NAT, firewall, VPN |
| **Transparency** | Confidence scores show result quality |
| **Simplicity** | REST + JSON, easy to debug |
| **Scalability** | MQ supports load distribution |

**Current Status:**
- ✅ WebGate (REST API) - Production ready
- 📋 MQ (Message Queue) - Planned for Phase 6+

**Next Phase:**
- Implement MQ server (TCP + JSON protocol)
- Add MQ polling to WebGate
- Support async verification flow
- Enable multiple WebGate instances
