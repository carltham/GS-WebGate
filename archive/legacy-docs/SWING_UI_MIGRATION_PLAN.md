# Swing UI Migration Plan - Top-Down TDD

**Date:** 2026-07-18  
**Status:** Active  
**Methodology:** Strict Top-Down Test-Driven Development (RED-GREEN-REFACTOR)  

---

## Executive Summary

This plan outlines a phased migration from CLI-only TextAnalyser to a modern Swing UI application, using strict TDD principles. Each phase follows RED-GREEN-REFACTOR, with test-first design and minimal implementation.

**Key Principles:**
1. Tests define behavior BEFORE code is written
2. Minimal code to pass tests (no gold-plating)
3. Refactor with tests passing
4. Each phase is independently deployable
5. Separation of concerns: UI, Business Logic, Persistence

---

## Architecture Overview

### Layering (6-Tier per Project Standards)

```
┌──────────────────────────────────────┐
│  6. User Interface Layer (Swing)     │  ← NEW
├──────────────────────────────────────┤
│  5. API/Controller Layer (UI Events) │  ← NEW
├──────────────────────────────────────┤
│  4. Application Service Layer        │  ← NEW (Orchestration)
├──────────────────────────────────────┤
│  3. Domain/Business Logic Layer      │  (EXISTING: Linters, Analyzers)
├──────────────────────────────────────┤
│  2. Repository/Persistence Layer     │  (EXISTING: FileDB)
├──────────────────────────────────────┤
│  1. External Services (Encoding)     │  (EXISTING: AdvancedEncodingEngine)
└──────────────────────────────────────┘
```

### Separation of Concerns

```
UI Layer (Swing Components)
    ↓ Events/Actions
Controller Layer (UI Logic)
    ↓ Service calls
Application Service Layer (Orchestration)
    ↓ Use cases
Domain Layer (Business Rules)
    ↓ Persistence
Database Layer (FileDB)
```

**Constraint:** UI layer NEVER directly accesses Domain or Persistence layers

---

## Phase Schedule

| Phase | Title | Duration | TDD Focus | Dependencies |
|-------|-------|----------|-----------|--------------|
| **0** | Foundation & Architecture | Week 1 | API Design & Interfaces | - |
| **1** | Project Management UI | Week 2 | Controller + Service tests | Phase 0 |
| **2** | Analysis Execution UI | Week 2 | Analysis workflow tests | Phase 1 |
| **3** | Report Viewing UI | Week 1 | Report display tests | Phase 2 |
| **4** | Configuration Management | Week 1 | Config UI tests | Phase 1 |
| **5** | Results Dashboard | Week 1 | Metrics + filtering tests | Phase 3 |
| **6** | Advanced Features | TBD | Auto-fix, encoding tools | Phase 5 |

**Total:** ~8 weeks for MVP, iterative beyond

---

## Phase 0: Foundation & Architecture (Week 1)

### Objective
Establish interfaces, service contracts, and test infrastructure WITHOUT implementation.

### TDD Approach: RED → Define Interfaces

**Step 1: Define Service Interfaces (Tests First)**

```java
// src/test/java/.../ui/TextAnalyserApplicationTest.java
@Test
public void applicationStartsWithValidConfiguration() throws Exception {
    // NOT YET IMPLEMENTED - This is RED
    TextAnalyserApplication app = new TextAnalyserApplication();
    assertNotNull(app.getMainWindow());
    assertTrue(app.getMainWindow().isVisible());
}

@Test
public void applicationLoadsConfigurationOnStartup() throws Exception {
    TextAnalyserApplication app = new TextAnalyserApplication();
    assertNotNull(app.getCurrentProjectName());
    assertNotNull(app.getCurrentSourcePath());
}
```

**Step 2: Define UI Service Contract**

