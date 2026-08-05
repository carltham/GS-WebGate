# Components and Contracts

## 1. Client Application

The client is the initiator of the flow. It is responsible for:
- creating a search request,
- sending it to GS-mq,
- polling for the matching response,
- handling the business outcome.

The client does not perform the search itself.

## 2. GS-mq

GS-mq is the coordination layer. It is responsible for:
- accepting incoming search requests,
- storing pending requests,
- delivering requests to GS-WebGate,
- storing completed responses,
- correlating each response with its request ID.

### Request contract
```json
{
  "requestId": "req-1001",
  "type": "search",
  "question": "What is the weather in London?",
  "context": "travel"
}
```

### Response contract
```json
{
  "requestId": "req-1001",
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
- polling GS-mq for pending requests,
- executing the search,
- building the structured response,
- publishing the response back to GS-mq.

It is the only component expected to reach the external search provider directly.

## Interaction Summary

```text
Client -> GS-mq : enqueue request
GS-WebGate -> GS-mq : dequeue request
GS-WebGate -> External Search Provider : execute search
GS-WebGate -> GS-mq : enqueue response
Client -> GS-mq : fetch response
```

## Supported Search Modes

The architecture is intended to support:
- generic web searches,
- site-specific searches,
- context-aware searches,
- searches that require a private machine to reach the internet.
