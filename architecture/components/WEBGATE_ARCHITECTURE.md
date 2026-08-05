# WebGate - Complete Architecture Documentation

**Standalone Internet Search & Verification Gateway**

**Version:** 2.0  
**Status:** Production Ready  
**Last Updated:** 2026-07-20

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture Diagram](#architecture-diagram)
3. [System Design](#system-design)
4. [Modules & Components](#modules--components)
5. [Communication Protocol](#communication-protocol)
6. [Deployment Architecture](#deployment-architecture)
7. [API Specifications](#api-specifications)
8. [Configuration](#configuration)
9. [Monitoring & Operations](#monitoring--operations)
10. [Security Considerations](#security-considerations)

---

# 1. Project Overview

## 🎯 WebGate Purpose

WebGate is a **standalone internet gateway service** that:
- ✅ Performs safe internet searches (DuckDuckGo API)
- ✅ Verifies class purposes via web patterns
- ✅ Answers generic questions with confidence scoring
- ✅ Communicates via on-site Message Queue (no direct network)
- ✅ Runs on local computers (behind NAT, no IP blocking)
- ✅ Processes search requests from the GS-WebGate JAR module

## 🔍 Key Characteristics

| Aspect | Details |
|--------|---------|
| **Language** | Java 11 |
| **Framework** | Spring Boot 2.7.14 |
| **Port** | 8080 (configurable) |
| **Dependencies** | Gson, Spring Web, RestTemplate |
| **External API** | DuckDuckGo (public, no auth) |
| **Deployment** | Standalone, behind NAT acceptable |
| **Communication** | TCP/MQ with on-site server |
| **IP Blocking Risk** | None (doesn't make direct calls) |

---

# 2. Architecture Diagram

## 🏗️ High-Level Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                    ON-SITE DATA CENTER                         │
│  ┌──────────────┐              ┌──────────────────┐            │
│  │   JAR Module │              │   MQ Server      │            │
│  │  (port 8081) │◄────TCP──────►(port 7000)      │            │
│  │              │              │                  │            │
│  │ • Analysis   │              │ • Request Queue  │            │
│  │ • REST API   │              │ • Response Store │            │
│  └──────────────┘              └────────┬─────────┘            │
│                                         │                      │
│                                   TCP Connection               │
│                                    (Outbound)                  │
└────────────────────────────────────────┼───────────────────────┘
                                         │
                                    Internet/VPN
                                         │
┌────────────────────────────────────────┼───────────────────────┐
│          LOCAL COMPUTER (Behind NAT)   │                       │
│                                        ↓                       │
│  ┌─────────────────────────────────────────────────────┐      │
│  │              WebGate Service (Port 8080)            │      │
│  │                                                     │      │
│  │  ┌─────────────────────────────────────────────┐   │      │
│  │  │  MQ Polling Layer                          │   │      │
│  │  │  • MQPoller.java                           │   │      │
│  │  │  • Polls on-site MQ for requests           │   │      │
│  │  │  • Writes responses back to MQ             │   │      │
│  │  └─────────────────────────────────────────────┘   │      │
│  │           ↓                        ↑                │      │
│  │  ┌─────────────────────────────────────────────┐   │      │
│  │  │  Request Processing Layer                   │   │      │
│  │  │  • SearchRequestProcessor.java              │   │      │
│  │  │  • Calls InternetSearchService              │   │      │
│  │  │  • Queues response to MQ                    │   │      │
│  │  └─────────────────────────────────────────────┘   │      │
│  │           ↓                        ↑                │      │
│  │  ┌─────────────────────────────────────────────┐   │      │
│  │  │  Search Service Layer                       │   │      │
│  │  │  • InternetSearchService.java               │   │      │
│  │  │  • queryGeneric() for questions             │   │      │
│  │  │  • search() for verification                │   │      │
│  │  └─────────────────────────────────────────────┘   │      │
│  │           ↓                        ↑                │      │
│  │  ┌─────────────────────────────────────────────┐   │      │
│  │  │  API Layer (Optional)                       │   │      │
│  │  │  • PurposeVerificationController.java       │   │      │
│  │  │  • REST endpoints for direct access         │   │      │
│  │  └─────────────────────────────────────────────┘   │      │
│  │           ↓                                         │      │
│  │  ┌─────────────────────────────────────────────┐   │      │
│  │  │  External Services                          │   │      │
│  │  │  • DuckDuckGo API                           │   │      │
│  │  │  • RestTemplate (HTTP calls)                │   │      │
│  │  └─────────────────────────────────────────────┘   │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                │
│  WebGate runs here → Can safely make internet calls           │
│  (Local IP, single machine, won't trigger rate limits)        │
└────────────────────────────────────────────────────────────────┘
```

## 📡 Communication Flow

```
Timeline: User Requests Analysis

T=0ms     JAR Module
          └─ Needs verification for "UserController"
          └─ Writes to on-site MQ: "search UserController"

T=100ms   WebGate Polling
          └─ Polls on-site MQ: "Any requests for me?"
          └─ Receives: "search UserController"
          └─ Marks as "processing"

T=150ms   WebGate Processing
          └─ Calls InternetSearchService.queryGeneric()
          └─ Builds query: "UserController purpose class"
          └─ Calls DuckDuckGo API (SAFE - local IP)

T=400ms   WebGate Response
          └─ DuckDuckGo returns: "Controller handles HTTP requests"
          └─ Calculates confidence: 0.85
          └─ Writes response to on-site MQ

T=450ms   JAR Reading
          └─ Polls MQ: "Any responses for me?"
          └─ Receives: "Answer found, confidence 0.85"
          └─ Completes analysis
          └─ Returns to UI

T=500ms   UI Display
          └─ Shows verification result
          └─ User sees analysis complete
```

---

# 3. System Design

## 🎯 Design Principles

### 1. **No Direct Internet Calls from Server**
- JAR module NEVER calls DuckDuckGo directly
- WebGate ALWAYS handles internet communication
- Prevents server IP from being blocked

### 2. **Message Queue as Central Hub**
- All communication through on-site MQ
- Decoupled, reliable, monitorable
- Works even if one service is down temporarily

### 3. **Polling-Based, Not Push**
- WebGate polls MQ for requests
- No incoming connections needed (works behind NAT)
- Graceful degradation if connectivity lost

### 4. **Single Responsibility**
- WebGate: Only handles internet searches
- JAR: Only handles analysis logic
- MQ: Only stores/routes messages

### 5. **Stateless Processing**
- WebGate doesn't store state between requests
- Easily scalable (multiple instances possible)
- Simple error handling (retry requests)

## 🔄 Request-Response Model

```
Request Lifecycle:
┌──────────────┐
│ Search Request
│ (id, question, context)
└──────┬───────┘
       │
       ├─ Enqueued to MQ
       │
       ├─ WebGate polls
       │
       ├─ WebGate processes
       │  └─ Calls DuckDuckGo
       │
       ├─ WebGate enqueues response
       │
       ├─ JAR polls for response
       │
       ├─ Response delivered to JAR
       │
       └─ Marked as complete
```

---

# 4. Modules & Components

## 📦 Project Structure

```
GS-WebGate/
├── src/main/java/com/noprobit/analyzers/webgate/
│   ├── WebGateApplication.java           (Spring Boot Entry)
│   ├── mq/
│   │   ├── MQClient.java                 (TCP Connection to MQ)
│   │   └── MQCommand.java                (MQ Protocol)
│   ├── polling/
│   │   ├── MQPoller.java                 (Polling Service)
│   │   └── SearchRequestProcessor.java   (Request Handler)
│   ├── service/
│   │   ├── InternetSearchService.java    (DuckDuckGo Integration)
│   │   └── SearchCache.java              (Optional: Response Caching)
│   ├── controller/
│   │   └── PurposeVerificationController.java (Optional: REST API)
│   └── model/
│       ├── SearchRequest.java
│       ├── SearchResponse.java
│       ├── QueryRequest.java
│       ├── QueryResponse.java
│       └── MQMessage.java
│
├── src/main/resources/
│   └── application.yml
│
├── src/test/java/com/noprobit/analyzers/webgate/
│   ├── MQPollerTest.java
│   ├── InternetSearchServiceTest.java
│   └── SearchRequestProcessorIT.java
│
├── pom.xml
├── Dockerfile
└── docker-compose.yml
```

## 🔧 Component Details

### WebGateApplication.java (Spring Boot Entry)

```java
@SpringBootApplication
@EnableScheduling
public class WebGateApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WebGateApplication.class, args);
    }
}

/**
 * Responsibilities:
 * - Application startup
 * - Bean initialization
 * - Component scanning
 * 
 * Configuration:
 * - Port: 8080
 * - Context Path: /
 * - Hot reload: Enabled
 * - Logging: INFO level
 */
```

### MQClient.java (TCP Connection)

```java
@Component
public class MQClient {
    
    @Value("${mq.host}")
    private String mqHost;
    
    @Value("${mq.port}")
    private int mqPort;
    
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    
    /**
     * Methods:
     * - connect(): Establish TCP connection to MQ
     * - sendCommand(MQCommand): Send JSON command
     * - readResponse(): Read JSON response
     * - disconnect(): Close connection
     * - isConnected(): Check connection status
     * 
     * Responsibilities:
     * - TCP socket management
     * - Connection lifecycle
     * - Command serialization
     * - Response deserialization
     */
}
```

### MQPoller.java (Polling Service)

```java
@Service
public class MQPoller {
    
    @Autowired
    private MQClient mqClient;
    
    @Autowired
    private SearchRequestProcessor processor;
    
    private volatile boolean running = false;
    
    /**
     * Methods:
     * - start(): Begin polling loop
     * - pollAndProcess(): Main polling iteration
     * - stop(): Graceful shutdown
     * - reconnectToMQ(): Handle disconnections
     * 
     * Polling Logic:
     * 1. Connect to on-site MQ
     * 2. Every 500ms:
     *    a. Send dequeue_request command
     *    b. Check response status
     *    c. If request found: pass to processor
     *    d. If queue empty: wait 500ms
     * 3. Handle reconnection on error
     * 
     * Responsibilities:
     * - Maintain MQ connection
     * - Continuous polling loop
     * - Error recovery
     * - Thread lifecycle management
     */
}
```

### SearchRequestProcessor.java (Request Handler)

```java
@Service
public class SearchRequestProcessor {
    
    @Autowired
    private InternetSearchService searchService;
    
    @Autowired
    private MQClient mqClient;
    
    /**
     * Methods:
     * - processRequest(SearchRequest): Main processing
     * - callDuckDuckGo(String query): HTTP call
     * - buildResponse(QueryResponse): Response packaging
     * - enqueueResponse(SearchResponse): Store in MQ
     * 
     * Processing Steps:
     * 1. Receive SearchRequest from MQPoller
     * 2. Extract question & context
     * 3. Call InternetSearchService.queryGeneric()
     * 4. Receive QueryResponse
     * 5. Wrap in SearchResponse
     * 6. Enqueue response to MQ
     * 7. Handle any errors gracefully
     * 
     * Responsibilities:
     * - Request processing orchestration
     * - Error handling
     * - Response formatting
     * - Timing/metrics
     */
}
```

### InternetSearchService.java (DuckDuckGo Integration)

```java
@Service
public class InternetSearchService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${search.duckduckgo.enabled:true}")
    private boolean searchEnabled;
    
    /**
     * Public Methods:
     * - queryGeneric(QueryRequest): Generic question search
     * - search(String query): Purpose verification search
     * 
     * Generic Query Search:
     * ├─ Input: question, context
     * ├─ Build query: question + context
     * ├─ Call DuckDuckGo: /api?q=...&format=json
     * ├─ Parse response:
     * │  ├─ Instant answer (confidence: 0.95)
     * │  ├─ Abstract text (confidence: 0.80)
     * │  ├─ Related topics (confidence: 0.70)
     * │  └─ No results (confidence: 0.20)
     * └─ Return QueryResponse
     * 
     * Verification Search:
     * ├─ Input: class name
     * ├─ Build semantic query
     * ├─ Call DuckDuckGo
     * ├─ Score for class relevance
     * └─ Return SearchResult
     * 
     * Responsibilities:
     * - DuckDuckGo API integration
     * - JSON parsing
     * - Confidence calculation
     * - Error handling/fallback
     * - Caching (optional)
     */
}
```

### Model Classes

```java
// SearchRequest.java
public class SearchRequest {
    private String id;              // UUID
    private String question;
    private String context;
    // getters/setters
}

// SearchResponse.java
public class SearchResponse {
    private String id;              // Matches request ID
    private QueryResponse queryResponse;
    private long processingTime;
    private String status;          // "success", "error", "timeout"
    // getters/setters
}

// QueryRequest.java (from MQ)
public class QueryRequest {
    private String question;
    private String context;
    private int maxResults;
    private long timeout;
    // getters/setters
}

// QueryResponse.java (to MQ)
public class QueryResponse {
    private String question;
    private boolean answerFound;
    private String answer;
    private double confidence;      // 0.0-1.0
    private String summary;
    private long processingTime;
    private List<String> sources;
    // getters/setters
}

// MQCommand.java (Protocol)
public class MQCommand {
    private String command;         // "dequeue_request", "enqueue_response", etc.
    private Map<String, Object> payload;
    // getters/setters
}
```

---

# 5. Communication Protocol

## 📡 MQ Protocol Specification

### Command Types

#### 1. **dequeue_request**
```json
{
  "command": "dequeue_request",
  "payload": {}
}

Response (if request found):
{
  "status": "ok",
  "data": {
    "id": "req-12345",
    "question": "What is REST API?",
    "context": "java spring boot"
  }
}

Response (if queue empty):
{
  "status": "empty"
}
```

#### 2. **enqueue_response**
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
      "sources": ["DuckDuckGo (Instant Answer)"]
    },
    "processingTime": 300,
    "status": "success"
  }
}

Response:
{
  "status": "ok",
  "message": "Response enqueued successfully"
}
```

#### 3. **has_response**
```json
{
  "command": "has_response",
  "payload": {
    "requestId": "req-12345"
  }
}

Response:
{
  "status": "ok",
  "has_response": true
}
```

## 📊 Message Flow Sequence

```
WebGate                     On-Site MQ                      JAR Module
    │                            │                               │
    │──────► TCP Connection ─────►│                               │
    │                            │                               │
    │ dequeue_request ──────────►│                               │
    │                            │ "Any requests for me?"        │
    │                            │                               │
    │◄────── (empty response) ───│                               │
    │                            │                               │
    │ [Wait 500ms]               │                               │
    │                            │                               │
    │ dequeue_request ──────────►│                               │
    │                            │ ← JAR enqueued request        │
    │◄──────── SearchRequest ────│                               │
    │ (id, question, context)    │                               │
    │                            │                               │
    │ [Process Request]          │                               │
    │ └─ Call DuckDuckGo API     │                               │
    │ └─ Parse response          │                               │
    │ └─ Calculate confidence    │                               │
    │                            │                               │
    │ enqueue_response ─────────►│                               │
    │ (id, answer, confidence)   │                               │
    │                            │ [Store response]              │
    │◄───────── (ack) ───────────│                               │
    │                            │                               │
    │                            │◄────── Poll for response ────┤
    │                            │ has_response(id)             │
    │                            │                               │
    │                            ├─ Check storage ──────────┐   │
    │                            │                          │   │
    │                            │ [Response found]         │   │
    │                            │                          │   │
    │                            │────► Response ready ────►│   │
    │                            │                               │
    │                            │◄────── dequeue_response ──────┤
    │                            │                               │
    │                            │ [Return answer]          │   │
    │                            │ [Delete from storage]    │   │
    │                            │                               │
    │                            ├─────────────────────────►│   │
    │                            │        QueryResponse      │   │
    │                            │                               │
    │ [Poll again] ──────────────►│                               │
    │ dequeue_request            │                               │
    │                            │                               │
```

---

# 6. Deployment Architecture

## 🚀 Deployment Topology

### Development (Local Only)

```
Laptop/Workstation
├── MQ Server (port 7000)
├── JAR Module (port 8081)
├── WebGate (port 8080)
└── UI (GUI)
```

### Production (Distributed)

```
┌─ ON-SITE DATA CENTER ─────────┐
│                               │
│  Server 1                     │
│  ├─ MQ Server (port 7000)     │
│  └─ JAR Module (port 8081)    │
│                               │
│  Server 2                     │
│  └─ UI Application            │
│                               │
└───────────────────┬───────────┘
                    │
              Internet/VPN
                    │
┌───────────────────┴─────────────────────────────┐
│                                                  │
│ LOCAL COMPUTER 1 (Behind NAT)                   │
│ └─ WebGate Instance 1 (port 8080)               │
│    (Handles searches, ~1000 requests/day)       │
│                                                  │
│ LOCAL COMPUTER 2 (Behind NAT)                   │
│ └─ WebGate Instance 2 (port 8080)               │
│    (Backup/Load distribution)                   │
│                                                  │
└──────────────────────────────────────────────────┘
```

## 🐳 Docker Deployment

### Dockerfile

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/GS-WebGate-1.0-SNAPSHOT.jar webgate.jar

EXPOSE 8080

ENV MQ_HOST=on-site-mq.internal
ENV MQ_PORT=7000
ENV SEARCH_ENABLED=true

ENTRYPOINT ["java", "-jar", "webgate.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  webgate:
    build:
      context: .
      dockerfile: Dockerfile
    
    container_name: GS-WebGate
    
    ports:
      - "8080:8080"
    
    environment:
      MQ_HOST: ${MQ_HOST:-on-site-mq.internal}
      MQ_PORT: ${MQ_PORT:-7000}
      SEARCH_ENABLED: ${SEARCH_ENABLED:-true}
      LOG_LEVEL: ${LOG_LEVEL:-INFO}
    
    networks:
      - webgate-network
    
    restart: always
    
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

networks:
  webgate-network:
    driver: bridge
```

---

# 7. API Specifications

## 📡 REST API (Optional Direct Access)

### Endpoint: Health Check

```http
GET /health
Content-Type: application/json

Response (200 OK):
{
  "status": "UP",
  "service": "WebGate",
  "version": "2.0",
  "mqConnected": true,
  "processingRequests": 0,
  "completedRequests": 1245,
  "uptime": 86400000
}
```

### Endpoint: Generic Query

```http
POST /api/query
Content-Type: application/json

Request:
{
  "question": "What is microservices architecture?",
  "context": "software design",
  "maxResults": 5,
  "timeout": 5000
}

Response (200 OK):
{
  "question": "What is microservices architecture?",
  "answerFound": true,
  "answer": "Microservices is an architectural style that structures...",
  "confidence": 0.88,
  "summary": "Comprehensive answer found",
  "processingTime": 320,
  "sources": [
    "DuckDuckGo (Abstract)",
    "Wikipedia",
    "Martin Fowler Blog"
  ]
}

Response (408 Request Timeout):
{
  "status": "error",
  "message": "DuckDuckGo API timeout",
  "confidence": 0.0,
  "answerFound": false
}

Response (503 Service Unavailable):
{
  "status": "error",
  "message": "MQ connection lost",
  "answerFound": false
}
```

### Endpoint: Purpose Verification

```http
POST /api/verify-purpose
Content-Type: application/json

Request:
{
  "className": "UserController",
  "detectedPurpose": "API_CONTROLLER",
  "keyword": "REST",
  "timestamp": "2026-07-20T12:00:00Z"
}

Response (200 OK):
{
  "className": "UserController",
  "detectedPurpose": "API_CONTROLLER",
  "verified": true,
  "reason": "High confidence match found",
  "internetSource": "DuckDuckGo",
  "confidence": 0.85,
  "processingTime": 250,
  "timestamp": "2026-07-20T12:00:00Z"
}
```

---

# 8. Configuration

## ⚙️ application.yml

```yaml
spring:
  application:
    name: GS-WebGate
  
  boot:
    admin:
      client:
        enabled: false

server:
  port: 8080
  servlet:
    context-path: /
  
  shutdown: graceful
  
  tomcat:
    threads:
      max: 200
      min-spare: 10

# MQ Connection (On-Site Server)
mq:
  host: on-site-server.example.com
  port: 7000
  connection-timeout-ms: 10000
  read-timeout-ms: 30000
  retry-attempts: 3
  retry-delay-ms: 5000

# Search Configuration
search:
  duckduckgo:
    enabled: true
    url: https://api.duckduckgo.com/
    timeout-ms: 10000
    user-agent: GS-WebGate/2.0
  
  cache:
    enabled: false  # Optional: enable for production
    ttl-minutes: 60
    max-size: 1000

# Polling Configuration
polling:
  enabled: true
  interval-ms: 500
  batch-size: 1
  
  reconnection:
    enabled: true
    max-attempts: -1  # Infinite retry
    delay-ms: 5000
    backoff-multiplier: 1.5

# Logging
logging:
  level:
    root: INFO
    com.noprobit.analyzers.webgate: DEBUG
  
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Metrics (optional)
management:
  endpoints:
    web:
      exposure:
        include: health,info
  
  endpoint:
    health:
      show-details: when-authorized
```

---

# 9. Monitoring & Operations

## 📊 Metrics & Monitoring

### Key Metrics

```
1. Request Processing
   ├─ Requests received: counter
   ├─ Requests completed: counter
   ├─ Requests failed: counter
   ├─ Average response time: histogram
   └─ P95/P99 response time: histogram

2. Search Results
   ├─ Average confidence score: gauge
   ├─ Answers found: counter
   ├─ No answers found: counter
   └─ Answer type distribution: gauge

3. MQ Communication
   ├─ MQ connection status: gauge
   ├─ Messages dequeued: counter
   ├─ Messages enqueued: counter
   ├─ Queue read latency: histogram
   └─ Queue write latency: histogram

4. External API
   ├─ DuckDuckGo API calls: counter
   ├─ API response time: histogram
   ├─ API errors: counter
   └─ API timeouts: counter

5. System Health
   ├─ Uptime: gauge
   ├─ Memory usage: gauge
   ├─ Thread count: gauge
   └─ JVM GC time: timer
```

### Monitoring Dashboard (Prometheus/Grafana)

```
┌─ WebGate Status ─────────────────────────────┐
│ Status: UP                                   │
│ Uptime: 48h 23m 15s                          │
│ MQ Connection: Connected                     │
│ Processing Requests: 2                       │
│ Completed Requests: 12,345                   │
└──────────────────────────────────────────────┘

┌─ Performance Metrics ────────────────────────┐
│ Avg Response Time: 285ms                     │
│ P95 Response Time: 520ms                     │
│ P99 Response Time: 890ms                     │
│ Requests/min: 4.2                            │
│ Error Rate: 0.8%                             │
└──────────────────────────────────────────────┘

┌─ Search Results ─────────────────────────────┐
│ Answers Found: 91.5%                         │
│ Avg Confidence: 0.82                         │
│ High Confidence (>0.8): 78%                  │
│ Medium Confidence (0.5-0.8): 18%             │
│ Low Confidence (<0.5): 4%                    │
└──────────────────────────────────────────────┘
```

### Logging Strategy

```
Log Levels:
├─ ERROR: Critical issues (MQ disconnection, API failures)
├─ WARN: Degraded functionality (timeouts, retries)
├─ INFO: Operations (request received, response sent, MQ events)
└─ DEBUG: Details (JSON payloads, timing info, decision paths)

Example Logs:
[2026-07-20 12:30:45] [INFO] WebGate started, listening on port 8080
[2026-07-20 12:30:46] [INFO] Connected to MQ: on-site-server:7000
[2026-07-20 12:31:02] [INFO] Request dequeued: req-abc123
[2026-07-20 12:31:02] [DEBUG] Processing: "What is REST?"
[2026-07-20 12:31:02] [DEBUG] Calling DuckDuckGo API
[2026-07-20 12:31:02] [DEBUG] Response: instant answer, confidence 0.95
[2026-07-20 12:31:02] [INFO] Response enqueued: req-abc123
[2026-07-20 12:31:03] [INFO] Processed 1 request in 1ms
```

## 🔧 Operations Checklist

### Daily Operations

```
□ Check MQ connection status
□ Review error logs for failures
□ Verify DuckDuckGo API availability
□ Check response times (should be <500ms)
□ Monitor disk space
□ Review confidence score distribution
□ Check for any stuck requests
```

### Weekly Operations

```
□ Review performance trends
□ Analyze question patterns
□ Check for repeated errors
□ Update monitoring thresholds if needed
□ Review and archive old logs
□ Test failover procedures
```

### Monthly Operations

```
□ Performance tuning review
□ Capacity planning
□ Update documentation
□ Security patches
□ Backup configuration
□ Disaster recovery drill
```

---

# 10. Security Considerations

## 🔐 Security Architecture

### Network Security

```
WebGate (Local Computer)
├─ No incoming connections (polling only)
├─ Outbound to MQ: Encrypted (optional TLS)
├─ Outbound to DuckDuckGo: HTTPS only
└─ No direct internet ports exposed
```

### Data Security

```
In Transit:
├─ MQ → WebGate: JSON (plain or TLS)
├─ WebGate → DuckDuckGo: HTTPS (encrypted)
└─ WebGate → JAR: Via MQ (no direct)

At Rest:
├─ No sensitive data stored
├─ Responses cached in memory only
├─ No persistent DB
└─ Logs cleaned after TTL
```

### API Security

```
Optional REST Endpoints:
├─ No authentication by default
├─ Add API key if needed:
│  X-API-Key: secret-key-here
├─ CORS: Restrict to localhost
└─ Rate limiting: 100 req/min per IP
```

### Input Validation

```
Question Input:
├─ Max length: 1000 characters
├─ Allowed characters: alphanumeric + space + punctuation
├─ SQL injection: N/A (no database)
├─ Command injection: N/A (no shell)
└─ Sanitization: URL encode before DuckDuckGo call

Context Input:
├─ Max length: 500 characters
├─ Similar validation as question
└─ Used only for search enhancement
```

### Error Handling

```
Public Errors (Sent to JAR):
├─ Generic messages (no stack traces)
├─ Confidence: 0.0 on error
├─ Status: "error" or "timeout"
└─ Example: "Search service temporarily unavailable"

Internal Logs:
├─ Full stack traces
├─ Detailed error context
├─ API response bodies
└─ Timing information
```

---

# Architecture Summary

## 🎯 Key Takeaways

| Aspect | Design Decision | Rationale |
|--------|-----------------|-----------|
| **Communication** | TCP/MQ polling | No incoming connections, works behind NAT |
| **Internet Calls** | WebGate only | Prevents server IP blocking |
| **Architecture** | Microservices | Independent scaling, loose coupling |
| **Protocol** | JSON over TCP | Human-readable, debuggable |
| **External API** | DuckDuckGo | Public, free, no authentication |
| **Deployment** | Standalone JAR | Portable, minimal dependencies |
| **Scaling** | Multiple WebGate instances | Load distribution, redundancy |
| **Monitoring** | Application metrics | Built-in health checks |
| **Resilience** | Graceful degradation | Works without WebGate (local analysis only) |

---

## 📋 Deployment Checklist

### Pre-Deployment

```
□ Configure MQ connection (host, port)
□ Test connectivity to on-site MQ
□ Verify DuckDuckGo API access
□ Set log level (INFO for prod)
□ Configure monitoring/alerts
□ Prepare SSL certificates (optional)
□ Backup configuration
□ Review firewall rules
```

### Post-Deployment

```
□ Verify MQ connection in logs
□ Test with sample search requests
□ Monitor initial request processing
□ Verify response times
□ Check error rate
□ Monitor confidence scores
□ Review log output
□ Test failover scenarios
```

---

## 🚀 Quick Start

```bash
# Build WebGate
mvn -pl GS-WebGate package

# Run WebGate (development)
mvn -pl GS-WebGate spring-boot:run

# Run WebGate (production)
java -jar GS-WebGate/target/GS-WebGate-1.0-SNAPSHOT.jar \
  --mq.host=on-site-server.com \
  --mq.port=7000 \
  --server.port=8080

# Docker
docker build -t webgate:latest .
docker run -e MQ_HOST=on-site-mq -p 8080:8080 webgate:latest
```

---

**Document Version: 2.0**  
**Status: Production Ready**  
**Last Updated: 2026-07-20**  
**Maintainer: GS Team**
