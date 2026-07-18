# Swing UI - Architecture Design (Documentation Only)

**Date:** 2026-07-18  
**Status:** Design Document - No Code Implementation

---

## Design Principles

### 1. Strict Layer Separation
- **No direct UI→Domain access**
- **No direct UI→Persistence access**
- All communication through UIApplicationService interface
- Each layer has single responsibility

### 2. Top-Down TDD
- Tests written first (RED)
- Minimal code to pass (GREEN)
- Improve while passing (REFACTOR)
- No assumptions or fantasies

### 3. Event-Driven Communication
- UI components emit events
- Services listen and respond
- Decoupled components
- Testable in isolation

### 4. No Blocking UI
- Long operations run in background threads
- UI remains responsive
- Progress updates via events
- Cancellation via message passing

---

## Layer Design

### Layer 6: Swing UI Components

**What it is:**
- JFrame, JPanels, JButtons, JLabels, JTables
- Visual presentation only
- No business logic
- No data access

**What it does:**
- Renders data on screen
- Captures user input
- Fires action listeners
- Updates display on events

**What it doesn't do:**
- Access domain services
- Query persistence
- Make business decisions
- Hold application state

**Examples:**
- `MainWindow` - Top-level JFrame
- `ProjectListPanel` - Lists projects in dropdown
- `AnalysisPanel` - Shows progress bar + status
- `ReportPanel` - Displays analysis results table

---

### Layer 5: UI Controllers

**What it is:**
- Bridges UI components and services
- Manages UI state
- Translates events to service calls
- Translates results to UI updates

**What it does:**
- Listen to UI component events
- Call UIApplicationService methods
- Listen to service events
- Update UI components with results

**What it doesn't do:**
- Render anything
- Access domain directly
- Make business decisions
- Hold persistent state

**Examples:**
- `ProjectSelectionController` - Handles project dropdown
- `AnalysisController` - Manages analysis start/stop/progress
- `ReportController` - Fetches and displays reports

---

### Layer 4: Application Service (New)

**What it is:**
- `UIApplicationService` interface
- Single point of entry for UI layer
- Orchestrates domain + persistence
- Fires events for UI updates

**What it does:**
- Loads projects and configuration
- Starts/stops/cancels analysis
- Retrieves reports and metrics
- Emits analysis events
- Emits project events

**What it doesn't do:**
- Render anything
- Know about Swing
- Make domain business decisions
- Persist directly

**Methods (defined in interface):**
- `loadProject(projectName)`
- `switchProject(projectName)`
- `startAnalysis()`
- `cancelAnalysis()`
- `getLatestReport(projectName)`
- `addAnalysisListener(listener)`
- `removeAnalysisListener(listener)`

---

### Layer 3: Domain Services (Existing)

**What it is:**
- ClassAnalysisEngine
- ClassNameValidator
- ClassFileAnalyzer
- Various linters
- PurposeAnalyser

**What it does:**
- Validates code conventions
- Analyzes Java files
- Produces analysis results

**What it doesn't do:**
- Know about UI
- Know about persistence
- Emit UI events

**Used by:** Application Service only

---

### Layer 2: Persistence (Existing)

**What it is:**
- FileDB - text-based analysis storage

**What it does:**
- Stores analysis results
- Retrieves historical reports
- Exports CSV/Markdown

**What it doesn't do:**
- Know about UI
- Know about domain rules

**Used by:** Application Service only

---

### Layer 1: External Services (Existing)

**What it is:**
- AdvancedEncodingEngine
- File encoding detection

**What it does:**
- Detects file encoding
- Converts between charsets

**Used by:** Domain layer (ClassAnalysisEngine)

---

## Communication Flow

### User Clicks "Start Analysis" Button

```
1. UI Component (AnalysisPanel)
   └─ User clicks button
   └─ actionPerformed() fires
   
2. UI Controller (AnalysisController)
   └─ Receives button click event
   └─ Calls service.startAnalysis()
   
3. Application Service (UIApplicationService)
   └─ Delegates to domain service
   └─ Runs analysis (async)
   └─ Fires progress events to listeners
   
4. Back to UI Controller
   └─ Receives progress events
   └─ Calls panel.updateProgress()
   
5. UI Component
   └─ Updates progress bar
   └─ Updates status label
```

