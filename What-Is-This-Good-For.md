# What Is This Good For?

GS-WebGate is a private-search gateway for scenarios where search work must be executed on a private machine, while the work-item/result flow is coordinated through a lightweight relay service.

The idea is simple:

- a client submits work to the relay over REST,
- the relay assigns a message ID and stores the work as pending,
- a searcher running on a private machine polls for bundles of work, processes them, and publishes results back,
- the client later polls the relay for its result by message ID.

This design is useful when:

- the searcher must remain inside a private or controlled environment,
- the system needs an asynchronous work flow instead of a direct connection,
- logging and correlation of work items and results are important,
- a lightweight polling-based pattern is preferred over a fully coupled architecture.

In short, GS-WebGate is meant to support private, auditable, and loosely coupled search workflows rather than being a general-purpose web application framework.
