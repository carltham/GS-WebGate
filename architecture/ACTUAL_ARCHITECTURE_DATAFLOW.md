# Runtime Flows

## Happy Path

1. The client creates a search request.
2. The request is enqueued in GS-mq.
3. GS-WebGate polls GS-mq for pending work.
4. GS-WebGate dequeues the request.
5. GS-WebGate executes the search.
6. GS-WebGate publishes a structured result back to GS-mq.
7. The client polls for the response and consumes it.

```text
Client -> GS-mq -> GS-WebGate -> External Search Provider
                ^                           |
                |                           |
                +------ response -----------+
```

## State Transitions

A request moves through a simple lifecycle:
- created / enqueued
- dequeued / processing
- completed / published
- consumed / removed

## Failure Handling

### GS-mq unavailable
- the searcher retries its connection,
- requests remain pending until the queue becomes reachable,
- clients can retry later if needed.

### GS-WebGate unavailable
- requests remain queued,
- processing resumes once the searcher is available again.

### External search provider unavailable
- the searcher returns a degraded response,
- the response can carry low confidence or no answer,
- the client can decide how to interpret that result.

## Operational Notes

- The queue is the main integration boundary.
- The client and searcher do not need to be online at the same time.
- The design is well suited to private-host deployment and outbound-only networking.
