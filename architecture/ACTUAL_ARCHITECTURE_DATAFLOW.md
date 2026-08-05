# GS-WebGate Request and Response Flow

**Version:** 2.0  
**Last Updated:** 2026-08-05

---

## Main Flow: Client Submits a Search

```text
Client Application
  |
  | 1. Create search request
  v
GS-mq
  | 2. Store request in pending queue
  v
GS-WebGate
  | 3. Poll queue for work
  | 4. Dequeue request
  | 5. Run search against internet
  | 6. Build structured result
  v
GS-mq
  | 7. Store response by request ID
  v
Client Application
  | 8. Poll for response
  | 9. Consume result
```

---

## Example Message Lifecycle

### Request message
```json
{
  "requestId": "req-1001",
  "type": "search",
  "question": "What is the current weather in London?",
  "context": "travel",
  "createdAt": 1722810000
}
```

### Response message
```json
{
  "requestId": "req-1001",
  "type": "search-result",
  "answerFound": true,
  "answer": "The weather is mostly cloudy with light rain.",
  "confidence": 0.87,
  "sources": ["DuckDuckGo"],
  "processingTimeMs": 412
}
```

---

## Searcher Processing Steps

1. The searcher connects to GS-mq.
2. It repeatedly polls for pending requests.
3. When a request is found, it dequeues it.
4. It performs the search using a search provider.
5. It formats the result as a structured response.
6. It publishes the response back to GS-mq.

---

## Failure Handling

### GS-mq unavailable
- The searcher retries its connection.
- The client continues to hold the request until the queue is available again.
- No direct dependency on the searcher being reachable by the client is required.

### Searcher unavailable
- Requests remain queued until the searcher comes back online.
- The client can retry or wait for completion.

### Search provider unavailable
- The searcher returns a graceful failure response with a low-confidence or empty result.
- The client can decide how to handle this case.

---

## Why this model works

- The queue is the integration contract.
- The searcher can live behind NAT or on a private host.
- The client does not need to be online at the same time as the searcher.
- The design supports both generic searches and site-specific searches.
