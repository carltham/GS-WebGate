# Contract-Driven Top-Down TDD for GS-WebGate

This project uses a contract-driven, top-down TDD workflow.

## Core Rule

The contract between the caller and the system is the authority. Lower layers must adapt to serve that contract.

## Workflow

1. Start with a client-visible test for the request/result flow.
2. Let that test fail at the first missing boundary.
3. Add the next layer below with the smallest implementation needed.
4. Repeat until the infrastructure layer is covered.
5. Re-run the stack from the bottom up and fix anything that violates the contract.

## Rules

- Prefer real implementations over mocks whenever possible.
- Use mocks only at true external boundaries.
- Keep tests focused on observable behavior.
- If the contract changes, update the tests first.

## Practical Checklist

- Write the request/response contract first.
- Add the client-facing test.
- Add the handler or orchestration test next.
- Add the service test after that.
- Implement the infrastructure layer last.
- Re-run the full flow after each step.

## Expected Outcome

This keeps the implementation aligned with the actual product contract and avoids hidden mismatches between GS-mq and GS-WebGate.
