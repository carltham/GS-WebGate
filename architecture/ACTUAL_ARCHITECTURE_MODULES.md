# Components and Contracts

## Architecture principles
- Keep each component focused on a single role.
- Use the simplest contract possible for each interaction.
- Separate the client-facing flow from the worker execution flow.
- Do not mix search execution into the relay layer.

## 1. Client Application

The client is the initiator of the flow. It is responsible for:
- creating a work item,
- submitting it to GS-relay over REST,
- receiving a message ID for tracking,
- polling for the matching result by message ID,
- handling the business outcome.

The client does not perform the search itself.

## 2. GS-relay

GS-relay is the coordination and persistence layer. It is responsible for:
- owning the client-facing API for submitting work and fetching results,
- storing pending work items and completed results,
- exposing a dedicated worker polling endpoint for GS-searcher,
- correlating each work item with its message ID.

GS-relay does not perform searches. It only coordinates and stores state.

### Relay API contract
```json
{
  "messageId": "msg-1001",
  "type": "search",
  "question": "What is the weather in London?",
  "context": "travel"
}
```

### Result contract
```json
{
  "messageId": "msg-1001",
  "type": "search-result",
  "answerFound": true,
  "answer": "Mostly cloudy with light rain",
  "confidence": 0.87,
  "sources": ["DuckDuckGo"],
  "processingTimeMs": 412
}
```

## 3. GS-searcher

GS-searcher is the execution layer. It is responsible for:
- polling GS-relay for the next pending work item,
- claiming that work item,
- executing the search against the external provider,
- building the structured result,
- publishing the result back to GS-relay.

GS-searcher is the only component that communicates with the external search provider.

## Interaction Summary

```text
Client -> GS-relay : POST /messages
GS-searcher -> GS-relay : GET /messages/next
GS-searcher -> External Search Provider : execute search
GS-searcher -> GS-relay : POST /results
Client -> GS-relay : GET /results/{messageId}
```

## Supported Search Modes

The architecture is intended to support:
- generic web searches,
- site-specific searches,
- context-aware searches,
- searches that require a private machine to reach the internet.
