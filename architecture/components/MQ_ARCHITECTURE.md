# GS-mq Component Architecture

## Role

GS-mq is the transport and coordination layer for the system. It allows clients and the private searcher to exchange work without needing to be online at the same time.

## Responsibilities

- store pending search requests,
- deliver requests to GS-WebGate,
- store completed responses,
- allow clients to retrieve the response later,
- preserve request/response correlation using the request ID.

## Message Model

### Request
```json
{
  "requestId": "req-1001",
  "type": "search",
  "question": "Find news about AI startups",
  "context": "business"
}
```

### Response
```json
{
  "requestId": "req-1001",
  "type": "search-result",
  "answerFound": true,
  "answer": "Results found",
  "confidence": 0.85,
  "sources": ["DuckDuckGo"]
}
```

## Interaction Pattern

```text
Client -> GS-mq : enqueue request
GS-WebGate -> GS-mq : dequeue request
GS-WebGate -> GS-mq : enqueue response
Client -> GS-mq : fetch response by requestId
```

## Design Notes

- the queue should stay lightweight and predictable,
- the broker should tolerate temporary searcher downtime,
- request/response pairing should be explicit and simple,
- the system should support multiple clients and multiple searcher instances if needed.
