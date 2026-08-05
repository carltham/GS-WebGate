# GS-WebGate Design Patterns

**Version:** 2.0  
**Last Updated:** 2026-08-05

---

## Queue-based decoupling

The core pattern is asynchronous decoupling through GS-mq. Clients submit work without needing to wait for the searcher to be online.

## Polling pattern

GS-WebGate uses a polling loop to check the queue for work. This avoids the need for inbound connectivity and fits the private-machine deployment model.

## Request/response correlation

Each message carries a request ID so that responses can be matched to the original request even when processing happens later.

## Outbound-only execution

The searcher is designed to make outbound calls only. This makes it suitable for private hosts and NAT environments.

## Graceful degradation

If the search provider is unavailable or the queue is temporarily unreachable, the system can continue to operate in a degraded mode and return partial or no-result responses.
