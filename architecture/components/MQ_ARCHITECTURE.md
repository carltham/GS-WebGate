# GS-relay Component Architecture

## Role

GS-relay is the coordination and persistence layer for the system. It allows clients and the private searcher to exchange work without needing to be online at the same time.

## Responsibilities

- store pending search requests with minimal persistence,
- expose simple REST endpoints for GS-WebGate to poll for work,
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
Client -> GS-relay : POST /requests
GS-WebGate -> GS-relay : GET /requests/pending
GS-WebGate -> GS-relay : POST /responses
Client -> GS-relay : GET /responses/{requestId}
```

## Design Notes

- the persistence layer should stay lightweight and predictable,
- the service should tolerate temporary searcher downtime,
- request/response pairing should be explicit and simple,
- the system should support multiple clients and multiple searcher instances if needed.
