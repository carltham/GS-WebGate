# GS-WebGate Component Architecture

## Role

GS-WebGate is the execution component of the system. It is responsible for taking work from GS-relay, performing the search, and publishing the outcome back to the service.

## Responsibilities

- poll GS-relay for pending work over REST,
- claim or consume a bundle of pending items,
- call the external search provider,
- build a structured response,
- publish the result back to GS-relay using the original message ID,
- recover gracefully when GS-relay or the external provider is unavailable.

## Runtime Loop

```text
connect to GS-relay
loop:
  GET pending work bundle
  if work exists -> process it
  POST result
  wait briefly
```

## Expected Request Shape

A work item should contain at least:
- messageId
- question
- optional context
- optional target or mode

## Expected Response Shape

A response should contain at least:
- messageId
- answerFound
- answer
- confidence
- sources
- processingTimeMs

## Deployment Notes

GS-WebGate should run in a location that has outbound internet access and where inbound connectivity is not required. A private workstation, laptop, or local server is a good fit.
