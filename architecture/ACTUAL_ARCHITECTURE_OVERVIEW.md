# TextAnalyser: Actual Architecture Overview

**Version:** 1.0 (Based on actual code analysis)  
**Last Updated:** 2026-07-20  
**Status:** Production (Phases 0-5 Complete, Phase 6+ Planned)

---

## What TextAnalyser Actually Is

TextAnalyser is a **Java code analysis and refactoring recommendation system** that:

1. **Analyzes Java class files** to extract structure (class names, inheritance, methods, imports)
2. **Suggests better class names** based on detected purpose and existing naming conventions
3. **Validates Java naming standards** (PascalCase, valid identifiers, etc.)
4. **Detects class purposes** using pattern matching (Controller, Panel, Dialog, Service, Utility, etc.)
5. **Optionally verifies purposes** via internet search (WebGate) for semantic validation
6. **Persists analysis results** to local file-based database
7. **Generates reports** with violations and recommendations
8. **Provides a Swing desktop UI** for interactive analysis and configuration

---

## Core Architecture

### Three-Module Design

```
┌─────────────────────────────────────────────────────────────┐
│                    TextAnalyser-pom                          │
│                  (Maven Multi-module)                        │
└─────────────────────────────────────────────────────────────┘
           ↓                    ↓                      ↓
    ┌──────────────┐   ┌─────────────────┐   ┌──────────────┐
    │ TextAnalyser │   │ TextAnalyser    │   │ TextAnalyser │
    │    -jar      │   │   -UI-swing     │   │  -webgate    │
    │              │   │                 │   │              │
    │ Core Engine  │←──┤  Desktop GUI    │   │ Spring Boot  │
    │ HTTP Server  │   │  (Swing MVC)    │   │ Gateway      │
    │ Port: 8081   │   │                 │   │ Port: 8080   │
    └──────────────┘   └─────────────────┘   └──────────────┘
         ↓                                            ↓
    ┌──────────────┐                        ┌──────────────┐
    │ FileDB       │                        │ DuckDuckGo   │
    │ (Analysis    │                        │ API          │
    │  results)    │                        │              │
    └──────────────┘                        └──────────────┘
```

### Module Responsibilities

| Module | Purpose | Technology |
|--------|---------|-----------|
| **TextAnalyser-jar** | Core analysis engine | Java 11, Embedded HttpServer, FileDB |
| **TextAnalyser-UI-swing** | Interactive desktop UI | Swing MVC, SwingWorker, REST client |
| **TextAnalyser-webgate** | External verification | Spring Boot, DuckDuckGo API |
| **TextAnalyser-mq** | Message queue (future) | TCP-based JSON protocol (Phase 6+) |

---

## Communication Architecture

### REST-Based HTTP Communication

**UI → Analysis Engine:**
```
POST http://localhost:8081/analysis/analyze
Content-Type: application/json

{
  "className": "UserController",
  "extendsClass": "BaseController",
  "filePath": "/src/main/java/com/example/UserController.java"
}
```

**Analysis Engine → WebGate (Optional Verification):**
```
POST http://localhost:8080/webgate/api/verify-purpose
Content-Type: application/json

{
  "className": "UserController",
  "detectedPurpose": "CONTROLLER",
  "keyword": "controller"
}
```

---

## Data Flow: User Runs Analysis

```
1. User Opens UI
   └─→ TextAnalyserApplication.main()
       └─→ MainWindow (5-tab interface)

2. User Selects Project
   └─→ ProjectSelectionPanel
       └─→ ProjectSelectionController
           └─→ ProjectMetadata (name, sourcePath, reportPath)

3. User Configures Analysis
   └─→ ConfigurationEditorPanel
       └─→ ConfigurationEditorController
           └─→ ConfigurationPersistence (load/save .properties)
               └─→ ConfigurationValidator

4. User Clicks "Analyze"
   └─→ AnalysisPanel
       └─→ AnalysisController
           └─→ AnalysisWorker (SwingWorker for non-blocking)
               ├─→ Reads source directory
               └─→ For each .java file:
                   ├─→ ClassFileAnalyzer (extract metadata)
                   ├─→ ClassAnalysisEngine (analyze)
                   │   ├─→ PurposeAnalyser (detect purpose)
                   │   │   ├─→ JsonConfiguredEngine (pattern matching)
                   │   │   ├─→ JavaClassLinter (naming validation)
                   │   │   └─→ (optional) WebGate verification
                   │   ├─→ ClassNameSuggester (recommend names)
                   │   └─→ JavaMethodLinter, JavaImportLinter, etc.
                   └─→ FileDB.store(result)
                   └─→ Publish AnalysisProgressEvent

5. UI Updates with Progress
   └─→ AnalysisPanel listens to events
       └─→ Updates progress bar, status text

6. Analysis Complete
   └─→ Fire AnalysisCompletedEvent
       └─→ ReportPanel becomes active

7. User Views Results
   └─→ ReportPanel displays violations
       ├─→ ViolationTable (sortable/filterable)
       ├─→ FilterPanel (filter by category)
       └─→ ReportExporter (export to file)

8. User Views Statistics
   └─→ DashboardPanel (Phase 5)
       ├─→ DashboardController
       ├─→ DashboardRefresh (auto-update)
       ├─→ ProjectOverview
       └─→ StatisticsDisplay
```

