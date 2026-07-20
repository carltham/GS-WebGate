# TextAnalyser: Actual Data Flow & Communication

**Version:** 1.0 (Based on actual code analysis)  
**Date:** 2026-07-20

---

## High-Level Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    User Opens UI                             │
│                (TextAnalyserApplication)                     │
└────────────────────────┬────────────────────────────────────┘
                         ↓
            ┌────────────────────────┐
            │   Tab 0: Project List  │
            │   (Select project)     │
            └────────────┬───────────┘
                         ↓
            ┌────────────────────────┐
            │  Tab 2: Configuration  │
            │  (Set source path)     │
            └────────────┬───────────┘
                         ↓
            ┌────────────────────────┐
            │  Tab 3: Analysis       │
            │  (Run analysis)        │
            └────────────┬───────────┘
                         ↓
        ┌───────────────────────────────────┐
        │      AnalysisWorker starts        │
        │  (SwingWorker - background)       │
        └───────────────┬───────────────────┘
                        ↓
        ┌───────────────────────────────────┐
        │  For each .java file in project:  │
        │  ├─ Read file from disk           │
        │  ├─ POST to jar module (8081)     │
        │  ├─ Update UI progress            │
        │  └─ Store result in FileDB        │
        └───────────────┬───────────────────┘
                        ↓
            ┌────────────────────────┐
            │  Tab 4: Report         │
            │  (View results)        │
            └────────────┬───────────┘
                         ↓
            ┌────────────────────────┐
            │  Tab 5: Dashboard      │
            │  (View statistics)     │
            └────────────────────────┘
```

---

## Workflow 1: User Runs Analysis

### Step 1: Application Startup

```
TextAnalyserApplication.main()
  ├─→ Create MainWindow (JFrame)
  │   └─→ Initialize 5 tabs
  ├─→ Load UITheme (Material Design)
  └─→ Show window

MainWindow visible:
├─ ProjectListPanel (projects available)
├─ ProjectSelectionPanel
├─ ConfigurationDisplayPanel
├─ AnalysisPanel (disabled)
├─ ReportPanel (disabled)
└─ DashboardPanel
```

### Step 2: User Selects Project

```
User clicks project in ProjectListPanel
  ↓
ProjectSelectionController.onProjectSelected(projectName)
  ├─→ Load ProjectMetadata:
  │   ├─ projectName
  │   ├─ sourcePath (/home/user/myproject/src/main/java)
  │   └─ reportPath (/home/user/myproject/reports)
  ├─→ Update ConfigurationDisplayPanel
  └─→ Enable Analysis tab
```

**Data Model:**
```java
ProjectMetadata {
  String projectName
  String sourcePath
  String reportPath
}
```

### Step 3: User Views/Edits Configuration

```
User clicks "Edit Configuration" button
  ↓
ConfigurationEditorPanel opens
  ├─→ ConfigurationPersistence.loadConfig()
  │   ├─ Read analysis.properties
  │   └─ Return Configuration object
  ├─→ Display fields in UI form
  └─→ User can edit, then save

User clicks "Save"
  ↓
ConfigurationValidator.validate()
  ├─→ Check sourcePath exists
  ├─→ Check paths are valid
  └─→ Return validation result

If valid:
  ├─→ ConfigurationPersistence.saveConfig()
  │   └─→ Write to analysis.properties
  ├─→ Update ProjectMetadata
  └─→ Show success message

If invalid:
  └─→ Show error message
```

### Step 4: User Starts Analysis

```
User clicks "Analyze" button in Tab 3
  ↓
AnalysisController.startAnalysis()
  ├─→ Create AnalysisWorker (SwingWorker)
  │   └─→ Pass ProjectMetadata and configuration
  └─→ Call worker.execute()