```java
// src/main/java/.../ui/service/UIApplicationService.java (Interface only)
public interface UIApplicationService {
    
    // Project Management
    void loadProject(String projectName) throws IOException;
    void switchProject(String projectName) throws IOException;
    List<String> getAvailableProjects();
    ProjectMetadata getCurrentProject();
    
    // Analysis Execution
    AnalysisProgress startAnalysis();
    void cancelAnalysis();
    boolean isAnalysisRunning();
    
    // Report Management
    List<AnalysisReport> getReports(String projectName);
    AnalysisReport getLatestReport(String projectName);
    
    // Event Publishing
    void addAnalysisListener(AnalysisListener listener);
    void removeAnalysisListener(AnalysisListener listener);
}

// Event interfaces
public interface AnalysisListener {
    void onAnalysisStarted(AnalysisEvent event);
    void onAnalysisProgress(ProgressEvent event);
    void onAnalysisCompleted(AnalysisResultEvent event);
    void onAnalysisError(ErrorEvent event);
}
```

**Step 3: Define Domain Service Interfaces**

```java
// src/main/java/.../service/AnalysisService.java (Interface)
public interface AnalysisService {
    AnalysisResult analyzeProject(String projectName, Path sourcePath) 
        throws IOException;
    void addListener(AnalysisProgressListener listener);
    void cancel();
}

// src/main/java/.../service/ReportService.java (Interface)
public interface ReportService {
    List<AnalysisReport> getReports(String projectName) throws IOException;
    void generateReport(String projectName, String format) throws IOException;
}
```

### Deliverables (Phase 0)

- [ ] `UIApplicationService` interface
- [ ] `AnalysisListener` event interfaces
- [ ] `AnalysisService` wrapper interface
- [ ] `ReportService` interface
- [ ] Data transfer objects (DTO): ProjectMetadata, AnalysisReport, ProgressEvent
- [ ] Test infrastructure setup (JUnit 5, Mockito)
- [ ] Documentation of service contracts

### Tests to Write (RED Phase)

```
❌ applicationStartsWithValidConfiguration
❌ applicationLoadsConfigurationOnStartup
❌ switchProjectLoadsNewConfiguration
❌ analysisServiceIsInvoked
❌ progressEventsAreFired
❌ reportServiceFetchesCorrectReports
```

