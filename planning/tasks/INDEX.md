# Planning Task Index

This index replaces the old Swing-era task tree with a compact roadmap for the current GS-WebGate implementation.

## Current Phases

| Phase | Goal | Next Task |
|---|---|---|
| Phase 0 | Define the shared contract and first tests | [phase-0/0.1-define-contracts.md](phase-0/0.1-define-contracts.md) |
| Phase 1 | Implement REST-backed persistence and correlation | [phase-1/1.1-implement-mq-store.md](phase-1/1.1-implement-mq-store.md) |
| Phase 2 | Implement the worker poll loop over REST | [phase-2/2.1-implement-worker-poll-loop.md](phase-2/2.1-implement-worker-poll-loop.md) |
| Phase 3 | Wire the full REST flow and harden it | [phase-3/3.1-end-to-end-integration.md](phase-3/3.1-end-to-end-integration.md) |

## Task Files

### Phase 0
- [phase-0/0.1-define-contracts.md](phase-0/0.1-define-contracts.md)
- [phase-0/0.2-write-contract-tests.md](phase-0/0.2-write-contract-tests.md)

### Phase 1
- [phase-1/1.1-implement-mq-store.md](phase-1/1.1-implement-mq-store.md)
- [phase-1/1.2-implement-response-correlation.md](phase-1/1.2-implement-response-correlation.md)

### Phase 2
- [phase-2/2.1-implement-worker-poll-loop.md](phase-2/2.1-implement-worker-poll-loop.md)
- [phase-2/2.2-implement-worker-result-publish.md](phase-2/2.2-implement-worker-result-publish.md)

### Phase 3
- [phase-3/3.1-end-to-end-integration.md](phase-3/3.1-end-to-end-integration.md)
- [phase-3/3.2-resilience-and-observability.md](phase-3/3.2-resilience-and-observability.md)
