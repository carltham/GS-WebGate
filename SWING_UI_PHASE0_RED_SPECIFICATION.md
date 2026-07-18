# Phase 0: Application Launcher - RED Tests Specification

**Date:** 2026-07-18  
**Phase:** 0 - Foundation  
**Stage:** RED - Test Specification (What tests to write)  
**Status:** Ready for test implementation

---

## Overview

Phase 0 creates a minimal working application with:
- Main window that appears on startup
- Project list displayed
- Configuration loaded and shown
- Graceful shutdown

**All behavior is specified as failing tests.**
**No implementation code yet.**

---

## Test File 1: ApplicationStartupTest

### Purpose
Verify application can start without crashing.

### Test Specifications (NOT CODE YET - just descriptions)

**Test 1.1: Application Starts**
- Description: Create TextAnalyserApplication instance
- Expected: No exception thrown
- Verification: Application object exists

**Test 1.2: Main Window Created**
- Description: After startup, request main window
- Expected: Window object returned (not null)
- Verification: Window exists

**Test 1.3: Main Window is Visible**
- Description: Check if main window is visible
- Expected: isVisible() returns true
- Verification: Window displayed on screen

**Test 1.4: Configuration Loads**
- Description: After startup, request current project configuration
- Expected: ProjectMetadata returned (not null)
- Verification: Contains projectName and sourcePath

**Test 1.5: Project List Populated**
- Description: Request available projects
- Expected: List returned with at least one project
- Verification: List size > 0

**Test 1.6: Shutdown Closes Window**
- Description: Call shutdown() on application
- Expected: Window closes without exception
- Verification: Window isVisible() returns false

**Test 1.7: Double Shutdown Safe**
- Description: Call shutdown() twice
- Expected: No exception on second call
- Verification: Idempotent operation

---

## Test File 2: ProjectListPanelTest

### Purpose
Verify project list UI component displays correctly.

### Test Specifications

**Test 2.1: Panel Created**
- Description: Create ProjectListPanel
- Expected: Panel object created (not null)
- Verification: Object exists

**Test 2.2: Projects Displayed**
- Description: Create panel with projects list
- Expected: Projects shown in dropdown/list
- Verification: Each project name visible

**Test 2.3: Initial Selection**
- Description: Check which project is initially selected
- Expected: First project or configured project selected
- Verification: getSelectedProject() returns valid value

**Test 2.4: Selection Listener Works**
- Description: Add listener, select different project
- Expected: Listener called with new project
- Verification: onProjectSelected() fires correct event

**Test 2.5: Empty List Handled**
- Description: Create panel with empty project list
- Expected: Panel displays gracefully
- Verification: No exception, shows "No projects" or similar

---

## Test File 3: ConfigurationDisplayPanelTest

### Purpose
Verify configuration display component shows current settings.

### Test Specifications

**Test 3.1: Panel Created**
- Description: Create ConfigurationDisplayPanel
- Expected: Panel object created
- Verification: Object exists

**Test 3.2: Project Name Displayed**
- Description: Set project name, display panel
- Expected: Project name shown in label
- Verification: getText() contains project name

**Test 3.3: Source Path Displayed**
- Description: Set source path, display panel
- Expected: Path shown in label
- Verification: getText() contains source path

**Test 3.4: Updates on Configuration Change**
- Description: Change configuration, notify panel
- Expected: Panel updates display
- Verification: New values shown

**Test 3.5: Read-Only Fields**
- Description: Try to edit displayed fields directly
- Expected: Cannot modify (fields disabled/read-only)
- Verification: setText() has no effect

---

## Test File 4: MainWindowTest

### Purpose
Verify main window container and layout.

### Test Specifications

**Test 4.1: Window Created**
- Description: Create MainWindow instance
- Expected: JFrame object created
- Verification: isInstanceOf(JFrame)

**Test 4.2: Title Set**
- Description: Check window title
- Expected: Title contains "TextAnalyser"
- Verification: getTitle().contains("TextAnalyser")

**Test 4.3: Close Button Works**
- Description: Click close button (X)
- Expected: Window closes
- Verification: isVisible() returns false

**Test 4.4: Panels Added to Window**
- Description: Create window with default panels
- Expected: Panels visible in container
- Verification: getContentPane().getComponentCount() > 0

**Test 4.5: Window Size Reasonable**
- Description: Check window dimensions
- Expected: Width > 400px, Height > 300px
- Verification: getWidth() and getHeight() > minimums

**Test 4.6: Window Initially Centered**
- Description: Check initial window position
- Expected: Window not at (0,0)
- Verification: getLocation() reasonable

---

## Test File 5: IntegrationStartupTest

