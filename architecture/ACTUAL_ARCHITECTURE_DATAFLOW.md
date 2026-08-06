# Runtime Flows

## Happy Path

1. The client creates a work item.
2. The work item is submitted to GS-relay over REST.
3. GS-relay stores it as pending work and returns a message ID.
4. GS-WebGate polls GS-relay for the next pending work item through a simple REST endpoint.
5. GS-WebGate claims the work item and executes the search.
6. GS-WebGate publishes a structured result back to GS-relay.
7. The client retrieves the result through REST by message ID and consumes it.

```text
Client -> REST -> GS-relay -> GS-WebGate -> External Search Provider
                   ^                     |
                   |                     |
                   +------ result <------+ 
```

## State Transitions

A work item moves through a simple lifecycle:
- submitted
- pending
- claimed
- completed
- consumed

## Failure Handling

### GS-relay unavailable
- the searcher retries its poll,
- work items remain stored until the service becomes reachable,
- clients can retry later if needed.

### GS-WebGate unavailable
- work items remain pending in GS-relay,
- processing resumes once the searcher is available again.

### External search provider unavailable
- the searcher returns a degraded response,
- the response can carry low confidence or no answer,
- the client can decide how to interpret that result.

## Operational Notes

- REST is the main integration boundary.
- The client and searcher do not need to be online at the same time.
- The design is well suited to private-host deployment and outbound-only networking.
