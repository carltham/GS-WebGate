# GS-WebGate Component Architecture

## Role

GS-WebGate is the execution component of the system. It is responsible for taking work from GS-relay, performing the search, and publishing the outcome back to the service.

## Responsibilities

- poll GS-relay for pending work over REST,
- claim the next pending work item,
- call the external search provider,
- build a structured result,
- publish the result back to GS-relay using the original message ID,
- recover gracefully when GS-relay or the external provider is unavailable.

## Runtime Loop

```text
connect to GS-relay
loop:
  GET next pending work item
  if work exists -> process it
  POST result
  wait briefly
```

## Expected Work-Item Shape

A work item should contain at least:
- messageId
- question
- optional context
- optional target or mode

## Expected Result Shape

A result should contain at least:
- messageId
- answerFound
- answer
- confidence
- sources
- processingTimeMs

## Deployment Notes

GS-WebGate should run in a location that has outbound internet access and where inbound connectivity is not required. A private workstation, laptop, or local server is a good fit.