### Purpose
Verify complete startup sequence works end-to-end.

### Test Specifications

**Test 5.1: Startup Sequence**
- Description: Start app → window visible → projects loaded
- Expected: All three steps complete without error
- Verification: Each step verified in order

**Test 5.2: Configuration Matches Projects**
- Description: Current project from config must exist in project list
- Expected: Config project name in available projects list
- Verification: getAvailableProjects() contains getCurrentProject().getName()

**Test 5.3: No Startup Exceptions**
- Description: Normal startup path throws no exceptions
- Expected: Clean startup without errors
- Verification: No try-catch needed

**Test 5.4: No Console Errors**
- Description: Check system err/out during startup
- Expected: No error messages printed
- Verification: Logging only, no errors

**Test 5.5: Memory Reasonably Used**
- Description: Check memory after startup
- Expected: < 100MB used (rough check)
- Verification: Runtime.getRuntime().totalMemory()

---

## Test Dependencies

### Required Test Utilities

**Mock Objects Needed:**
- Mock UIApplicationService (for controllers to use)
- Mock AnalysisListener (for event testing)
- Mock ProjectListener (for event testing)

**Test Fixtures Needed:**
- Sample project list: ["TextAnalyser", "GSPos"]
- Sample configuration: name="TextAnalyser", path="src/main/java"
- Sample ProjectMetadata objects

**Assertions Needed:**
- assertNotNull()
- assertEquals()
- assertTrue()
- assertFalse()
- assertThrows()

---

## Test Execution Order

**Phase 0 tests should run in this order:**

1. ApplicationStartupTest (verify app launches)
2. MainWindowTest (verify window structure)
3. ProjectListPanelTest (verify projects display)
4. ConfigurationDisplayPanelTest (verify config display)
5. IntegrationStartupTest (verify complete flow)

---

## Expected Test Results After Implementation

**RED Phase (before any code):**
- All tests: ❌ FAIL (classes don't exist)

**GREEN Phase (after minimal implementation):**
- All tests: ✅ PASS (implementation added)

**REFACTOR Phase:**
- All tests: ✅ PASS (code improved, tests still pass)

---

## Acceptance Criteria (Phase 0 Complete)

- [ ] All test files created
- [ ] All tests written (RED - all fail)
- [ ] All tests pass after implementation (GREEN)
- [ ] No code before test existed
- [ ] No test without corresponding code
- [ ] Code only as minimal as needed
- [ ] No gold-plating or extra features
- [ ] Clean compilation
- [ ] All tests still pass after refactoring

---

## Notes on Test Writing

### What Tests Should Specify
- Method names and signatures
- Parameter types
- Return types
- Exceptions thrown
- Event firing
- State changes
- Edge cases

### What Tests Should NOT Do
- Implement application logic
- Assume implementation details
- Test private methods
- Test implementation-specific behavior
- Make UI assumptions (just behavior)

### Good Test Characteristics
- Single responsibility (test one thing)
- Clear name (test name describes what's tested)
- Independent (no test depends on another)
- Deterministic (same result every time)
- Fast (run in milliseconds)

---

## Directory Structure for Tests

```
src/test/java/com/noprobit/tools/ui/
  ├── ApplicationStartupTest.java
  ├── ProjectListPanelTest.java
  ├── ConfigurationDisplayPanelTest.java
  ├── MainWindowTest.java
  ├── IntegrationStartupTest.java
  └── fixtures/
      ├── TestProjectMetadata.java
      ├── MockUIApplicationService.java
      └── MockListeners.java
```

---

## Running Tests

### Maven Command
```bash
mvn test -Dtest=ApplicationStartupTest
mvn test -Dtest=ProjectListPanelTest
mvn test    # Run all tests
```

### Expected Output (RED Phase)
```
[ERROR] ApplicationStartupTest.testApplicationStarts() 
        → Class TextAnalyserApplication not found
[ERROR] ProjectListPanelTest.testPanelCreated()
        → Class ProjectListPanel not found
...
Tests run: 20, Failures: 20, Errors: 0
```

### Expected Output (GREEN Phase)
```
[INFO] ApplicationStartupTest.testApplicationStarts()
[INFO] ProjectListPanelTest.testPanelCreated()
...
Tests run: 20, Failures: 0, Errors: 0
BUILD SUCCESS
```

---

## Next Steps

1. **Create test files** with specifications above
2. **Write failing tests** (RED)
3. **Run tests** to verify they fail
4. **Write minimal implementation**
5. **Run tests** to verify they pass
6. **Refactor** if needed
7. **Commit** tests + code

---

**Status:** Ready for test creation  
**Action Item:** Write test classes based on specifications above  
**No implementation code yet**

