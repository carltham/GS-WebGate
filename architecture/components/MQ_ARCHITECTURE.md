# GS-relay Component Architecture

## Role

GS-relay is the coordination and persistence layer for the system. It allows clients and the private searcher to exchange work without needing to be online at the same time.

## Responsibilities

- store pending work submissions with minimal persistence,
- expose simple REST endpoints for GS-WebGate to poll for work,
- store completed results,
- allow clients to retrieve the result later,
- preserve work/result correlation using the message ID.

## Message Model

### Submission
```json
{
  "messageId": "msg-1001",
  "type": "search",
  "question": "Find news about AI startups",
  "context": "business"
}
```

### Result
```json
{
  "messageId": "msg-1001",
  "type": "search-result",
  "answerFound": true,
  "answer": "Results found",
  "confidence": 0.85,
  "sources": ["DuckDuckGo"]
}
```

## Interaction Pattern

```text
Client -> GS-relay : POST /messages
GS-WebGate -> GS-relay : GET /messages/pending
GS-WebGate -> GS-relay : POST /results
Client -> GS-relay : GET /results/{messageId}
```

## Design Notes

- the persistence layer should stay lightweight and predictable,
- the service should tolerate temporary searcher downtime,
- work/result pairing should be explicit and simple,
- the system should support multiple clients and multiple searcher instances if needed.
