# GS-WebGate Architecture Documentation

This document is the entry point for the architecture set.

## Architecture at a Glance

GS-WebGate is a two-part system:
- GS-WebGate runs on a private machine and performs internet searches.
- GS-relay acts as the coordination layer between clients and the searcher.

The design is intentionally simple: clients enqueue work, the searcher polls for it, and results are returned asynchronously through the queue.

## Recommended Reading Order

1. [ACTUAL_ARCHITECTURE_OVERVIEW.md](ACTUAL_ARCHITECTURE_OVERVIEW.md) — context, goals, and boundaries
2. [ACTUAL_ARCHITECTURE_MODULES.md](ACTUAL_ARCHITECTURE_MODULES.md) — components and contracts
3. [ACTUAL_ARCHITECTURE_DATAFLOW.md](ACTUAL_ARCHITECTURE_DATAFLOW.md) — runtime flows and error handling
4. [ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md](ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md) — why the design looks this way
5. [components/WEBGATE_ARCHITECTURE.md](components/WEBGATE_ARCHITECTURE.md) — GS-WebGate internals
6. [components/MQ_ARCHITECTURE.md](components/MQ_ARCHITECTURE.md) — GS-relay internals

## Key Idea

The queue is the integration boundary. The searcher can live behind NAT or on a private host, while clients remain decoupled from the search execution layer.
