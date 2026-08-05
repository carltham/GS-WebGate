# GS-WebGate Deep Map

**Version:** 2.0  
**Last Updated:** 2026-08-05

---

## System Layers

### 1. Client layer
- Submits search requests to GS-mq
- Polls GS-mq for responses
- Owns the business use case that requires a search

### 2. Queue layer
- GS-mq stores requests and responses
- Correlates them via request ID
- Decouples the client from the searcher

### 3. Searcher layer
- GS-WebGate polls GS-mq for work
- Executes the internet search
- Publishes a structured response back to the queue

---

## Message Flow

```text
Client -> GS-mq : enqueue request
GS-WebGate -> GS-mq : dequeue request
GS-WebGate -> Internet : run search
GS-WebGate -> GS-mq : enqueue response
Client -> GS-mq : fetch response
```

---

## Important Concepts

### Request
A request should contain:
- requestId
- question
- optional context
- optional target or mode

### Response
A response should contain:
- requestId
- answerFound
- answer
- confidence
- sources
- processingTimeMs

---

## Runtime Characteristics

- The searcher can run on a private machine
- The searcher can remain outbound-only
- The queue is the integration boundary
- The client and searcher do not need to be online at the same time

---

## Suggested Module Responsibilities

### GS-WebGate
- queue polling
- search execution
- formatting results
- graceful error handling

### GS-mq
- persistence of pending requests
- persistence of completed responses
- correlation by request ID
- asynchronous delivery

### Client application
- creating requests
- consuming completed results
- handling business logic around the returned answer
