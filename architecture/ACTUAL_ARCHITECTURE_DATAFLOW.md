# WebGate & MQ: Data Flow & Communication

**Version:** 1.0 (WebGate/MQ focused)  
**Date:** 2026-07-20

---

## Workflow: WebGate Verifies Purpose (via REST API)

### Purpose Verification Flow

```
JAR Module has detected purpose: CONTROLLER
  ↓
If RemoteVerification enabled:
  ├─→ Call WebGate API
  │   └─→ POST http://localhost:8080/webgate/api/verify-purpose
  │
  └─→ Request payload:
      {
        "className": "UserController",
        "detectedPurpose": "CONTROLLER",
        "keyword": "controller"
      }
  ↓
WebGate receives request
  └─→ PurposeVerificationController.verifyPurpose()
      ├─→ InternetSearchService.verifyPurpose()
      │   ├─→ Build DuckDuckGo query
      │   │   └─→ "UserController controller pattern"
      │   ├─→ HTTP GET to https://api.duckduckgo.com/
      │   │
      │   ├─→ Parse DuckDuckGo response:
      │   │   ├─ Instant answer (if present): confidence 0.95
      │   │   ├─ Abstract (if present): confidence 0.80
      │   │   └─ Related topics (if present): confidence 0.70
      │   │
      │   └─→ Return RemoteVerificationResult {
      │         className: "UserController",
      │         purpose: "CONTROLLER",
      │         verified: true,
      │         confidence: 0.92,
      │         sources: ["DuckDuckGo Instant Answer"]
      │       }
      │
      └─→ Send JSON response
  ↓
JAR Module receives verification
  ├─→ Update confidence score
  │   └─→ 0.95 (local) * 0.92 (verification) = 0.874
  └─→ Include in AnalysisResult
```

---

## Workflow: WebGate Generic Query (via REST API)

### Generic Query Flow

```
Any system queries WebGate for general knowledge
  ↓
POST http://localhost:8080/webgate/api/query
  └─→ {
      "question": "What is REST API?",
      "context": "java spring boot",
      "maxResults": 5,
      "timeout": 5000
    }
  ↓
WebGate.PurposeVerificationController.queryGeneric()
  └─→ InternetSearchService.queryGeneric()
      ├─→ Build DuckDuckGo query
      │   ├─ Add question: "What is REST API?"
      │   ├─ Add context: "java spring boot"
      │   └─ Set max results: 5
      │
      ├─→ HTTP GET to DuckDuckGo API
      │   └─→ https://api.duckduckgo.com/?q=What+is+REST+API+java+spring+boot
      │
      ├─→ Parse response:
      │   ├─ Extract instant answer (confidence 0.95)
      │   │   └─→ "REST is Representational State Transfer, an architectural style..."
      │   ├─ Extract abstract (confidence 0.80)
      │   ├─ Extract related topics (confidence 0.70)
      │   └─ Extract sources
      │
      └─→ Return QueryResponse {
        question: "What is REST API?",
        answerFound: true,
        answer: "REST is Representational State Transfer...",
        confidence: 0.92,
        summary: "Direct answer found",
        processingTime: 245,
        sources: ["DuckDuckGo", "Wikipedia", "MDN"]
      }
  ↓
Client receives answer with confidence score
```

---

## Workflow: MQ Request-Response Cycle

### Message Queue Communication

```
JAR Module needs verification
  ├─→ Enqueue request to MQ
  │   POST with command: "enqueue_request"
  │   {
  │     "id": "req-12345",
  │     "question": "What is REST API?",
  │     "context": "java spring boot"
  │   }
  │
  └─→ MQ Server stores request (FIFO queue)
  
WebGate polls MQ
  ├─→ Periodic polling (every 500ms)
  │   POST with command: "dequeue_request"
  │
  ├─→ MQ Server returns oldest request
  │   {
  │     "id": "req-12345",
  │     "question": "What is REST API?",
  │     "context": "java spring boot"
  │   }
  │
  └─→ WebGate marks as "processing"

WebGate processes request
  ├─→ Call DuckDuckGo API
  ├─→ Parse response
  ├─→ Calculate confidence
  └─→ Build response object

WebGate enqueues response to MQ
  ├─→ POST with command: "enqueue_response"
  │   {
  │     "id": "req-12345",
  │     "queryResponse": {
  │       "question": "What is REST API?",
  │       "answerFound": true,
  │       "answer": "REST is Representational State Transfer...",
  │       "confidence": 0.92,
  │       "sources": ["DuckDuckGo"]
  │     }
  │   }
  │
  └─→ MQ Server stores response (by requestId)

JAR Module polls MQ for response
  ├─→ POST with command: "has_response"
  │   { "requestId": "req-12345" }
  │
  ├─→ MQ Server checks storage
  │
  └─→ When response is ready:
      POST with command: "dequeue_response"
      { "requestId": "req-12345" }
      
      MQ Server returns response and deletes from storage

JAR Module receives answer
  ├─→ Processes verification result
  └─→ Continues analysis
```

---

## Error Handling: WebGate Unavailable (Graceful Degradation)

```
JAR tries to verify purpose via WebGate
  ├─→ POST to http://localhost:8080/webgate/api/verify-purpose
  ├─→ Connection timeout (WebGate not running)
  │
  └─→ InternetSearchService catches exception:
      ├─→ Log warning
      ├─→ Continue with local confidence score (no verification)
      ├─→ Mark source as "Local analysis only"
      └─→ Return result with original confidence
```

---

## Error Handling: MQ Unavailable (Graceful Degradation)

```
WebGate tries to poll MQ
  ├─→ TCP connection attempt to MQ server
  ├─→ Connection refused (MQ not running)
  │
  └─→ MQPoller catches exception:
      ├─→ Log error
      ├─→ Wait and retry (exponential backoff)
      ├─→ No requests to process during outage
      └─→ Resume polling when MQ comes back online
```

---

## Summary: Communication Patterns

WebGate supports two communication modes:

**Mode 1: Direct REST API (Synchronous)**
- JAR calls WebGate directly via HTTP
- WebGate immediately queries DuckDuckGo
- Response returned synchronously
- Used for optional verification during analysis
- Gracefully degrades if WebGate unavailable

**Mode 2: Message Queue (Asynchronous)**
- JAR enqueues request to MQ
- WebGate polls MQ periodically
- WebGate processes independently
- Response stored in MQ for later retrieval
- Decoupled: either service can be down temporarily
- Planned for Phase 6+

Both modes use the same underlying:
- DuckDuckGo API for searches
- Confidence scoring (0.0-1.0)
- Source attribution
