# GS-WebGate Deep Map

**Version:** 2.0  
**Last Updated:** 2026-08-06

---

## System Layers

### 1. Client layer
- Submits work items to GS-relay
- Polls GS-relay for results by message ID
- Owns the business use case that requires a search

### 2. Relay layer
- GS-relay stores pending work items and completed results
- Correlates them via message ID
- Decouples the client from the searcher

### 3. Searcher layer
- GS-searcher polls GS-relay for the next pending work item
- Executes the internet search
- Publishes a structured result back to the relay

---

## Message Flow

```text
Client -> GS-relay : submit work item
GS-searcher -> GS-relay : fetch next pending work item
GS-searcher -> Internet : run search
GS-searcher -> GS-relay : publish result
Client -> GS-relay : fetch result by message ID
```

---

## Important Concepts

### Work item
A work item should contain:
- messageId
- question
- optional context
- optional target or mode

### Result
A result should contain:
- messageId
- answerFound
- answer
- confidence
- sources
- processingTimeMs

---

## Runtime Characteristics

- The searcher can run on a private machine
- The searcher can remain outbound-only
- The relay is the integration boundary
- The client and searcher do not need to be online at the same time

---

## Suggested Module Responsibilities

### GS-WebGate
- polling for the next pending work item
- search execution
- formatting results
- graceful error handling

### GS-relay
- persistence of pending work items
- persistence of completed results
- correlation by message ID
- asynchronous delivery

### Client application
- creating work items
- consuming completed results
- handling business logic around the returned answer
