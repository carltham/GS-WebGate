# WebGate & MQ: Module Architecture

**Version:** 1.0 (WebGate/MQ focused)  
**Date:** 2026-07-20  

---

## Module 1: WebGate - Spring Boot Gateway

**Location:** `/GS-WebGate/GS-WebGate/`  
**Purpose:** Internet search verification and generic query service  
**Port:** 8080  
**Technology:** Spring Boot, REST API, DuckDuckGo  
**Deployment:** Standalone JAR, works behind NAT

### Package Structure

```
com.noprobit.analyzers.webgate/
├── WebGateApplication                   # Spring Boot entry point
├── PurposeVerificationController        # REST endpoints
│   ├── POST /api/verify-purpose
│   └── POST /api/query (generic queries)
├── InternetSearchService                # DuckDuckGo integration
├── QueryRequest                         # Request model
├── QueryResponse                        # Response model
└── SearchResult                         # Search result model
```

### Core Classes

#### WebGateApplication
**Responsibility:** Spring Boot application bootstrap

```java
@SpringBootApplication
public class WebGateApplication {
    // Embedded Tomcat on port 8080
    // Context path: /webgate
    // Auto-enables scheduling for polling (Phase 6+)
}
```

#### PurposeVerificationController
**Responsibility:** Handle REST requests for verification and queries

```java
@RestController
@RequestMapping("/api")
public class PurposeVerificationController {

    @PostMapping("/verify-purpose")
    public RemoteVerificationResult verifyPurpose(
        String className,
        String detectedPurpose,
        String keyword
    )
    // Verify if detected purpose makes sense
    // Query DuckDuckGo for confirmation
    // Return confidence score

    @PostMapping("/query")
    public QueryResponse queryGeneric(QueryRequest request)
    // Answer general knowledge questions
    // Support both technical and general queries
    // Return answer with confidence

    @GetMapping("/health")
    public String health()
    // Health check endpoint for monitoring
}
```

#### InternetSearchService
**Responsibility:** Query DuckDuckGo API and score results

```java
public class InternetSearchService {

    public RemoteVerificationResult verifyPurpose(
        String className,
        String detectedPurpose,
        String keyword
    )
    // Build: "ClassName + keyword + pattern"
    // Query: DuckDuckGo
    // Score: confidence based on result type
    // Return: RemoteVerificationResult

    public QueryResponse queryGeneric(QueryRequest request)
    // Build: question + context
    // Query: DuckDuckGo API
    // Extract: instant answer, abstract, or related topics
    // Score: confidence (0.95 → 0.20)
    // Return: QueryResponse with sources
}
```

### REST Endpoints

#### Purpose Verification

```
POST /webgate/api/verify-purpose
Content-Type: application/json

Request:
{
  "className": "UserController",
  "detectedPurpose": "CONTROLLER",
  "keyword": "controller"
}

Response:
{
  "className": "UserController",
  "purpose": "CONTROLLER",
  "verified": true,
  "confidence": 0.92,
  "sources": ["DuckDuckGo Instant Answer"]
}
```

#### Generic Queries

```
POST /webgate/api/query
Content-Type: application/json

Request:
{
  "question": "What is REST API?",
  "context": "java spring boot",
  "maxResults": 5,
  "timeout": 5000
}

Response:
{
  "question": "What is REST API?",
  "answerFound": true,
  "answer": "REST is an architectural style for web services...",
  "confidence": 0.85,
  "summary": "Direct answer found",
  "processingTime": 245,
  "sources": ["DuckDuckGo", "Wikipedia"]
}
```

#### Health Check

```
GET /webgate/health
Content-Type: application/json

Response:
{
  "status": "UP",
  "version": "2.0",
  "mqConnected": true,
  "processingRequests": 2,
  "completedRequests": 1245,
  "uptime": 86400000
}
```

### Configuration

**File:** `application.yml` or `application.properties`