---

## Core Analysis Process

### Purpose Detection Pipeline

```
Input: Java Class File
  ↓
1. Check learned patterns (in-memory cache)
  ↓
2. Check JSON-configured engines (priority-ordered)
  ├─→ ClassNamingPatterns (priority 100)
  │   └─→ "controller" → CONTROLLER (confidence 0.95)
  ├─→ SemanticPatterns (priority 80)
  │   └─→ Various semantic rules
  └─→ Custom engines (user-defined)
  ↓
3. Check extends class keywords
  ├─→ If extends "BaseController" → CONTROLLER
  └─→ If extends "BasePanel" → PANEL
  ↓
4. Check class name keywords
  ├─→ Contains "Controller" → CONTROLLER
  ├─→ Contains "Panel" → PANEL
  ├─→ Contains "Service" → SERVICE
  └─→ ... (more patterns)
  ↓
5. Optional WebGate verification (if enabled)
  └─→ Query internet for semantic validation
  ↓
6. Track unknown patterns
  └─→ Log to logs/purpose-analysis.log
  ↓
Output: AnalysisResult {
  actualName,
  suggestedName,
  purpose,
  confidence,
  extendsClass
}
```

---

## Data Models

### Core Analysis Models

**AnalysisResult** (main output)
```java
String actualName          // Original class name
String suggestedName       // Recommended class name
String purpose            // Detected purpose (CONTROLLER, PANEL, etc.)
String extendsClass       // Parent class name
```

**PurposeType** (enum)
```
CONTROLLER  - REST/Web controllers
PANEL       - Swing UI panels
DIALOG      - Swing dialog windows
SERVICE     - Business logic services
UTILITY     - Utility/helper classes
LISTENER    - Event listeners
ADAPTER     - Adapter pattern implementations
FACTORY     - Factory pattern implementations
MODEL       - Data models/POJOs
REPOSITORY  - Data access objects
OTHER       - Unclassified
```

**PurposeMatch** (detection result)
```java
String purpose            // Detected purpose
double confidence         // Confidence score (0.0-1.0)
String source            // Detection method (pattern, extends, etc.)
```

### UI Models

**ProjectMetadata**
```java
String projectName       // User-friendly name
String sourcePath       // Path to source files
String reportPath       // Path to export reports
```

**AnalysisProgressEvent**
```java
int filesProcessed      // Number of files analyzed
int totalFiles          // Total files to analyze
String currentFile      // Currently processing file
```

---

## Configuration System

### Purpose Mappings (JSON)

**File:** `src/main/resources/purpose-mappings.json` (in jar module)

```json
{
  "engines": [
    {
      "engineName": "ClassNamingPatterns",
      "priority": 100,
      "mappings": [
        {
          "pattern": "controller",
          "purpose": "CONTROLLER",
          "confidence": 0.95
        },
        {
          "pattern": "panel",
          "purpose": "PANEL",
          "confidence": 0.95
        },
        ...more patterns...
      ]
    }
  ]
}
```

### Application Configuration

**File:** `analysis.properties`

```properties
project.name=TextAnalyser
source.node.path=src/main/java
```

### Environment Variables

```bash
JAR_SERVICE_URL=http://localhost:8081/analysis  # Override analysis endpoint
```

---

## Persistence: FileDB

Simple text-file-based database for analysis results.

**Directory:** `.analysis-db/`  
**Format:** One record per line (text files)  
**Operations:**
- `store(result)` - Save analysis result
- `get(key)` - Retrieve analysis result
- `query(filter)` - Query results by criteria

**In-Memory Caching:** Results cached on first read for fast access

---

## Logging

**Directory:** `logs/`  
**File:** `purpose-analysis.log`

Tracks:
- All analysis runs
- Unknown patterns encountered
- Confidence scores for purposes
- Time taken per analysis

---

## Testing Strategy: Three-Tier TDD

**Naming Convention:**
- `*Test.java` - Unit tests (isolated, mocked dependencies)
- `*LT.java` - Layer tests (component boundaries, light integration)
- `*IT.java` - Integration tests (full workflow, real database)

**Maven Profiles for Selective Execution:**

```bash
mvn test                    # Run unit tests only (default)
mvn test -P layer          # Run layer tests
mvn test -P integration    # Run integration tests
mvn test -P all-tests      # Run all three
```

---

## UI Architecture: MVC Pattern

### MainWindow Structure

```
MainWindow (JFrame)
├─ Tab 0-1: Project Selection
│  ├─ ProjectListPanel
│  ├─ ProjectSelectionPanel
│  └─ ProjectSelectionController
│
├─ Tab 2: Configuration
│  ├─ ConfigurationDisplayPanel
│  ├─ ConfigurationEditorPanel
│  └─ ConfigurationEditorController
│
├─ Tab 3: Analysis Execution
│  ├─ AnalysisPanel
│  ├─ AnalysisController
│  └─ AnalysisWorker (SwingWorker)
│
├─ Tab 4: Report & Export
│  ├─ ReportPanel
│  ├─ ReportController
│  ├─ ViolationTable (JTable)
│  ├─ FilterPanel
│  └─ ReportExporter
│
└─ Tab 5: Dashboard (Phase 5)
   ├─ DashboardPanel
   ├─ DashboardController
   ├─ DashboardRefresh
   ├─ ProjectOverview
   └─ StatisticsDisplay
```