AnalysisWorker.doInBackground()  [Runs on background thread]
  ├─→ Read source directory
  │   └─→ Find all .java files recursively
  ├─→ Initialize progress counter
  └─→ For each .java file:
      │
      ├─→ 1. Read file content
      │   └─→ Get file from disk
      │
      ├─→ 2. POST to jar module
      │   │
      │   ├─→ Build JSON request:
      │   │   {
      │   │     "filePath": "/src/main/java/com/example/UserController.java",
      │   │     "className": "UserController",
      │   │     "extendsClass": "BaseController",
      │   │     "methods": ["getUser", "updateUser", ...],
      │   │     "imports": [...]
      │   │   }
      │   │
      │   ├─→ HTTP POST to http://localhost:8081/analysis/analyze
      │   │
      │   ├─→ JAR Module processes:
      │   │   └─→ (See Workflow 2 below)
      │   │
      │   └─→ Receive JSON response:
      │       {
      │         "actualName": "UserController",
      │         "suggestedName": "UserController",
      │         "purpose": "CONTROLLER",
      │         "confidence": 0.95,
      │         "extendsClass": "BaseController",
      │         "violations": [...]
      │       }
      │
      ├─→ 3. Store result
      │   └─→ FileDB.store(className, analysisResult)
      │       └─→ Write to .analysis-db/
      │
      ├─→ 4. Publish progress event
      │   └─→ AnalysisProgressEvent {
      │         filesProcessed: 45,
      │         totalFiles: 127,
      │         currentFile: "UserController.java",
      │         percentComplete: 35
      │       }
      │
      └─→ Continue to next file

AnalysisWorker.process() [Runs on EDT]
  ├─→ Listen for progress events
  └─→ Update UI:
      ├─ Progress bar (35/127 files)
      ├─ Status text (currently processing UserController.java)
      └─ Elapsed time
```

### Step 5: Analysis Completes

```
AnalysisWorker finishes all files
  ├─→ Set progress to 100%
  └─→ Fire AnalysisCompletedEvent

AnalysisWorker.done() [Runs on EDT]
  ├─→ Update UI
  │   └─→ Show "Analysis Complete" message
  ├─→ Enable Report tab
  └─→ Optionally switch to Report tab

Results now available:
├─ FileDB contains all analysis results
├─ AnalysisReport summarizes findings
└─ Ready for viewing/exporting
```

---

## Workflow 2: JAR Module Analyzes Single Class

### Complete Analysis Pipeline

```
HTTP Request arrives at AnalysisController
  │
  ├─→ JSON Deserialization
  │   └─→ Build ClassAnalysisRequest object
  │
  ├─→ ClassAnalysisEngine.analyzeClassFile()
  │   │
  │   ├─→ Stage 1: Extract Metadata
  │   │   └─→ ClassFileAnalyzer.extractMetadata()
  │   │       ├─ Parse class name
  │   │       ├─ Parse extends class
  │   │       ├─ Extract method names
  │   │       ├─ Extract import statements
  │   │       └─ Return ClassMetadata
  │   │
  │   ├─→ Stage 2: Detect Purpose
  │   │   └─→ PurposeAnalyser.analyzePurpose()
  │   │       │
  │   │       ├─ 2a. Check learned patterns (in-memory cache)
  │   │       │     └─→ Return if match found
  │   │       │
  │   │       ├─ 2b. Query JsonConfiguredEngine[] (priority-ordered)
  │   │       │     └─→ For each engine:
  │   │       │         ├─ Check pattern in className
  │   │       │         ├─ If match: return PurposeMatch
  │   │       │         └─ Else: continue to next engine
  │   │       │
  │   │       ├─ 2c. Check extends class keywords
  │   │       │     └─→ If extendsClass.contains("Controller"):
  │   │       │         └─→ Return CONTROLLER (confidence 0.90)
  │   │       │
  │   │       ├─ 2d. Check class name keywords
  │   │       │     └─→ If className.contains("Listener"):
  │   │       │         └─→ Return LISTENER (confidence 0.85)
  │   │       │
  │   │       ├─ 2e. Optional WebGate Verification
  │   │       │     └─→ If enabled:
  │   │       │         ├─ POST to WebGate (/api/verify-purpose)
  │   │       │         ├─ Enhance confidence score
  │   │       │         └─ Add verification source
  │   │       │
  │   │       └─ 2f. Track unknown patterns
  │   │             └─→ Log to logs/purpose-analysis.log
  │   │
  │   ├─→ Stage 3: Validate Naming
  │   │   ├─→ JavaClassLinter.validate()
  │   │   │   ├─ Check PascalCase
  │   │   │   ├─ Check valid identifier
  │   │   │   ├─ Check no reserved words
  │   │   │   └─ Return violations
  │   │   ├─→ JavaMethodLinter.validate()
  │   │   │   ├─ Check camelCase
  │   │   │   └─ Return violations
  │   │   ├─→ JavaImportLinter.validate()
  │   │   │   ├─ Check unused imports
  │   │   │   └─ Return violations
  │   │   └─→ JavaMethodOrderLinter.validate()
  │   │       ├─ Check method ordering
  │   │       └─ Return violations
  │   │
  │   ├─→ Stage 4: Suggest Better Names
  │   │   └─→ ClassNameSuggester.suggest()
  │   │       ├─ Analyze current name
  │   │       ├─ Check naming conventions
  │   │       ├─ Apply transformation rules
  │   │       └─ Return suggested name
  │   │
  │   └─→ Stage 5: Build Result
  │       └─→ AnalysisResult {
  │             actualName: "UserController",
  │             suggestedName: "UserController",
  │             purpose: "CONTROLLER",
  │             confidence: 0.95,
  │             extendsClass: "BaseController",
  │             violations: [...]
  │           }
  │
  └─→ JSON Serialization & Return

