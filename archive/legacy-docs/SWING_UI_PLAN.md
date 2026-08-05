# Swing UI - Phased Migration Plan (Documents Only)

**Date:** 2026-07-18  
**Methodology:** Strict Top-Down Test-Driven Development (RED-GREEN-REFACTOR)  
**Status:** Planning Phase - No Code Yet

---

## Phase Structure

### Phase 0: Application Launcher (Week 1)
**Purpose:** Minimal working UI that starts the application

**RED Tests to Write:**
1. Application starts with no crashes
2. Main window appears and is visible
3. Configuration loads from file
4. Project list displays
5. Window closes without error

**Green Implementation:** 
- Just enough code to pass above tests
- Single JFrame with project dropdown
- No functionality yet

**Files to Create:**
- `TextAnalyserApplication.java` - Main entry point
- `MainWindow.java` - Simple JFrame container
- `ProjectListPanel.java` - Shows project list
- Test classes for each above

---

### Phase 1: Project Selection (Week 2)
**Purpose:** Switch between projects, load configuration

**RED Tests to Write:**
1. Select project from dropdown
2. Configuration updates when project selected
3. Invalid project shows error
4. Configuration persists
5. Project list refreshes on demand

**Green Implementation:**
- Project dropdown selection listener
- Configuration reload on selection
- Error dialog on failure
- Refresh button

**Files to Create:**
- `ProjectSelectionPanel.java` - Dropdown + buttons
- `ConfigurationDisplayPanel.java` - Read-only config display
- Test classes for each

---

### Phase 2: Analysis Execution (Week 2)
**Purpose:** Start analysis, show progress

**RED Tests to Write:**
1. Analysis starts without crash
2. Progress bar updates (0% → 100%)
3. Status message changes during analysis
4. Cancel button stops analysis
5. Completion message shows on finish
6. Error message shows if analysis fails

**Green Implementation:**
- Start/Cancel buttons
- Progress bar
- Status label
- Run analysis in background thread
- Event listeners for progress

**Files to Create:**
- `AnalysisPanel.java` - UI for analysis
- `AnalysisWorker.java` - Background thread
- Test classes for threading, events

---

### Phase 3: Report Display (Week 1)
**Purpose:** Show analysis results

**RED Tests to Write:**
1. Report loads after analysis
2. Violation table displays
3. Filter by violation type works
4. Sort by column works
5. Export to CSV works
6. Export to Markdown works

**Green Implementation:**
- Table with violation data
- Filter dropdown
- Sort buttons
- Export buttons

**Files to Create:**
- `ReportPanel.java` - Report display
- `ViolationTable.java` - Custom table model
- Test classes for filtering, sorting

---

### Phase 4: Configuration Editor (Week 1)
**Purpose:** Edit project configuration from UI

**RED Tests to Write:**
1. Project name field editable
2. Source path field editable
3. Path validation works (non-empty, exists)
4. Save button persists changes
5. Cancel button discards changes
6. Error shows on invalid path

**Green Implementation:**
- Text fields for name and path
- Save/Cancel buttons
- Path validation
- Error dialog

**Files to Create:**
- `ConfigurationEditorPanel.java` - Edit UI
- Test classes for validation, persistence

---

### Phase 5: Dashboard (Week 1)
**Purpose:** Aggregate metrics, show trends

**RED Tests to Write:**
1. Multiple projects' metrics display
2. Compliance rate shows for each
3. Violation count shows for each
4. Trend line shows improvement/decline
5. Date picker filters by date range

**Green Implementation:**
- Multi-project table
- Basic chart for trends
- Date range filter

**Files to Create:**
- `DashboardPanel.java` - Dashboard view
- Test classes for metrics

---

## Architecture (No Implementation Yet)

### Layer Structure (6-Tier per Project Standards)

```
Layer 6: Swing UI Components
  JFrame, JPanels, JButtons, JTables
  ↓ (call via listeners/action events)
  
Layer 5: UI Controllers
  Handle events, update UI, call services
  ↓ (call)
  
Layer 4: Application Services (UIApplicationService interface)
  Orchestrate domain + persistence
  ↓ (call)
  
Layer 3: Domain Services (existing)
  ClassAnalysisEngine, ClassNameValidator, etc.
  ↓ (call)
  
Layer 2: Persistence (FileDB - existing)
  Analysis storage and retrieval
  ↓
  
Layer 1: External Services (AdvancedEncodingEngine - existing)
  File encoding detection
```

### Key Constraint
**UI layer NEVER directly accesses Domain or Persistence layers.**
All access flows through UIApplicationService interface.

---

## Test-First Specifications

### RED Phase - Tests Define Behavior

