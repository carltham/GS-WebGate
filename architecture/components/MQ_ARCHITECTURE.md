# GS-mq Component Architecture

**Version:** 2.0  
**Last Updated:** 2026-08-05

---

## Overview

GS-mq is the messaging broker for the system. It connects client applications with the private searcher without requiring them to be online at the same time.

It acts as the queue for:
- incoming search requests
- completed search responses

---

## Main Responsibilities

- store pending requests
- deliver requests to GS-WebGate
- store responses by request ID
- allow the original client to fetch the response later
- keep the interaction asynchronous and decoupled

---

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

---

## Interaction Pattern

```text
Client -> GS-mq : enqueue request
GS-WebGate -> GS-mq : dequeue request
GS-WebGate -> GS-mq : enqueue response
Client -> GS-mq : dequeue response by requestId
```

---

## Design Notes

- The queue should be simple and lightweight.
- Request-response correlation is based on request ID.
- The broker should tolerate temporary searcher downtime.
- The system should support multiple clients and multiple searcher instances if needed.