### User Changes Project Selection

```
1. UI Component (ProjectListPanel)
   └─ User selects from dropdown
   
2. UI Controller (ProjectSelectionController)
   └─ Receives selection event
   └─ Calls service.switchProject(name)
   
3. Application Service
   └─ Loads configuration
   └─ Fires projectLoaded event
   
4. Back to UI Controller
   └─ Receives projectLoaded event
   └─ Updates dependent panels
   
5. UI Components
   └─ Configuration panel updates
   └─ Enable/disable analysis button
```

---

## Event System Design

### Event Types

**Analysis Events:**
- AnalysisStartedEvent (contains: projectName, timestamp)
- AnalysisProgressEvent (contains: fileCount, totalFiles, percentage, current file)
- AnalysisCompletedEvent (contains: violations found, analysis time)
- AnalysisErrorEvent (contains: error message, exception)

**Project Events:**
- ProjectLoadingEvent (contains: projectName)
- ProjectLoadedEvent (contains: ProjectMetadata)
- ProjectConfigurationChangedEvent (contains: field, oldValue, newValue)
- ProjectErrorEvent (contains: error message, exception)

### Listener Interfaces

**AnalysisListener:**
- `onAnalysisStarted(event)`
- `onAnalysisProgress(event)`
- `onAnalysisCompleted(event)`
- `onAnalysisError(event)`

**ProjectListener:**
- `onProjectLoadingStarted(event)`
- `onProjectLoaded(event)`
- `onProjectConfigurationChanged(event)`
- `onProjectError(event)`

---

## Data Transfer Objects (DTOs)

### ProjectMetadata
- projectName: String
- sourcePath: String
- loadedTime: long

### AnalysisReport
- projectName: String
- timestamp: long
- totalClassesAnalyzed: int
- violationsFound: int
- compliancePercentage: int
- entries: List<ReportEntry>

### AnalysisProgress
- processedFiles: int
- totalFiles: int
- currentFile: String
- percentage: int
- running: boolean

---

## Thread Safety Requirements

### Analysis Runs in Background Thread
- Long-running operation (seconds/minutes)
- Must not block UI thread
- Progress updates via thread-safe events
- Cancellation via volatile flag

### Event Firing is Thread-Safe
- Events can be fired from any thread
- Listeners called on event dispatch thread
- SwingUtilities.invokeLater() for UI updates

### State Updates are Synchronized
- AnalysisProgress object uses volatile fields
- UIApplicationService implementation uses synchronized/volatile

---

## Error Handling Strategy

### User-Facing Errors
- Show error dialog
- Include actionable message
- Suggest next step

### System Errors
- Log to file
- Show generic "unexpected error" message
- Provide support contact info

### Validation Errors
- Prevent invalid input (field validation)
- Show inline error messages
- Disable invalid operations (button state)

---

## Testing Architecture

### Unit Tests per Component

**UI Component Tests:**
- Mock listeners
- Fire events
- Verify UI state changes

**Controller Tests:**
- Mock service
- Fire UI events
- Verify service called correctly
- Verify listeners notified correctly

**Service Tests:**
- Mock domain and persistence
- Verify business logic
- Verify event firing

### Integration Tests

**End-to-End Scenarios:**
- Application startup
- Project switching
- Complete analysis workflow
- Report viewing

---

## No Code Yet

This document describes the design **without implementation**.

Actual code will be written when:
1. Tests are written first (RED)
2. Tests fail
3. Minimal code added (GREEN)
4. Tests pass

**Current Status:** Ready for test specification

---

## Next Phase

Detailed test specifications for Phase 0 will describe:
- Exact method signatures
- Expected exceptions
- Event firing behavior
- State changes
- Edge cases

All as test cases (code examples of what SHOULD pass).

