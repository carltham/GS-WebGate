# GS-WebGate

GS-WebGate is a private-search gateway built around a simple pattern:

- a client submits a search request,
- a lightweight relay accepts and logs the request/response flow,
- a searcher running on a private machine performs the actual search and returns the result.

## Purpose

This project is designed for scenarios where search execution must remain inside a private or controlled environment, while still allowing a remote or external client to submit work and receive results in a structured way.

## Repository Structure

- GS-WebGate-pom/ - Maven parent build and module configuration
- GS-WebGate-pom/GS-searcher/ - search execution component
- GS-WebGate-pom/GS-relay/ - relay service for request/response flow and logging
- architecture/ - architecture documentation
- planning/ - planning and phase notes

## Status

The repository is currently being structured around the relay/searcher architecture and the initial request/response flow.
