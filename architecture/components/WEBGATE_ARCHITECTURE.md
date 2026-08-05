# GS-WebGate Component Architecture

**Version:** 2.0  
**Last Updated:** 2026-08-05

---

## Overview

GS-WebGate is the execution component of the system. It runs on a private machine and acts as a search worker that receives work from GS-mq, performs internet searches, and writes results back to the queue.

This component is intentionally simple:
- it polls for work
- it executes search tasks
- it returns structured results
- it does not need to expose a public inbound endpoint

---

## Component Responsibilities

### Polling layer
- Connects to GS-mq
- Periodically checks for pending requests
- Dequeues work when available

### Search execution layer
- Builds search queries from incoming requests
- Calls an external search provider
- Parses the response into structured data
- Applies confidence or relevance scoring if needed

### Response publishing layer
- Packages the result with metadata
- Publishes the response back to GS-mq using the original request ID

### Error handling layer
- Handles connection failures gracefully
- Continues retrying when the queue is temporarily unavailable
- Returns a fallback response when the search provider fails

---

## Runtime Model

```text
Start
  -> connect to GS-mq
  -> loop
       -> poll for request
       -> if request exists -> process it
       -> publish result
       -> wait briefly
```

---

## Search Request Shape

A request should include at least:
- requestId
- question
- optional context
- optional search mode or target domain

---

## Search Result Shape

A result should include:
- requestId
- answerFound
- answer
- confidence
- sources
- processingTimeMs
- optional metadata

---

## Deployment Notes

GS-WebGate should run where internet access is available and where inbound connectivity is not a requirement. A private workstation, laptop, or local server is a good fit.

This allows the system to work even when the rest of the architecture sits behind controlled network boundaries.
