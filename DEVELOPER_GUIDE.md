# GS-WebGate Developer Guide

## Architecture Overview

The GS-WebGate project will follow a contract-driven, top-down TDD approach. The client-facing behavior defines the contract first, and every lower layer must adapt to satisfy that contract.

## Contract-Driven Top-Down TDD

### Core principle
- The client contract is the primary authority.
- Tests are written from the outside in.
- Lower layers are adjusted to match the contract of the layer above them.

### Workflow
1. Start with an end-to-end or client-level test that defines the expected behavior.
2. Let that test fail at the first missing boundary.
3. Add the next test at the next layer below.
4. Continue until the lowest implementation layer is reached.
5. Re-run tests from the bottom up and fix the implementation to satisfy the higher-level contract.

### Rules
- Prefer real implementations over mocks.
- Mocks should only be used outside the system, such as for external calls or network boundaries.
- Do not hide integration mismatches with overly permissive test doubles.
- Keep tests focused on observable behavior, not internal implementation details.

### Practical checklist
- Define the request and response contract first.
- Write the client-facing test.
- Add the handler or orchestration test next.
- Then add the service test.
- Implement the adapter or integration layer last.

See [planning/TDD_TOP_DOWN_GUIDE.md](planning/TDD_TOP_DOWN_GUIDE.md) for the full working pattern.

### Project Structure
```
GS-WebGate-pom/
├── GS-WebGate/                             # Private worker / gateway module
├── GS-relay/                                  # Queue and correlation module
├── config/                                 # Shared configuration
└── pom.xml                                 # Parent Maven build
```

---

## Development Setup

### Prerequisites
```bash
# Java 11+
java -version

# Maven 3.6+
mvn -version

# Git
git --version
```

### Initial Setup
```bash
cd GS-WebGate-pom
mvn clean install
```

### Running Tests
```bash
mvn clean test
```

### Running Application
```bash
mvn -pl GS-WebGate spring-boot:run
```

---

## Architecture & Design

### Application Pattern
- **Request/Response Contracts:** define the queue payloads first
- **Services:** contain the core business behavior
- **Adapters:** bridge the queue and the worker execution layer

### Delivery Phases
```
Phase 0: Contracts and test harness
    ↓
Phase 1: GS-relay queue behavior
    ↓
Phase 2: GS-WebGate worker loop
    ↓
Phase 3: Integration and resilience
```

### Concurrency Model
- **Worker loop:** processes queue entries asynchronously
- **Queue boundary:** keeps the worker isolated from direct client coupling
- **Operational safety:** retries and timeouts should be handled at the boundary

---

## Adding a New Feature

### Step 1: Write Tests (RED)
```bash
# Create TestFile in src/test/java/com/noprobit/tools/ui/
# Write all test cases for the feature
mvn test  # Verify tests fail (RED phase)
```

### Step 2: Implement (GREEN)
```bash
# Create implementation in src/main/java/com/noprobit/tools/ui/
# Write minimal code to pass tests
mvn test  # Verify tests pass (GREEN phase)
```

### Step 3: Refactor (REFACTOR)
```bash
# Review code for clarity and optimization
# Optimize performance if needed
# Verify design patterns applied
mvn test  # Verify tests still pass
```

### Step 4: Commit
```bash
git add .
git commit -m "Feature: [Description]

- Implementation detail 1
- Implementation detail 2

Tests: N tests added, all passing."
```

---

## Code Standards

### Naming Conventions
- Classes: PascalCase (ProjectListPanel)
- Methods: camelCase (loadProjectStatistics)
- Constants: UPPER_SNAKE_CASE (DEFAULT_SOURCE_PATH)
- Variables: camelCase (projectName)

### Comments
- Only when WHY is non-obvious
- No comments for what code does (use clear naming)
- Avoid comments on self-documenting code

### Error Handling
- Log all errors with appropriate level
- Use try-catch for external APIs only
- Trust framework/internal guarantees

### Testing
- One assertion per test when possible
- Test names describe what they test
- Use DisplayName for clarity
- Mock external dependencies

---

## Key Classes

### GSWebGateApplication
Entry point for the gateway module.
```java
GSWebGateApplication app = new GSWebGateApplication();
```

### QueueService
Coordinates request and response storage.
```java
QueueService service = new QueueService();
String requestId = service.enqueueRequest(request);
```

### WorkerOrchestrator
Coordinates the worker loop and result publication.
```java
WorkerOrchestrator orchestrator = new WorkerOrchestrator();
orchestrator.processNextRequest();
```

---

## Testing Best Practices

### Unit Tests
```java
@Test
@DisplayName("Should calculate average violations correctly")
void testAverageViolationsPerFile() {
    QueueService controller = new QueueService();
    controller.loadProjectStatistics("Test");
    assertTrue(controller.getAverageViolationsPerFile() >= 0);
}
```

### Integration Tests
```java
@Test
@DisplayName("Should complete the queue workflow")
void testCompleteQueueWorkflow() {
    // Setup
    QueueService service = new QueueService();

    // Execute request and response flow
    // Assert result correlation
}
```

### Mocking
```java
@Test
void testWithMock() {
    QueueService service = Mockito.mock(QueueService.class);
    when(service.enqueueRequest(any())).thenReturn("req-1");
    assertEquals("req-1", service.enqueueRequest(new RequestPayload()));
}
```

---

## Performance Considerations

### Memory
- Clear large collections when done
- Use GridLayout for scalability
- Test with 20+ projects regularly

### CPU
- Use background threads (SwingWorker)
- Cache calculations where appropriate
- Optimize trending algorithm

### Responsiveness
- Keep EDT operations < 100ms
- Move long operations to worker threads
- Use progress updates for user feedback

---

## Debugging

### Enable Logging
```java
java.util.logging.Logger logger = 
    java.util.logging.Logger.getLogger("com.noprobit.tools.ui");
logger.setLevel(java.util.logging.Level.FINE);
```

### Debug Tests
```bash
mvn test -Dtest=SpecificTest -X
```

### IntelliJ IDEA
- Set breakpoints in test code
- Run test with debugger
- Step through execution

---

## Common Issues & Solutions

### Tests Failing After Changes
```bash
mvn clean test  # Clean rebuild
```

### Application Won't Start
- Check MainWindow initialization
- Verify project list not empty
- Review exception logs

### Memory Leaks
- Ensure listeners are removed
- Dispose UI components properly
- Check worker thread cleanup

---

## Contributing

1. Create feature branch: `git checkout -b feature/name`
2. Write tests first (TDD)
3. Implement feature
4. Ensure all tests pass: `mvn clean test`
5. Create pull request with description
6. Code review approval required

---

## Release Checklist

- [ ] All 249 tests passing
- [ ] Zero compiler warnings
- [ ] Code review complete
- [ ] Documentation updated
- [ ] Performance tested
- [ ] Tagged in Git

---

## Resources

- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Maven Documentation](https://maven.apache.org/)
- [JUnit 5](https://junit.org/junit5/)
- [Mockito](https://site.mockito.org/)