### Theme & Styling

**UITheme** - Consistent Swing styling with modern Material Design aesthetics

---

## Deployment Architecture

### Development Environment

All three modules run on localhost:
- **UI:** Desktop application (Swing)
- **Analysis Engine:** `http://localhost:8081` (embedded HTTP server)
- **WebGate:** `http://localhost:8080/webgate` (Spring Boot)

### Configuration Discovery Chain

1. `../config/analysis.properties` (parent module)
2. `config/analysis.properties` (project root)
3. `/mnt/DATA/WORKSPACE/Textanalyser/analysis.properties` (workspace)
4. Subdirectories in workspace
5. Hard defaults (TextAnalyser, src/main/java)

### Future Deployment (Phase 6+)

**Planned:**
- Docker containerization
- Kubernetes orchestration
- Message Queue (TCP-based) for decoupled communication
- Cloud deployment support

---

## Design Patterns Used

| Pattern | Usage | Examples |
|---------|-------|----------|
| **MVC** | UI layer organization | MainWindow, Panels, Controllers |
| **Observer/Listener** | Event-driven updates | AnalysisProgressEvent, AnalysisCompletedEvent |
| **Strategy** | Pluggable analysis engines | JsonConfiguredEngine[] array |
| **Factory** | Object creation | ClassNameSuggester creates suggestions |
| **SwingWorker** | Long-running tasks off EDT | AnalysisWorker for background analysis |
| **Service Layer** | HTTP communication abstraction | AnalysisServiceClient |
| **REST** | Inter-module communication | HTTP endpoints on jar and webgate |

---

## Key Architectural Decisions

1. **REST over Direct Calls**
   - Allows UI and Engine to be separate JVM processes
   - Easier testing and deployment

2. **Embedded HTTP Server in JAR**
   - Uses Java's built-in `com.sun.net.httpserver.HttpServer`
   - No Spring Boot overhead for simple analysis endpoint
   - Simple, fast, lightweight

3. **Optional WebGate Verification**
   - Analysis works without internet connection
   - Verification is enhancement, not requirement
   - Graceful degradation if WebGate unavailable

4. **FileDB for Persistence**
   - Simple text-based storage
   - No database setup required
   - Human-readable format
   - Fast in-memory caching

5. **JSON Configuration**
   - Pattern-driven purpose detection
   - Easy to add new patterns without code changes
   - User-configurable without recompilation

6. **Three-Tier Testing**
   - Unit tests verify logic in isolation
   - Layer tests verify component interactions
   - Integration tests verify end-to-end workflows
   - Selective execution via Maven profiles

---

## Entry Points

| Component | Entry Point | Port | Type |
|-----------|------------|------|------|
| Desktop UI | `TextAnalyserApplication.main()` | N/A | Swing GUI |
| Analysis Engine | Embedded in jar module | 8081 | HTTP Server |
| WebGate | `WebGateApplication.main()` | 8080 | Spring Boot |
| Configuration | `ConfigurationEditorController` | N/A | UI dialog |

---

## Performance Characteristics

- **Analysis Speed:** Depends on project size (processes .java files sequentially)
- **UI Responsiveness:** Maintained via SwingWorker (long tasks off EDT)
- **Memory Footprint:** Lightweight (in-memory cache of analysis results)
- **Network:** Minimal (only REST calls between modules)

---

## Phase History (TDD Approach)

| Phase | Focus | Status |
|-------|-------|--------|
| 0 | Project Selection - RED | ✅ Complete |
| 1 | Project Selection - GREEN/REFACTOR | ✅ Complete |
| 2 | Analysis Execution | ✅ Complete |
| 3 | Report Display & Export | ✅ Complete |
| 4 | Configuration Editor | ✅ Complete |
| 5 | Dashboard & Statistics | ✅ In Progress |
| 6+ | Message Queue Infrastructure | 📋 Planned |

**Phase 5 Additions (Latest):**
- `DashboardController.java` - Statistics management
- `DashboardPanel.java` - Dashboard UI
- `DashboardRefresh.java` - Auto-refresh mechanism
- `ProjectOverview.java` - Project information display
- `StatisticsDisplay.java` - Statistics visualization

---

## Summary

TextAnalyser is a focused, modular Java code analysis system with:
- ✅ Clear separation of concerns (UI, Engine, Gateway)
- ✅ REST-based loose coupling
- ✅ Pattern-driven analysis via JSON configuration
- ✅ Optional internet-based verification
- ✅ Simple file-based persistence
- ✅ Comprehensive TDD coverage
- ✅ Desktop UI for interactive use

The architecture prioritizes **simplicity, testability, and maintainability** over complexity, making it easy to extend with new analysis engines, export formats, or verification strategies.
