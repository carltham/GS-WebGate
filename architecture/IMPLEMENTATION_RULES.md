# Implementation Rules for GS-WebGate Relay

## Architecture rules
- Keep the relay flow explicit: submit -> pending -> claimed -> completed -> retrieved.
- Keep responsibilities separated by layer:
  - controller: HTTP boundary
  - service/store: business logic
  - repository: persistence access
  - domain/model: data objects and contracts
  - persistence package: JPA entities and repositories

## Planning rules
- Define the smallest useful implementation slice first.
- Write the contract and acceptance criteria before implementation.
- Do not introduce extra abstractions before the first happy path is working.

## Implementation rules
- Use package names that match the responsibility.
- Keep naming consistent with Java conventions.
- Do not mix persistence concerns into the wrong package.
- Verify the build after structural changes.
