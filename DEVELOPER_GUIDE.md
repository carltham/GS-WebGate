# TextAnalyser Swing UI - Developer Guide

## Architecture Overview

The Swing UI is built using Test-Driven Development (TDD) with 6 phases, each implementing a complete feature.

### Project Structure
```
TextAnalyser-UI-swing/
├── src/main/java/com/noprobit/tools/ui/    # Implementation files
│   ├── TextAnalyserApplication.java         # Main entry point
│   ├── MainWindow.java                      # Main UI container (6 phases)
│   ├── Phase 0: Application Launcher        # Startup & initialization
│   ├── Phase 1: Project Selection          # Project switching
│   ├── Phase 2: Analysis Execution         # Background analysis
│   ├── Phase 3: Report Display             # Results viewing
│   ├── Phase 4: Configuration Editor       # Settings management
│   └── Phase 5: Dashboard                  # Metrics & trends
├── src/test/java/com/noprobit/tools/ui/    # Test files (249 total)
└── pom.xml                                  # Maven configuration
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
cd TextAnalyser-pom/TextAnalyser-UI-swing
mvn clean install
```

### Running Tests
```bash
mvn clean test                              # All 249 tests
mvn test -Dtest=DashboardControllerTest    # Specific test
```

### Running Application
```bash
mvn exec:java -Dexec.mainClass="com.noprobit.tools.ui.TextAnalyserApplication"
```

---

## Architecture & Design

### MVC Pattern
- **Models:** ProjectMetadata, AnalysisReport, etc.
- **Views:** JPanel components (ProjectListPanel, DashboardPanel, etc.)
- **Controllers:** *Controller classes (ProjectSelectionController, DashboardController, etc.)

### Phase Dependencies
```
Phase 0: Application Launcher (foundation)
    ↓
Phase 1: Project Selection (depends on Phase 0)
    ↓
Phase 2: Analysis Execution (depends on Phase 1)
    ↓
Phase 3: Report Display (depends on Phase 2)
    ↓
Phase 4: Configuration Editor (parallel to Phase 2-3)
    ↓
Phase 5: Dashboard (depends on Phase 3)
```

### Threading Model
- **EDT (Event Dispatch Thread):** UI operations only
- **Worker Threads:** Long-running analysis via SwingWorker
- **Safe Synchronization:** volatile fields, synchronized blocks where needed

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

### TextAnalyserApplication
Entry point, initializes projects and main window.
```java
TextAnalyserApplication app = new TextAnalyserApplication();
// Application starts with default project
```

### MainWindow
Main UI container with tabbed interface for all phases.
```java
MainWindow window = new MainWindow(config, projects);
window.setVisible(true);
```

### DashboardController
Aggregates metrics across projects.
```java
DashboardController controller = new DashboardController();
controller.loadProjectStatistics("MyProject");
double avg = controller.getAverageViolationsPerFile();
```

### ReportController
Manages report display and export.
```java
ReportController controller = new ReportController();
controller.displayReport(analysisReport);
controller.exportToCSV("/path/to/file.csv");
```

### AnalysisWorker
Background thread for analysis (SwingWorker).
```java
AnalysisWorker worker = new AnalysisWorker();
worker.execute();  // Runs in background thread
```

---

## Testing Best Practices

### Unit Tests
```java
@Test
@DisplayName("Should calculate average violations correctly")
void testAverageViolationsPerFile() {
    DashboardController controller = new DashboardController();
    controller.loadProjectStatistics("Test");
    assertTrue(controller.getAverageViolationsPerFile() >= 0);
}
```

### Integration Tests
```java
@Test
@DisplayName("Should complete end-to-end workflow")
void testCompleteWorkflow() {
    // Setup
    TextAnalyserApplication app = new TextAnalyserApplication();
    
    // Execute phases
    // Assert results
}
```

### Mocking
```java
@Test
void testWithMock() {
    DashboardController controller = Mockito.mock(DashboardController.class);
    when(controller.getTotalViolations()).thenReturn(50);
    assertEquals(50, controller.getTotalViolations());
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
