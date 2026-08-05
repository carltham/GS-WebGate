# What Is This Good For?

This application is meant to support a two-part web gateway setup.

## The core idea

A searcher component runs on a private computer and is allowed to freely access the internet. It acts as the part of the system that can perform web searches without exposing the internal network directly.

The MQ component sits on the domain and acts as a queue for search requests. Other applications can submit search tasks to the queue without needing direct access to the private searcher.

## How it works

1. A client application on the domain submits a search request to the MQ queue.
2. The searcher polls the queue for new tasks.
3. The searcher performs the search, either as:
   - a generic web search, or
   - a targeted site-specific search.
4. The searcher pushes the result back to the MQ queue.
5. The original client polls for its result and processes it in its own way.

## What this is good for

This design is useful when:

- internal systems need web search capability without direct internet exposure
- a private machine is allowed to access the internet but the rest of the domain is not
- applications need an asynchronous, queue-based search workflow
- searches must be decoupled from the client application so they can be handled independently

## Typical use cases

- Enabling domain applications to request web search results safely
- Running search operations from a controlled private host
- Building a lightweight search broker between internal systems and external web sources
- Supporting both generic internet search and focused site search workflows

## In one sentence

It is a gateway and queue-based search system that lets internal applications request web searches through a private searcher and an MQ-backed workflow.