**Every feature starts with failing tests that specify:**
- What methods exist
- What they return
- What exceptions they throw
- What side effects occur
- What events fire

**Example (Phase 0):**
```
ApplicationStartupTest
  - testApplicationStartsWithoutCrash()
  - testMainWindowIsVisible()
  - testConfigurationLoads()
  - testProjectListPopulated()
  - testWindowClosesGracefully()

All tests FAIL initially (RED phase)
```

**Example (Phase 1):**
```
ProjectSelectionTest
  - testSelectingProjectLoadsConfiguration()
  - testInvalidProjectThrowsException()
  - testConfigurationPersists()
  - testProjectListRefreshes()

All tests FAIL initially (RED phase)
```

### GREEN Phase - Minimal Code

Once tests exist and fail, write ONLY enough code to pass them:
- No extra features
- No optimization
- No "nice-to-have" improvements
- Minimal, direct implementation

### REFACTOR Phase - Improve Code

With tests passing, improve code:
- Better variable names
- Extract common patterns
- Add logging
- Optimize hot paths
- Keep tests passing throughout

---

## TDD Discipline Rules (Non-Negotiable)

1. **Never write code without failing test first**
2. **Write minimal code to pass test**
3. **All changes must pass existing tests**
4. **Refactor only with green tests**
5. **Commit test + code together**
6. **No gold-plating or assumptions**

---

## File Structure (After All Phases)

```
TextAnalyser-jar/
  src/
    main/java/com/noprobit/tools/
      ui/
        TextAnalyserApplication.java
        MainWindow.java
        panels/
          ProjectListPanel.java
          ProjectSelectionPanel.java
          ConfigurationDisplayPanel.java
          ConfigurationEditorPanel.java
          AnalysisPanel.java
          ReportPanel.java
          DashboardPanel.java
        components/
          ViolationTable.java
          ProgressPanel.java
        workers/
          AnalysisWorker.java
      service/
        UIApplicationService.java (interface only)
        UIApplicationServiceImpl.java
        (event classes)
        
    test/java/com/noprobit/tools/
      ui/
        ApplicationStartupTest.java
        ProjectSelectionTest.java
        AnalysisExecutionTest.java
        ReportDisplayTest.java
        ConfigurationEditorTest.java
        DashboardTest.java
        (all other test classes)
```

---

## Dependencies (To Add to pom.xml)

**Swing:** Included in JDK - no dependency needed

**Testing:**
- JUnit 5 (already have)
- Mockito (already have)
- AssertJ (for readable assertions)

```xml
<!-- Only if adding AssertJ -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.1</version>
    <scope>test</scope>
</dependency>
```

---

## Development Workflow per Phase

1. **Create Test File** → Write failing tests
2. **Run Tests** → Verify all fail (RED)
3. **Create Implementation** → Write minimal code
4. **Run Tests** → Verify all pass (GREEN)
5. **Refactor** → Improve code quality
6. **Run Tests** → Verify still pass
7. **Commit** → Test + implementation together

---

## Quality Checkpoints (Per Phase)

- [ ] All tests written BEFORE implementation
- [ ] All tests passing
- [ ] No untested code
- [ ] No code without corresponding test
- [ ] No gold-plating beyond test requirements
- [ ] Clean compilation
- [ ] Proper layer separation verified
- [ ] No direct UI→Domain access

---

## Security & Standards Compliance

**Project Standards to Follow:**
- Java naming conventions (from project-settings)
- 6-tier architecture (from project-settings)
- TDD discipline (from project-settings)
- No direct layer jumping
- Proper logging per standards
- Event-based communication

---

## Estimated Timeline

| Phase | Duration | Tests | Code | Notes |
|-------|----------|-------|------|-------|
| 0 | Week 1 | 5 | ~100 lines | Application launcher |
| 1 | Week 2 | 10 | ~200 lines | Project switching |
| 2 | Week 2 | 12 | ~300 lines | Analysis + threading |
| 3 | Week 1 | 8 | ~250 lines | Report display |
| 4 | Week 1 | 6 | ~200 lines | Configuration editor |
| 5 | Week 1 | 6 | ~300 lines | Dashboard |
| **Total** | **8 weeks** | **~47** | **~1350 lines** | MVP |

---

## Next Steps (When Ready)

1. Create test files for Phase 0
2. Write failing tests (RED)
3. Run tests to verify they fail
4. Write minimal implementation (GREEN)
5. Refactor if needed
6. Commit
7. Move to Phase 1

**This document is PLANNING ONLY - No code to be written until tests exist.**

---

**Status:** Ready for Phase 0 test creation  
**Awaiting:** Explicit instruction to begin writing tests

