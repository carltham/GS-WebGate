# GS-WebGate

GS-WebGate is a private-search gateway built around a simple asynchronous work-queue pattern:

- a client submits work to a relay over REST,
- the relay stores the work and assigns a message ID,
- a searcher running on a private machine polls for pending work, executes it, and publishes the result back,
- the client later polls the relay for the result using the message ID.

## Purpose

This project is designed for scenarios where search execution must remain inside a private or controlled environment, while still allowing a remote or external client to submit work and receive results in a structured way.

## Repository Structure

- GS-WebGate-pom/ - Maven parent build and module configuration
- GS-WebGate-pom/GS-searcher/ - search execution component
- GS-WebGate-pom/GS-relay/ - relay service for work submission, polling, and result storage
- architecture/ - architecture documentation
- planning/ - planning and phase notes

## Status

The repository is currently being structured around the relay/searcher architecture and the initial REST-based polling workflow.
