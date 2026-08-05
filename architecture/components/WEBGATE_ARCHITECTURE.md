# GS-WebGate Component Architecture

## Role

GS-WebGate is the execution component of the system. It is responsible for taking queued work, performing the search, and publishing the outcome back to the queue.

## Responsibilities

- poll GS-mq for pending requests,
- dequeue work when available,
- call the external search provider,
- build a structured response,
- publish the result back to GS-mq using the original request ID,
- recover gracefully when the queue or external provider is unavailable.

## Runtime Loop

```text
connect to GS-mq
loop:
  poll for request
  if request exists -> process it
  publish result
  wait briefly
```

## Expected Request Shape

A request should contain at least:
- requestId
- question
- optional context
- optional target or mode

## Expected Response Shape

A response should contain at least:
- requestId
- answerFound
- answer
- confidence
- sources
- processingTimeMs

## Deployment Notes

GS-WebGate should run in a location that has outbound internet access and where inbound connectivity is not required. A private workstation, laptop, or local server is a good fit.
