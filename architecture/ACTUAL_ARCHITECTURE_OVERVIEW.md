# GS-WebGate Architecture Overview

**Version:** 2.0  
**Last Updated:** 2026-08-05  
**Status:** Updated for the private search gateway model

---

## Purpose

GS-WebGate is a two-part web search gateway system designed for environments where a searcher must run on a private machine and reach the internet freely while other components remain on a controlled domain network.

The system is split into two parts:

1. GS-WebGate
   - Runs on a private or local computer
   - Has outbound internet access
   - Performs web searches and returns structured results

2. GS-mq
   - Runs on a domain-side host or internal server
   - Receives search requests from client applications
   - Stores pending requests and completed responses

---

## Core Idea

A client application submits a search request to the queue. The searcher polls the queue, processes the request, and publishes the result back to the same queue. The client later polls for the result and consumes it independently.

This design is useful when:
- the searcher must stay on a private machine behind NAT
- direct inbound connections are not available or desired
- the client and the searcher must stay loosely coupled
- asynchronous processing is preferred over a direct HTTP call

---

## High-Level Architecture

```text
+------------------------+        +------------------------+
| Client Application     |        | GS-mq                 |
| (domain-side)         |        | (domain-side queue)   |
| - submits request     |  ->    | - stores requests     |
| - polls for result    |  <-    | - stores responses    |
+------------------------+        +------------------------+
             |                                  |
             |                                  |
             |                                  v
             |                        +------------------------+
             |                        | GS-WebGate            |
             |                        | (private machine)     |
             |                        | - polls MQ            |
             |                        | - runs searches       |
             |                        | - writes results      |
             |                        +------------------------+
             |
             +--------------------------> Internet
```

---

## Responsibilities

### GS-WebGate
- Polls the queue for new search tasks
- Executes a web search using an external search provider
- Builds a structured response with answer, confidence, sources, and metadata
- Publishes the result back to the queue
- Operates behind NAT or on a private host without requiring inbound connectivity

### GS-mq
- Accepts requests from client applications
- Stores pending requests in a queue
- Correlates each request with a response via request ID
- Allows the searcher to dequeue work and publish results
- Provides a simple asynchronous transport layer

---

## Typical Request Flow

1. A client creates a search request with a question and optional context.
2. The client sends the request to GS-mq.
3. GS-WebGate polls GS-mq for pending requests.
4. GS-WebGate performs the search.
5. GS-WebGate sends the result back to GS-mq.
6. The client polls for the response and processes it.

---

## Deployment Model

### Private-machine deployment
- GS-WebGate runs on a workstation, laptop, or private server
- It is allowed to reach the internet directly
- It does not need inbound firewall rules

### Domain-side deployment
- GS-mq runs on a host inside the organization network
- Clients submit requests over the network
- The queue remains the integration boundary between systems

---

## Design Principles

- Outbound-only connectivity for the searcher
- Asynchronous request/response handling
- Simple queue-based integration
- No dependency on direct inbound connectivity
- Search tasks are decoupled from result consumption
- The queue acts as the contract between client and searcher

---

## Summary

GS-WebGate is not a direct synchronous API service first; it is a private search worker that integrates through GS-mq. The queue is the coordination layer, and the searcher is the execution layer.