HTTP Response sent back to UI
  └─→ UI stores result and updates progress
```

---

## Workflow 3: WebGate Verifies Purpose (Optional)

### Purpose Verification Flow

```
JAR Module has detected purpose: CONTROLLER
  ↓
If RemoteVerification enabled:
  ├─→ Call WebGate API
  │   └─→ POST http://localhost:8080/webgate/api/verify-purpose
  │
  └─→ Request payload:
      {
        "className": "UserController",
        "detectedPurpose": "CONTROLLER",
        "keyword": "controller"
      }
  ↓
WebGate receives request
  └─→ PurposeVerificationController.verifyPurpose()
      ├─→ InternetSearchService.verifyPurpose()
      │   ├─→ Build DuckDuckGo query
      │   │   └─→ "UserController controller pattern"
      │   ├─→ HTTP GET to https://api.duckduckgo.com/
      │   │
      │   ├─→ Parse DuckDuckGo response:
      │   │   ├─ Instant answer (if present): confidence 0.95
      │   │   ├─ Abstract (if present): confidence 0.80
      │   │   └─ Related topics (if present): confidence 0.70
      │   │
      │   └─→ Return RemoteVerificationResult {
      │         className: "UserController",
      │         purpose: "CONTROLLER",
      │         verified: true,
      │         confidence: 0.92,
      │         sources: ["DuckDuckGo Instant Answer"]
      │       }
      │
      └─→ Send JSON response
  ↓
JAR Module receives verification
  ├─→ Update confidence score
  │   └─→ 0.95 (local) * 0.92 (verification) = 0.874
  └─→ Include in AnalysisResult
```

### Generic Query Flow

```
Any system queries WebGate for general knowledge
  ↓
POST http://localhost:8080/webgate/api/query
  └─→ {
      "question": "What is REST API?",
      "context": "java spring boot",
      "maxResults": 5,
      "timeout": 5000
    }
  ↓
WebGate.PurposeVerificationController.queryGeneric()
  └─→ InternetSearchService.queryGeneric()
      ├─→ Build DuckDuckGo query
      │   ├─ Add question: "What is REST API?"
      │   ├─ Add context: "java spring boot"
      │   └─ Set max results: 5
      │
      ├─→ HTTP GET to DuckDuckGo API
      │   └─→ https://api.duckduckgo.com/?q=What+is+REST+API+java+spring+boot
      │
      ├─→ Parse response:
      │   ├─ Extract instant answer (confidence 0.95)
      │   │   └─→ "REST is Representational State Transfer, an architectural style..."
      │   ├─ Extract abstract (confidence 0.80)
      │   ├─ Extract related topics (confidence 0.70)
      │   └─ Extract sources
      │
      └─→ Return QueryResponse {
        question: "What is REST API?",
        answerFound: true,
        answer: "REST is Representational State Transfer...",
        confidence: 0.92,
        summary: "Direct answer found",
        processingTime: 245,
        sources: ["DuckDuckGo", "Wikipedia", "MDN"]
      }
  ↓