**Status:** All tests RED (fail because classes don't exist yet)

---

## Phase 1: Project Management UI (Week 2)

### Objective
Implement project selection, configuration loading, and navigation.

### TDD Workflow

#### RED: Write Tests

```java
// src/test/java/.../ui/controller/ProjectManagementControllerTest.java
public class ProjectManagementControllerTest {
    
    private ProjectManagementController controller;
    private UIApplicationService mockService;
    
    @BeforeEach
    public void setup() {
        mockService = mock(UIApplicationService.class);
        controller = new ProjectManagementController(mockService);
    }
    
    @Test
    public void loadProjectsPopulatesProjectList() throws Exception {
        List<String> projects = Arrays.asList("TextAnalyser", "GSPos");
        when(mockService.getAvailableProjects()).thenReturn(projects);
        
        controller.refreshProjectList();
        
        List<String> displayed = controller.getDisplayedProjects();
        assertEquals(projects, displayed);
        verify(mockService).getAvailableProjects();
    }
    
    @Test
    public void selectingProjectSwitchesContext() throws Exception {
        ProjectMetadata metadata = new ProjectMetadata("GSPos", "/path/to/src");
        when(mockService.getCurrentProject()).thenReturn(metadata);
        
        controller.selectProject("GSPos");
        
        verify(mockService).switchProject("GSPos");
        assertEquals("GSPos", controller.getCurrentProjectName());
    }
    
    @Test
    public void invalidProjectThrowsException() throws Exception {
        when(mockService.switchProject("Invalid"))
            .thenThrow(new IOException("Project not found"));
        
        assertThrows(IOException.class, () -> 
            controller.selectProject("Invalid")
        );
    }
}
```

#### GREEN: Minimal Implementation

```java
// src/main/java/.../ui/controller/ProjectManagementController.java
public class ProjectManagementController {
    
    private UIApplicationService service;
    private List<String> displayedProjects;
    private String currentProject;
    
    public ProjectManagementController(UIApplicationService service) {
        this.service = service;
        this.displayedProjects = new ArrayList<>();
    }
    
    public void refreshProjectList() {
        this.displayedProjects = service.getAvailableProjects();
    }
    
    public List<String> getDisplayedProjects() {
        return new ArrayList<>(displayedProjects);
    }
    
    public void selectProject(String projectName) throws IOException {
        service.switchProject(projectName);
        this.currentProject = projectName;
    }
    
    public String getCurrentProjectName() {
        return currentProject;
    }
}
```

#### REFACTOR: Improve Code

- Extract project loading to separate method
- Add error handling decorator
- Add logging
- Optimize list copying

### Deliverables (Phase 1)

- [ ] `ProjectManagementController` with tests
- [ ] `ProjectSelectionPanel` (Swing JPanel)
- [ ] Configuration display (read-only)
- [ ] Project switch button
- [ ] Error dialog for invalid projects
- [ ] Navigation to Analysis UI

### UI Components (Swing)

```
┌─────────────────────────────────────┐
│  Main Window                        │
├─────────────────────────────────────┤
│  ┌─────────────┐ ┌───────────────┐  │
│  │ Projects    │ │ Configuration │  │
│  │ • TextAn    │ │ Path: ...      │  │
│  │ • GSPos     │ │ Name: GSPos    │  │
│  └─────────────┘ └───────────────┘  │
│  ┌─────────────────────────────────┐ │
│  │ [Switch Project] [Start Analysis]│ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Tests Status (Phase 1)

```
✅ loadProjectsPopulatesProjectList
✅ selectingProjectSwitchesContext
✅ invalidProjectThrowsException
✅ refreshProjectListUpdatesDisplay
✅ getCurrentProjectNameReturnsSelected
```

---

## Phase 2: Analysis Execution UI (Week 2)

### Objective
Build analysis progress UI with real-time status updates.

### TDD: Analysis Workflow Tests

```java
// src/test/java/.../ui/controller/AnalysisControllerTest.java
@Test
public void startAnalysisInvokesService() throws Exception {
    when(mockService.startAnalysis()).thenReturn(progress);
    
    controller.startAnalysis();
    
    verify(mockService).startAnalysis();
    assertTrue(controller.isAnalysisRunning());
}

@Test
public void progressEventUpdatesUI() throws Exception {
    ProgressEvent event = new ProgressEvent(50, 100, "Processing File 50/100");
    ArgumentCaptor<AnalysisListener> listenerCaptor = 
        ArgumentCaptor.forClass(AnalysisListener.class);
    
    controller.startAnalysis();
    verify(mockService).addAnalysisListener(listenerCaptor.capture());
    
    AnalysisListener listener = listenerCaptor.getValue();
    listener.onAnalysisProgress(event);
    
    assertEquals(50, controller.getProgressPercentage());
    assertEquals("Processing File 50/100", controller.getStatusMessage());
}

@Test
public void analysisCompletionShows Results() throws Exception {
    AnalysisResultEvent resultEvent = new AnalysisResultEvent(
        100, 
        Arrays.asList(/* results */),
        System.currentTimeMillis()
    );
    
    ArgumentCaptor<AnalysisListener> listenerCaptor = 
        ArgumentCaptor.forClass(AnalysisListener.class);
    
    controller.startAnalysis();
    verify(mockService).addAnalysisListener(listenerCaptor.capture());
    
    listenerCaptor.getValue().onAnalysisCompleted(resultEvent);
    
    assertFalse(controller.isAnalysisRunning());
    assertTrue(controller.hasResults());
}

@Test
public void cancelAnalysisStopsProcessing() throws Exception {
    controller.startAnalysis();
    controller.cancelAnalysis();
    
    verify(mockService).cancelAnalysis();
    assertFalse(controller.isAnalysisRunning());
}
```

### UI Components

```
┌──────────────────────────────────┐
│ Analysis: GSPos                  │
├──────────────────────────────────┤
│ Progress: ████████░░ 50%         │
│ Status: Processing File 50/100   │
│                                  │
│ Files: 50/100                    │
│ Time: 2m 35s                     │
│                                  │
│ ┌────────────────────────────────┐│
│ │ Class Issues found: 25         ││
│ │ Method Issues: 12              ││
│ │ Import Issues: 3               ││
│ └────────────────────────────────┘│
│                                  │
│ [Cancel]  [View Results]         │
└──────────────────────────────────┘
```

### Deliverables (Phase 2)

- [ ] `AnalysisController` with event handling
- [ ] Progress bar and status display
- [ ] Real-time metrics updates
- [ ] Cancel button with graceful shutdown
- [ ] Results summary display
- [ ] Error handling and retry

---

## Phase 3: Report Viewing UI (Week 1)

### Objective
Display CSV and Markdown reports in user-friendly format.

### TDD: Report Display Tests

```java
@Test
public void reportDataIsLoadedAndDisplayed() throws Exception {
    AnalysisReport report = mock(AnalysisReport.class);
    when(mockService.getLatestReport("GSPos")).thenReturn(report);
    
    controller.loadReport("GSPos");
    
    assertTrue(controller.isReportLoaded());
    assertEquals(report, controller.getCurrentReport());
}

@Test
public void violationsAreFilteredByType() throws Exception {
    List<Violation> allViolations = Arrays.asList(
        new Violation("Class", "NameError", "critical"),
        new Violation("Method", "NameError", "warning"),
        new Violation("Class", "SpecialChar", "warning")
    );
    
    controller.setViolations(allViolations);
    controller.filterByType("Class");
    
    List<Violation> filtered = controller.getFilteredViolations();
    assertEquals(2, filtered.size());
}

@Test
public void sortingWorks() throws Exception {
    controller.sortBy(SortField.SEVERITY, SortOrder.DESCENDING);
    
    List<Violation> sorted = controller.getViolations();
    assertEquals("critical", sorted.get(0).getSeverity());
    assertEquals("warning", sorted.get(sorted.size()-1).getSeverity());
}
```

### UI Components

```
┌────────────────────────────────────────┐
│ Report: GSPos (2026-07-18)             │
├────────────────────────────────────────┤
│ Filter: [All ▼] Sort: [Severity ▼]    │
├────────────────────────────────────────┤
│ Violations: 142 | Compliance: 34%      │
├────────────────────────────────────────┤
│ ┌──────────────────────────────────────┐│
│ │ Class Name │ Issue │ Severity │ Sugg ││
│ │ PaymentInfo│ Naming│ ERROR    │ Ca... ││
│ │ configPnl │ Naming│ ERROR    │ Co... ││
│ │ mainPanel │ Naming│ WARNING  │ Mai... ││
│ │ JPanelCrv │ Naming│ INFO     │ Jpa... ││
│ └──────────────────────────────────────┘│
│ [Export CSV] [Export MD] [Refresh]     │
└────────────────────────────────────────┘
```

---

## Phase 4: Configuration Management (Week 1)

### Objective
Edit configuration files and switch projects from UI.

### TDD: Configuration Tests

```java
@Test
public void configurationCanBeEdited() throws Exception {
    ConfigurationPanel panel = new ConfigurationPanel(mockService);
    
    panel.setProjectName("NewProject");
    panel.setSourcePath("/new/path");
    panel.save();
    
    verify(mockService).updateConfiguration("NewProject", "/new/path");
}

@Test
public void pathValidationPreventsInvalid Paths() throws Exception {
    ConfigurationPanel panel = new ConfigurationPanel(mockService);
    
    panel.setSourcePath("/nonexistent/path");
    
    assertFalse(panel.validatePath());
    assertTrue(panel.getErrorMessage().contains("not found"));
}
```

---

## Phase 5: Results Dashboard (Week 1)

### Objective
Aggregate metrics across projects and show trends.

### Key Features
- Project comparison
- Compliance trend over time
- Violation heatmap
- Export aggregated reports

---

## Phase 6: Advanced Features (TBD)

### Future Enhancements
- Auto-fix suggestions with preview
- Encoding conversion tools
- IDE integration
- CI/CD pipeline integration
- Real-time analysis on save

---

## Implementation Guidelines

### TDD Strict Adherence

**ALWAYS:**
1. Write test FIRST (RED phase)
2. Watch test FAIL
3. Write minimal code to PASS test (GREEN phase)
4. REFACTOR while keeping tests green
5. Commit test + implementation together

**NEVER:**
- Write code without failing test
- Write production code before test exists
- Commit passing code without test
- Gold-plate or over-engineer (more than test requires)

### Architecture Constraints

**UI Layer MUST:**
- Use only Swing components
- Never call Domain layer directly
- Never call Persistence layer directly
- Only call UIApplicationService or Controllers
- Emit events, never query state

**Controller Layer MUST:**
- Orchestrate service calls
- Transform events to UI updates
- Handle UI state management
- Never contain business logic

**Service Layer MUST:**
- Implement UIApplicationService
- Delegate to Domain services
- Handle transactions and persistence
- Emit domain events

### Testing Requirements

| Layer | Test Type | Mock | Coverage |
|-------|-----------|------|----------|
| UI | Unit + Integration | Services | 80%+ |
| Controller | Unit | Services | 90%+ |
| Service | Unit | Domain | 90%+ |
| Domain | Unit | Persistence | 95%+ |

### Code Quality

**Apply Project Standards:**
- Naming conventions from NAMING_CONVENTIONS.md
- 6-tier architecture from ARCHITECTURE_AND_LAYERING.md
- Security requirements from SECURITY_AND_ISOLATION.md
- Logging from EVENTS_AND_OBSERVABILITY.md
- Java-specific rules from language-specific/java-template.md

---

## Dependency Management

### New Maven Dependencies

```xml
<!-- Swing (included in JDK) -->
<!-- No new dependencies for base Swing -->

<!-- Testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.10.3</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.3.1</version>
    <scope>test</scope>
</dependency>

<!-- Assertion library (already have) -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.10.3</version>
    <scope>test</scope>
</dependency>
```

---

## Success Criteria

### Phase 0 (Foundation)
- [ ] All service interfaces defined
- [ ] All DTO classes created
- [ ] Test infrastructure ready
- [ ] 100% of interfaces have test stubs

### Phase 1 (Project Mgmt)
- [ ] Project selection works
- [ ] Configuration switches
- [ ] 90%+ test coverage
- [ ] No direct domain layer access

### Phase 2 (Analysis)
- [ ] Analysis runs with progress
- [ ] Real-time events work
- [ ] Cancellation works
- [ ] 90%+ test coverage

### Phase 3 (Reports)
- [ ] Reports display correctly
- [ ] Filtering works
- [ ] Sorting works
- [ ] 85%+ test coverage

### Overall
- [ ] Zero code without tests
- [ ] Zero tests written after code
- [ ] All layers properly separated
- [ ] No security violations
- [ ] Proper logging everywhere
- [ ] Clean build with zero warnings

---

## Risk Mitigation

| Risk | Mitigation | Contingency |
|------|-----------|-------------|
| **Swing complexity** | Start simple, add features incrementally | Use layout managers effectively |
| **Event handling bugs** | Comprehensive event tests | Implement event queue debugging |
| **Performance issues** | Profile early, optimize late | Async analysis via threads |
| **Test brittleness** | Mock carefully, avoid implementation details | Use spy instead of mock when needed |
| **Architecture violations** | Code review every phase | Add compile-time checks via architecture tools |

---

## Git Commit Strategy

**Commit Pattern:**
```
[Phase N] Feature: Brief description

- Test added (RED)
- Implementation (GREEN)
- Refactoring improvements
- TODO: None

Follows TDD RED-GREEN-REFACTOR cycle.
Test coverage: X%
Architecture: 6-tier separated
Co-Authored-By: Claude Haiku 4.5 <noreply@anthropic.com>
```

**Never commit:**
- Failing tests
- Code without tests
- Tests without corresponding code
- Untested refactoring

---

## Signoff

**Plan Created:** 2026-07-18  
**Status:** Ready for Phase 0 kickoff  
**Owner:** Technical Lead  
**Reviewed By:** Architecture Committee  

**Approval for Phase 0:** [ ] Approved [ ] Needs Changes

---

**Next Step:** Begin Phase 0 - Writing service interface tests

