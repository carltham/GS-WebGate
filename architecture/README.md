# GS-WebGate Architecture Documentation

**Architecture reference for the private search gateway system**

---

## Documentation Set

- [ACTUAL_ARCHITECTURE_OVERVIEW.md](ACTUAL_ARCHITECTURE_OVERVIEW.md) — system overview and deployment model
- [ACTUAL_ARCHITECTURE_DATAFLOW.md](ACTUAL_ARCHITECTURE_DATAFLOW.md) — request and response flow
- [ACTUAL_ARCHITECTURE_MODULES.md](ACTUAL_ARCHITECTURE_MODULES.md) — module responsibilities
- [ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md](ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md) — design patterns
- [components/WEBGATE_ARCHITECTURE.md](components/WEBGATE_ARCHITECTURE.md) — GS-WebGate component design
- [components/MQ_ARCHITECTURE.md](components/MQ_ARCHITECTURE.md) — GS-mq component design

---

## Architecture Summary

The architecture is a two-part system:

1. GS-WebGate runs on a private machine and performs web searches.
2. GS-mq acts as the queue and coordination layer between clients and the searcher.

This gives the system a simple asynchronous flow where requests are queued, processed, and returned later.