Client receives answer with confidence score
```

---

## Workflow 4: View Results in Report

### Report Display & Export

```
User switches to Report tab
  ↓
ReportPanel.load()
  ├─→ FileDB.query() - Retrieve all analysis results
  ├─→ Filter results (if filtered state exists)
  └─→ Load into ViolationTable (JTable)

ViolationTable displays:
├─ Columns: ClassName | SuggestedName | Purpose | Confidence | File
├─ Sortable (click column header)
├─ Filterable (FilterPanel)
└─ Copy-able (Ctrl+C)

User can:
  ├─→ Filter by purpose (dropdown)
  ├─→ Sort by confidence (descending)
  ├─→ View individual violations
  └─→ Export results

User clicks "Export to CSV"
  ↓
ReportExporter.exportToCSV()
  ├─→ Get selected directory
  ├─→ Build CSV content
  │   └─→ "ClassName,SuggestedName,Purpose,Confidence\n..."
  ├─→ Write to file (reports/analysis-export.csv)
  └─→ Show success message

User clicks "Print Report"
  ↓
ReportExporter.printReport()
  ├─→ Create PrinterJob
  ├─→ Format results for printing
  └─→ Send to printer
```

---

## Workflow 5: View Dashboard

### Statistics Display

```
User opens Dashboard tab
  ↓
DashboardPanel.load()
  ├─→ DashboardController.loadStatistics()
  │   ├─→ FileDB.query() - Get all results
  │   └─→ Calculate statistics:
  │       ├─ Total classes: 127
  │       ├─ Classes with issues: 34
  │       ├─ Average confidence: 0.87
  │       ├─ Purpose distribution:
  │       │   ├─ CONTROLLER: 23
  │       │   ├─ SERVICE: 45
  │       │   ├─ PANEL: 34
  │       │   └─ OTHER: 25
  │       └─ Violations by type:
  │           ├─ Naming: 23
  │           ├─ Import: 8
  │           └─ Method order: 3
  │
  └─→ StatisticsDisplay renders:
      ├─ Summary cards (total, issues, confidence)
      ├─ Pie chart (purpose distribution)
      ├─ Bar chart (violation types)
      └─ List view (issues sorted by confidence)

DashboardRefresh auto-updates:
  ├─→ If new analysis runs
  └─→ Refresh statistics every 5 seconds
```

---

## Configuration Data Flow

### Configuration Loading

```
Application startup:
  ├─→ ConfigurationLoader.load()
  │   ├─→ Check ../config/analysis.properties
  │   ├─→ Check config/analysis.properties
  │   ├─→ Check /mnt/DATA/WORKSPACE/Textanalyser/analysis.properties
  │   ├─→ Check workspace subdirectories
  │   └─→ Fall back to defaults
  │
  └─→ Load purpose-mappings.json
      ├─→ Parse JSON
      ├─→ Create JsonConfiguredEngine[] array
      ├─→ Sort by priority (100, 80, etc.)
      └─→ Cache in memory

Later, when purpose-mappings.json changes:
  ├─→ PurposeMappingLoader.reload()
  └─→ Update in-memory engines array
```

---

## Persistence: FileDB

### Write Path

```
Analysis complete for class "UserController"
  ├─→ AnalysisResult result = {...}
  ├─→ FileDB.store("UserController", result)
  │
  └─→ FileDB.store():
      ├─→ Check .analysis-db/ directory exists
      │   └─→ Create if missing
      ├─→ Serialize AnalysisResult to JSON/text
      ├─→ Write to .analysis-db/UserController.analysis
      ├─→ Update in-memory cache
      │   └─→ cache.put("UserController", result)
      └─→ Return success
