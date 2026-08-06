# GS-WebGate System Overview

## Purpose

GS-WebGate is a private-search gateway built for environments where a searcher must run on a private machine and reach the internet freely, while other systems remain on a controlled internal network.

## Problem Statement

The system solves a simple integration problem:
- clients need search capability,
- the searcher must live on a private host,
- direct inbound connections are not desirable,
- asynchronous processing is preferred.

## Main Actors

- Client application: submits a work item to GS-relay over REST and later polls for the result by message ID.
- GS-relay: stores pending work items, exposes an endpoint to fetch the next pending item, and stores completed results for later retrieval.
- GS-WebGate: polls for work through REST, claims one work item at a time, performs the search, and publishes the result back to GS-relay.
- External search provider: provides the actual internet search results.

## High-Level Shape

```text
Client -> REST -> GS-relay -> GS-WebGate -> External Search Provider
                   ^                     |
                   |                     |
                   +------ result <------+ 
```

## Why this architecture exists

This design is useful when:
- the searcher must stay behind NAT or on a private host,
- the client and searcher should remain loosely coupled,
- the system should tolerate temporary outages,
- the integration should be asynchronous and simple.

## Boundaries

- GS-WebGate is the execution layer.
- GS-relay is the coordination layer.
- Clients are the consumers of the service.
- The external search provider is an external dependency.

## Design Principles

- outbound-only connectivity for the searcher,
- REST as the primary client-to-relay contract,
- minimal persistence for pending work and completed results,
- contract-first, test-first, top-down TDD as the implementation discipline.
