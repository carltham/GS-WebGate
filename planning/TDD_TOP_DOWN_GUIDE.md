# Contract-Driven Top-Down TDD for GS-WebGate

This project will use a contract-driven, top-down TDD approach.

## Core Rule

The client contract is the primary authority. All lower layers must adapt to satisfy the contract defined by the layer above.

## Workflow

### Phase 1: Push Down
1. Start with an end-to-end test that describes the client-visible behavior.
2. Run it and let it fail at the first missing boundary.
3. Add the next test at the next layer below.
4. Continue until the lowest implementation layer is reached.

### Phase 2: Pop Up
1. Re-run tests from the bottom up.
2. If a lower layer fails against the contract above it, fix the lower layer.
3. Keep the higher layer's contract intact.
4. Do not weaken the contract to make a lower layer pass.

## Rules

- Prefer real implementations over mocks whenever possible.
- Mocks should only be used outside the system, such as for external calls or network boundaries.
- Let the end-to-end test define the real interface.
- Keep tests focused on observable behavior, not implementation details.
- When a contract changes, update the tests first and then adapt the implementation.

## Practical Checklist

- Write the client-facing test first.
- Define the expected request and response shape.
- Add the handler or orchestration test next.
- Add the service test after that.
- Only then implement the adapter or infrastructure layer.
- Run the full stack after each upward step.

## Expected Outcome

This approach keeps the system aligned around real contracts, surfaces integration issues early, and prevents hidden mismatches between layers.