```yaml
spring:
  application:
    name: GS-WebGate

server:
  port: 8080
  servlet:
    context-path: /webgate

# DuckDuckGo Integration
search:
  duckduckgo:
    enabled: true
    url: https://api.duckduckgo.com/
    timeout-ms: 10000
    user-agent: GS-WebGate/2.0

# Optional: Message Queue (Phase 6+)
mq:
  host: on-site-server.example.com
  port: 7000
  connection-timeout-ms: 10000
  read-timeout-ms: 30000

logging:
  level:
    root: INFO
    com.noprobit.analyzers.webgate: DEBUG
```

### Data Models

**RemoteVerificationResult** (Purpose verification response)
```java
String className          // The class being verified
String purpose           // Verified purpose (CONTROLLER, SERVICE, etc.)
boolean verified         // Whether verification succeeded
double confidence        // Confidence score (0.0-1.0)
List<String> sources     // Where answer came from
```

**QueryRequest** (Generic query request)
```java
String question          // Required: the question to answer
String context          // Optional: contextual info (e.g., "java spring boot")
int maxResults          // Optional: max results to return (default 5)
long timeout            // Optional: timeout in ms (default 5000)
```

**QueryResponse** (Generic query response)
```java
String question         // The original question
boolean answerFound     // Whether an answer was found
String answer          // The answer text
double confidence      // Confidence score (0.0-1.0)
String summary         // Summary of answer type (e.g., "Direct answer found")
long processingTime    // How long the search took (ms)
List<String> sources   // Attribution sources
```

### DuckDuckGo Integration

**Answer Type Confidence Scoring:**
```
Instant Answer (direct, exact)
  └─→ Confidence: 0.90-1.0
  
Abstract (summary, overview)
  └─→ Confidence: 0.75-0.90
  
Related Topics (tangential)
  └─→ Confidence: 0.60-0.75
  
No Results
  └─→ Confidence: 0.0-0.20
```

**Search Flow:**
```
1. Build query: question + context keywords
2. Call: HTTPS GET to api.duckduckgo.com/?q=...
3. Parse: JSON response
4. Extract: Best available answer type
5. Score: Based on answer type and relevance
6. Return: QueryResponse with confidence
```

### Testing

**Unit Tests:**
- `InternetSearchServiceTest` - DuckDuckGo response parsing
- `PurposeVerificationTest` - Confidence calculation
- `GenericQueryServiceTest` - Query building and formatting

**Layer Tests:**
- `PurposeVerificationControllerLT` - REST endpoint handling
- `GenericQueryControllerLT` - JSON serialization

**Integration Tests:**
- `PurposeVerificationIT` - End-to-end with DuckDuckGo
- `GenericQueryServiceIT` - Full query pipeline
- `DuckDuckGoIntegrationIT` - API availability and response parsing

---

## Module 2: MQ - Message Queue Server

**Location:** `/GS-WebGate/GS-mq/`  
**Status:** Planned for Phase 6+  
**Purpose:** Decoupled communication between JAR and WebGate  
**Technology:** TCP-based JSON protocol  
**Port:** 7000 (default, configurable)

### Architecture Overview

**MQ Server:**
```
TCP Server (port 7000)
├─→ Accepts client connections (JAR, WebGate)
├─→ Per-client thread handlers
├─→ Shared MessageStore (thread-safe)
└─→ Auto-cleanup (TTL-based expiration)

MessageStore:
├─→ Request Queue (FIFO)
├─→ Response Storage (HashMap by requestId)
└─→ Automatic cleanup (30-second TTL)
```

### Core Components

```
MQServer.java
├─→ TCP ServerSocket on port 7000
├─→ Accept client connections
├─→ Spawn ClientHandler per connection
└─→ Print statistics every 5 seconds

ClientHandler.java (per-client thread)
├─→ Read JSON commands from client
├─→ Dispatch to command handlers
├─→ Send JSON responses
└─→ Handle client disconnection

MessageStore.java (thread-safe)
├─→ enqueueRequest(id, request)
├─→ dequeueRequest()
├─→ enqueueResponse(id, response)
├─→ dequeueResponse(id)
├─→ hasResponse(id)
├─→ cleanup() - auto-remove expired
└─→ getStats()
```

### Protocol: TCP Commands

