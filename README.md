# GS-WebGate

GS-WebGate is a private-search gateway built around a simple asynchronous work-queue pattern:

- a client submits work to a relay over REST,
- the relay stores the work and assigns a message ID,
- a searcher running on a private machine polls for pending work, executes it, and publishes the result back,
- the client later polls the relay for the result using the message ID.

## Purpose

This project is designed for scenarios where search execution must remain inside a private or controlled environment, while still allowing a remote or external client to submit work and receive results in a structured way.

## Repository Structure

- GS-WebGate-pom/ - Maven parent build and module configuration
- GS-WebGate-pom/GS-searcher/ - search execution component
- GS-WebGate-pom/GS-relay/ - relay service for work submission, polling, and result storage
- architecture/ - architecture documentation
- planning/ - planning and phase notes

## Status

### ✅ Phase 0: Complete
- **Contract Definition**: WorkItemRequest, WorkItemResponse, SearchResult, PolledWork
- **GS-relay**: 5 tests passing
  - REST endpoints: Submit work, poll for work, store result, retrieve result
  - Persistence: H2 database with WorkItem/Result entities
  - Message correlation: Message ID links submissions to results
- **GS-searcher**: 5 tests passing
  - Worker loop: Poll relay → Execute search → Submit result
  - HTTP client: Real communication with relay (no mocks)
  - Search executor: Mock implementation for testing
- **Build Status**: 10/10 tests passing, BUILD SUCCESS
- **Date Completed**: 2026-08-06

### 🔄 Phase 1: In Progress
- Planned: Resilience, retry logic, timeouts, dead-letter queue
- See [planning/tasks/INDEX.md](planning/tasks/INDEX.md) for task tracking
