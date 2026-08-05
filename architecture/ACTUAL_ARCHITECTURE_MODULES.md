# GS-WebGate Modules

**Version:** 2.0  
**Last Updated:** 2026-08-05

---

## Module 1: GS-WebGate

**Location:** GS-WebGate-pom/GS-WebGate

**Purpose:**
A private search worker that runs on a local or private machine and performs internet searches for incoming queue messages.

**Responsibilities:**
- Poll GS-mq for search requests
- Process each request independently
- Use an external search provider such as DuckDuckGo
- Convert results into a structured response object
- Publish the result back to GS-mq

**Core responsibilities in practice:**
- request polling
- search execution
- result enrichment
- error handling and retry logic

**Design notes:**
- The module should be able to run without inbound connectivity requirements.
- It should be able to operate behind NAT or on a private host.
- It should be safe to run on a machine that can reach the internet but should not expose a public service unnecessarily.

---

## Module 2: GS-mq

**Location:** GS-WebGate-pom/GS-mq

**Purpose:**
The transport and coordination layer between client applications and the private searcher.

**Responsibilities:**
- Receive search requests from clients
- Store pending requests
- Deliver work to the searcher
- Receive results from the searcher
- Expose completed responses to the original client
- Keep request and response state correlated by request ID

**Core characteristics:**
- queue-based communication
- simple request/response correlation
- asynchronous processing
- lightweight operational model

---

## Cross-Module Interaction

```text
Client -> GS-mq : enqueue request
GS-WebGate -> GS-mq : dequeue request
GS-WebGate -> Internet : run search
GS-WebGate -> GS-mq : enqueue response
Client -> GS-mq : retrieve response
```

---

## Search Types Supported

The architecture is intended to support:
- generic web searches
- site-specific searches
- context-aware search requests
- search tasks that require a private machine to access external search services

---

## Summary

The architecture is centered on a simple separation of concerns:
- GS-mq handles transport and coordination
- GS-WebGate handles execution and search logic
- client applications handle business usage and consume results
