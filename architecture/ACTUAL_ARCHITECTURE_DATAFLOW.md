# Runtime Flows

## Happy Path

1. The client creates a search request.
2. The request is submitted to GS-relay over REST.
3. GS-WebGate polls GS-relay for pending work through a simple REST endpoint.
4. GS-WebGate claims the request and executes the search.
5. GS-WebGate publishes a structured result back to GS-relay.
6. The client retrieves the response through REST and consumes it.

```text
Client -> REST -> GS-relay -> GS-WebGate -> External Search Provider
                    ^                     |
                    |                     |
                    +------ response -----+
```

## State Transitions

A request moves through a simple lifecycle:
- created / enqueued
- dequeued / processing
- completed / published
- consumed / removed

## Failure Handling

### GS-relay unavailable
- the searcher retries its request,
- requests remain stored until the service becomes reachable,
- clients can retry later if needed.

### GS-WebGate unavailable
- requests remain pending in GS-relay,
- processing resumes once the searcher is available again.

### External search provider unavailable
- the searcher returns a degraded response,
- the response can carry low confidence or no answer,
- the client can decide how to interpret that result.

## Operational Notes

- REST is the main integration boundary.
- The client and searcher do not need to be online at the same time.
- The design is well suited to private-host deployment and outbound-only networking.
