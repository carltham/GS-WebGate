# Design Decisions and Trade-offs

## 1. REST-first decoupling

The core decision is to use REST as the integration boundary. This keeps the client and searcher decoupled and allows asynchronous processing without requiring a more complex messaging broker.

## 2. Polling over live sockets

GS-WebGate uses polling because the searcher is expected to run on a private or NATed host. Polling avoids the need for inbound connectivity and keeps the system operational in restricted environments.

## 3. Message ID correlation

Every work item carries a message ID so that results can be matched to the original submission and later retrieved by the client.

## 4. Outbound-only execution

The searcher is designed to make outbound calls only. That makes it suitable for private hosts and reduces exposure to inbound network risk.

## 5. Graceful degradation

If the search provider or GS-relay is temporarily unavailable, the system can degrade gracefully and return an incomplete or low-confidence result instead of failing completely.

## Summary

The architecture favors simplicity, decoupling, and resilience over a more complex synchronous integration model.