#### Command 1: Enqueue Request
```json
{
  "command": "enqueue_request",
  "payload": {
    "id": "req-12345",
    "question": "What is REST API?",
    "context": "java spring boot"
  }
}

Response: { "status": "ok", "message": "Request enqueued: req-12345" }
```

#### Command 2: Dequeue Request
```json
{
  "command": "dequeue_request",
  "payload": {}
}

Response (if available):
{
  "status": "ok",
  "data": {
    "id": "req-12345",
    "question": "What is REST API?",
    "context": "java spring boot"
  }
}

Response (if empty):
{ "status": "empty" }
```

#### Command 3: Enqueue Response
```json
{
  "command": "enqueue_response",
  "payload": {
    "id": "req-12345",
    "queryResponse": {
      "question": "What is REST API?",
      "answerFound": true,
      "answer": "REST is an architectural style...",
      "confidence": 0.85,
      "summary": "Direct answer found",
      "processingTime": 250,
      "sources": ["DuckDuckGo"]
    }
  }
}

Response: { "status": "ok", "message": "Response enqueued: req-12345" }
```

#### Command 4: Dequeue Response
```json
{
  "command": "dequeue_response",
  "payload": {
    "requestId": "req-12345"
  }
}

Response: { "status": "ok", "data": { ...QueryResponse... } }
```

#### Command 5: Has Response
```json
{
  "command": "has_response",
  "payload": {
    "requestId": "req-12345"
  }
}

Response: { "status": "ok", "has_response": true }
```

#### Command 6: Statistics
```json
{
  "command": "stats",
  "payload": {}
}

Response:
{
  "status": "ok",
  "stats": {
    "totalMessages": 145,
    "pendingRequests": 3,
    "pendingResponses": 12,
    "timestamp": 1721475600000
  }
}
```

### Message Lifecycle

```
1. JAR enqueues request
   └─→ Status: "pending"

2. WebGate dequeues request
   └─→ Status: "processing"

3. WebGate processes (calls DuckDuckGo)

4. WebGate enqueues response
   └─→ Status: "completed"

5. JAR retrieves response
   └─→ Status: "delivered"
   └─→ Message deleted from storage

6. Auto-cleanup (30s TTL)
   └─→ Expired messages removed
```

### Features

- **In-Memory Storage** - No database required
- **Thread-Safe** - ConcurrentHashMap with synchronized operations
- **Auto-Cleanup** - TTL-based message expiration (30 seconds)
- **Statistics** - Per-command metrics (5-second reporting)
- **Multiple Clients** - Handles JAR + multiple WebGate instances
- **Simple Protocol** - JSON over TCP, easy to debug

### Deployment

**Docker:**
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /mq
COPY target/GS-mq-1.0-SNAPSHOT.jar mq-server.jar
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "mq-server.jar"]
```

**Run:**
```bash
mvn -pl GS-mq package
java -jar GS-mq/target/GS-mq-1.0-SNAPSHOT.jar
```

---

## Module Interactions

### Current (Phase 0-5): Direct REST

```
JAR Module (port 8081)
  ├─→ Calls WebGate REST API (optional)
  │   └─→ POST http://localhost:8080/webgate/api/verify-purpose
  └─→ DuckDuckGo (if verification enabled)

WebGate Module (port 8080)
  └─→ Calls DuckDuckGo API
      └─→ HTTPS GET https://api.duckduckgo.com/
```

### Planned (Phase 6+): Via Message Queue

```
JAR Module
  └─→ Enqueue request to MQ (port 7000)
  
MQ Server
  ├─→ Stores requests (FIFO)
  └─→ Stores responses (by requestId)
  
WebGate Module
  ├─→ Polls MQ for requests (every 500ms)
  └─→ Enqueues response when done
```

---

## Summary

**WebGate Module:**
- Spring Boot REST service
- DuckDuckGo API integration
- Confidence scoring and answer extraction
- Health checks and monitoring
- Works behind NAT (no incoming connections)

**MQ Module:**
- TCP-based message queue
- FIFO request queue
- HashMap response storage
- Auto-cleanup and statistics
- Enables decoupled async communication
- Supports multiple WebGate instances
