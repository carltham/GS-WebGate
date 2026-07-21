# WebGate & MQ: Design Patterns

**Version:** 1.0 (WebGate/MQ focused)  
**Date:** 2026-07-20

---

## Pattern: Adapter

**Used in:** WebGate (DuckDuckGo API Integration)  
**Problem:** Adapt third-party API to our domain model

### Implementation

```
InternetSearchService (Adapter)
  │
  ├─→ Takes: QueryRequest (our domain model)
  │   └─ question: String
  │   └─ context: String
  │   └─ maxResults: int
  │
  ├─→ Calls: DuckDuckGo API (third-party)
  │   └─ Different format, different response structure
  │
  └─→ Returns: QueryResponse (our domain model)
      └─ answer: String
      └─ confidence: double
      └─ sources: String[]
```

### Adaptation Logic

```
QueryRequest
  ├─→ InternetSearchService
  │   ├─ Transforms to DuckDuckGo format
  │   ├─ Calls DuckDuckGo API (HTTPS GET)
  │   ├─ Parses DuckDuckGo JSON response
  │   ├─ Extracts:
  │   │   ├─ Instant answer (confidence 0.95)
  │   │   ├─ Abstract (confidence 0.80)
  │   │   ├─ Related topics (confidence 0.70)
  │   │   └─ No results (confidence 0.20)
  │   └─ Transforms to QueryResponse
  └─→ QueryResponse
     ├─ answer: "REST is an architectural..."
     ├─ confidence: 0.92
     └─ sources: ["DuckDuckGo", "Wikipedia"]

Result: WebGate callers never touch DuckDuckGo API directly
        Only interact with our domain models
```

### Benefits

- ✅ Isolates third-party API changes
- ✅ Consistent interface for all queries
- ✅ Easy to swap search provider (Google, Bing, etc.)
- ✅ Cleaner calling code in JAR module
- ✅ Testable (can mock DuckDuckGo responses)
- ✅ Confidence scoring abstracted from API details

---

## Pattern: Polling

**Used in:** WebGate (Message Queue Polling)  
**Problem:** Retrieve tasks from queue without incoming connections

### Implementation

```
MQPoller (Polling Service)
  │
  ├─→ Configuration:
  │   ├─ Interval: 500ms (configurable)
  │   ├─ Server: on-site-mq.internal:7000
  │   └─ Command: dequeue_request
  │
  ├─→ Main Loop:
  │   ├─ Every 500ms:
  │   │   ├─ Connect to MQ (TCP)
  │   │   ├─ Send: { "command": "dequeue_request" }
  │   │   ├─ Receive: SearchRequest (if available)
  │   │   ├─ Pass to SearchRequestProcessor
  │   │   └─ Loop continues
  │   └─ On error: exponential backoff retry
  │
  └─→ Benefits:
      ├─ No incoming connections (works behind NAT)
      ├─ No firewall rules needed
      ├─ Gracefully handles server downtime
      └─ Simple, reliable pattern
```

### Request-Response via Polling

```
1. WebGate polls for requests
   └─→ "Any work for me?"
   
2. MQ returns request or "empty"
   └─→ If request: SearchRequest {id, question, context}
   
3. WebGate processes
   └─→ Call DuckDuckGo, format response
   
4. WebGate enqueues response
   └─→ MQ stores by requestId
   
5. JAR polls for response
   └─→ "Is my response ready?"
   
6. MQ returns response when ready
   └─→ JAR receives, analysis continues
```

### Benefits

- ✅ No incoming connections (NAT-friendly)
- ✅ Simple protocol (TCP, JSON)
- ✅ Decoupled services (either can be down)
- ✅ Handles multiple WebGate instances (load distribution)
- ✅ Built-in monitoring (stats command)

---

## Pattern: Graceful Degradation

**Used in:** WebGate (Optional Verification)  
**Problem:** System works even if external service unavailable

### Implementation

```
JAR Analysis Flow (with optional WebGate):

1. Local Analysis (always works)
   ├─→ Detect class purpose via patterns
   ├─→ Confidence: 0.95 (PascalCase match)
   └─→ Source: "Pattern matching"

2. Optional WebGate Verification (may fail)
   ├─→ Try to POST to WebGate
   ├─→ If timeout/unavailable:
   │   ├─ Log warning
   │   ├─ Continue with local result
   │   └─ Mark: "Local analysis only"
   └─→ If success:
       ├─ Combine confidence scores
       ├─ Update result
       └─ Mark: "Verified via internet"

3. Return to UI
   └─→ Analysis complete (with or without verification)
```

### Fallback Behavior

```
Scenario 1: WebGate Available
  Confidence: 0.95 (local) × 0.92 (verified) = 0.874

Scenario 2: WebGate Timeout
  Confidence: 0.95 (local only)
  Source: "Local analysis, verification unavailable"

Scenario 3: WebGate Error
  Confidence: 0.95 (local only)
  Log: "[WARN] WebGate unavailable, using local confidence"
```

### Benefits

- ✅ System works without internet
- ✅ Works if WebGate is down
- ✅ Network issues don't block analysis
- ✅ Users get results either way
- ✅ Enhanced results when services available

---

## Summary: WebGate Design Philosophy

1. **Adapter Pattern** - Hide DuckDuckGo complexity behind clean API
2. **Polling Pattern** - Avoid incoming connections (NAT-friendly)
3. **Graceful Degradation** - System works without external services
4. **Loose Coupling** - Via Message Queue (Phase 6+)
5. **Confidence Scoring** - Transparent quality metrics

These patterns enable WebGate to be:
- **Deployable anywhere** (behind NAT, firewall, VPN)
- **Reliable** (works when external services fail)
- **Simple** (clear responsibilities, minimal complexity)
- **Scalable** (multiple instances via MQ load distribution)