```

### Read Path

```
Report tab requests results
  ├─→ FileDB.query()
  │
  └─→ FileDB.query():
      ├─→ Check in-memory cache first
      │   ├─→ If cache hit: return cached results
      │   └─→ If cache miss: load from disk
      ├─→ Read all files from .analysis-db/
      ├─→ Deserialize each to AnalysisResult
      ├─→ Apply filter criteria
      │   ├─→ By purpose
      │   ├─→ By confidence
      │   └─→ By filename
      ├─→ Sort results
      └─→ Return List<AnalysisResult>
```

---

## Error Handling Flows

### Network Error: JAR Module Unreachable

```
UI tries to call jar module
  ├─→ POST to http://localhost:8081/analysis/analyze
  ├─→ Connection refused (server not running)
  │
  └─→ AnalysisServiceClient catches exception:
      ├─→ Log error
      ├─→ Show UI error message: "Analysis service unavailable"
      ├─→ Suggest: "Start the jar module on port 8081"
      └─→ Continue (analysis stops for this class)
```

### WebGate Unavailable (Graceful Degradation)

```
JAR tries to verify purpose via WebGate
  ├─→ POST to http://localhost:8080/webgate/api/verify-purpose
  ├─→ Connection timeout (WebGate not running)
  │
  └─→ InternetSearchService catches exception:
      ├─→ Log warning
      ├─→ Continue with local confidence score (no verification)
      ├─→ Mark source as "Local analysis only"
      └─→ Return AnalysisResult with original confidence
```

### Invalid Configuration

```
User tries to analyze with invalid source path
  ├─→ ConfigurationValidator.validate()
  │
  └─→ Validation fails:
      ├─→ Path doesn't exist
      ├─→ No .java files found
      └─→ Show error: "Source path does not contain Java files"

Analysis doesn't start until config is valid.
```

---

## Performance Considerations

### Caching Strategy

```
Purpose Patterns (In-Memory):
  └─→ Loaded once at startup from purpose-mappings.json
  └─→ Ordered by priority (100, 80, etc.)
  └─→ Reused for every class analysis

Analysis Results (FileDB Cache):
  └─→ Loaded on first query
  └─→ Subsequent queries hit memory cache
  └─→ Cache invalidated on new analysis run

UI Event Batching:
  └─→ AnalysisProgressEvent published periodically
  └─→ Not on every file (would overwhelm EDT)
  └─→ Every N files or every T milliseconds
```

### Async Processing

```
Long-running operations use SwingWorker:
  ├─→ Analysis: Off EDT (background thread)
  ├─→ File I/O: Off EDT
  ├─→ Network calls: Off EDT
  └─→ UI updates: Always on EDT (thread-safe)
```

---

## Data at Rest

### FileDB Directory Structure

```
.analysis-db/
├── UserController.analysis
├── UserService.analysis
├── UserRepository.analysis
├── UserDTO.analysis
└── ... (one file per analyzed class)
```

### Configuration Files

```
analysis.properties
├─ project.name=TextAnalyser
└─ source.node.path=src/main/java

purpose-mappings.json
├─ engines[]
│  ├─ engineName: "ClassNamingPatterns"
│  ├─ priority: 100
│  └─ mappings[]: {pattern, purpose, confidence}
└─ ...
```

### Logs

```
logs/
└── purpose-analysis.log
    ├─ Timestamp | ClassName | DetectedPurpose | Confidence | Source
    ├─ 2026-07-20 10:15:23 | UserController | CONTROLLER | 0.95 | Pattern
    └─ ...
```

---

## Summary

**Data flows through three main channels:**

1. **Configuration** - Loaded at startup, cached, used for all analyses
2. **Analysis** - User → UI → JAR → FileDB → Report
3. **Verification** - JAR → WebGate → DuckDuckGo → JAR → UI

**All communication is REST/HTTP based**, allowing modules to be:
- In separate JVM processes
- On different ports (8081, 8080)
- Even on different machines (with network configuration)
- Independently deployed and scaled
