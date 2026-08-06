# Components and Contracts

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
- accepting incoming work submissions over REST,
- storing pending work items with the simplest possible persistence,
- exposing a simple endpoint for GS-WebGate to fetch the next pending work item,
- storing completed results,
- correlating each result with its message ID.

### Work-item contract
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

## 3. GS-WebGate

GS-WebGate is the execution layer. It is responsible for:
- polling GS-relay for the next pending work item,
- claiming that work item,
- executing the search,
- building the structured result,
- publishing the result back to GS-relay.

It is the only component expected to reach the external search provider directly.

## Interaction Summary

```text
Client -> GS-relay : POST /messages
GS-WebGate -> GS-relay : GET /messages/next
GS-WebGate -> External Search Provider : execute search
GS-WebGate -> GS-relay : POST /results
Client -> GS-relay : GET /results/{messageId}
```

## Supported Search Modes

The architecture is intended to support:
- generic web searches,
- site-specific searches,
- context-aware searches,
- searches that require a private machine to reach the internet.
