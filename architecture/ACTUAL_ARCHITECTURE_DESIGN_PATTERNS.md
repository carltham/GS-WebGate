# TextAnalyser: Actual Design Patterns

**Version:** 1.0 (Based on actual code analysis)  
**Date:** 2026-07-20

---

## Overview

TextAnalyser uses well-established design patterns throughout its three modules to achieve modularity, testability, and maintainability.

---

## Pattern 1: Model-View-Controller (MVC)

**Used in:** TextAnalyser-UI-swing  
**Problem:** Separate UI presentation from business logic and data

### Implementation

```
TextAnalyser-UI-swing/
├── view/ (V)
│   ├── ProjectSelectionPanel
│   ├── ConfigurationEditorPanel
│   ├── AnalysisPanel
│   ├── ReportPanel
│   └── DashboardPanel
│
├── controller/ (C)
│   ├── ProjectSelectionController
│   ├── ConfigurationEditorController
│   ├── AnalysisController
│   ├── ReportController
│   └── DashboardController
│
└── model/ (M)
    ├── ProjectMetadata
    ├── AnalysisResult
    └── AnalysisReport
```

### Key Principle

**Each Phase has its own MVC triad:**

```
Phase 0-1: Project Selection
  View:       ProjectSelectionPanel
  Controller: ProjectSelectionController
  Model:      ProjectMetadata

Phase 2: Configuration
  View:       ConfigurationEditorPanel
  Controller: ConfigurationEditorController
  Model:      Configuration

Phase 3: Analysis
  View:       AnalysisPanel
  Controller: AnalysisController
  Model:      AnalysisReport

Phase 4: Report
  View:       ReportPanel + ViolationTable
  Controller: ReportController
  Model:      List<AnalysisResult>

Phase 5: Dashboard
  View:       DashboardPanel
  Controller: DashboardController
  Model:      Statistics object
```

### Benefits

- ✅ UI logic separated from business logic
- ✅ Easy to test controllers in isolation (mock views)
- ✅ Models can be reused across multiple views
- ✅ Clear separation of concerns

### Example: Configuration Editing

```
View (ConfigurationEditorPanel)
  ├─→ User edits source path field
  └─→ Calls controller.onSourcePathChanged(newPath)

Controller (ConfigurationEditorController)
  ├─→ Validates new path
  ├─→ Updates model
  ├─→ Persists to disk
  └─→ Calls view.showSuccessMessage()

Model (Configuration)
  └─→ Stores validated data
      ├─ sourcePath
      ├─ projectName
      └─ reportPath
```

---

## Pattern 2: Observer/Listener (Event-Driven)

**Used in:** TextAnalyser-UI-swing (progress updates)  
**Problem:** Decouple components so long-running operations don't block UI

### Implementation

```
AnalysisWorker (background thread)
  └─→ Publishes AnalysisProgressEvent
      ├─ filesProcessed: 45
      ├─ totalFiles: 127
      └─ currentFile: "UserController.java"

AnalysisPanel (UI thread)
  └─→ Listens for AnalysisProgressEvent
      └─→ Updates progress bar, status text
```

### Key Listeners

```
Event: AnalysisProgressEvent
└─→ Listener: AnalysisPanel.onProgressUpdate()
    └─→ Update UI (progress bar, status label)

Event: AnalysisCompletedEvent
└─→ Listener: ReportPanel.onAnalysisComplete()
    └─→ Load results from FileDB
    └─→ Display in ViolationTable

Event: AnalysisErrorEvent
└─→ Listener: AnalysisPanel.onAnalysisError()
    └─→ Show error message
    └─→ Log error
```

### Benefit

- ✅ UI stays responsive during long operations
- ✅ Progress updates flow smoothly
- ✅ Components loosely coupled (no direct dependencies)
- ✅ Easy to add new listeners

### Code Example

```java
// Publisher (AnalysisWorker)
protected void process(List<AnalysisProgressEvent> events) {
    for (AnalysisProgressEvent event : events) {
        updateProgressUI(event);  // SwingWorker handles thread safety
    }
}

// Listener (AnalysisPanel)
private void updateProgressUI(AnalysisProgressEvent event) {
    progressBar.setValue(event.getPercentComplete());
    statusLabel.setText("Processing: " + event.getCurrentFile());
}
```

---

## Pattern 3: Strategy

**Used in:** TextAnalyser-jar (purpose detection engines)  
**Problem:** Support multiple analysis algorithms without hardcoding

### Implementation

```
PurposeAnalyser
  ├─→ Uses: JsonConfiguredEngine[] engines
  │   ├─ engines[0]: ClassNamingPatterns (priority 100)
  │   ├─ engines[1]: SemanticPatterns (priority 80)
  │   └─ engines[N]: CustomEngine (user-defined)
  │
  └─→ analyzePurpose(className):
      └─→ For each engine (priority-ordered):
          ├─ Try engine.detectPurpose(className)
          ├─ If match found: return result
          └─ Else: continue to next
```

### Strategy Execution

```
Purpose Detection Pipeline:
  1. Check learned patterns cache
  2. Try engine[0] (ClassNamingPatterns) - priority 100
  3. Try engine[1] (SemanticPatterns) - priority 80
  4. Try engine[N] (Custom) - priority varies
  5. Check extends class keywords
  6. Check class name keywords
  7. (Optional) Verify via WebGate
  8. Track unknown patterns
```

### Adding New Strategy

```
To add a new detection strategy, simply add to purpose-mappings.json:

{
  "engineName": "NewPatternEngine",
  "priority": 90,
  "mappings": [
    { "pattern": "newkeyword", "purpose": "PURPOSE", "confidence": 0.85 }
  ]
}

No code changes needed - new strategy is automatically loaded!
```

### Benefits

- ✅ Multiple strategies coexist and work together
- ✅ Easy to add new strategies (config only)
- ✅ Priority-based ordering
- ✅ No hardcoding of detection logic

---

## Pattern 4: Factory

**Used in:** TextAnalyser-jar (name suggestion)  
**Problem:** Create objects (suggested names) based on analysis results

### Implementation

```
ClassNameSuggester (Factory)
  │
  └─→ suggest(AnalysisResult)
      ├─ Analyze current name
      ├─ Apply transformation rules
      ├─ Check naming conventions
      └─ Create and return SuggestedName

SuggestedName
  ├─ originalName: "usercontroller"
  ├─ suggestedName: "UserController"
  ├─ changeType: "PascalCase"
  └─ confidence: 0.95
```

### Benefits

- ✅ Complex object creation encapsulated
- ✅ Creation logic centralized
- ✅ Easy to add new suggestion strategies
- ✅ Objects created with all required properties

---

## Pattern 5: SwingWorker (Asynchronous Background Processing)

**Used in:** TextAnalyser-UI-swing (analysis execution)  
**Problem:** Long-running analysis shouldn't block UI thread

### Implementation

```
AnalysisWorker extends SwingWorker<Void, AnalysisProgressEvent>
  │
  ├─→ doInBackground() [BACKGROUND THREAD]
  │   ├─ Iterate through .java files
  │   ├─ Call jar module for each (HTTP REST)
  │   ├─ Publish progress events
  │   └─ Store results
  │
  ├─→ process(List<AnalysisProgressEvent>) [EDT]
  │   └─ Update progress bar and status text safely
  │
  └─→ done() [EDT]
      ├─ Analysis complete
      ├─ Enable Report tab
      └─ Fire AnalysisCompletedEvent
```

### Thread Safety

```
Background Thread:
  ├─ Handles file I/O
  ├─ Makes network calls
  ├─ Calls publish(progressEvent)  // Thread-safe queue
  └─ No direct UI updates

EDT (Event Dispatch Thread):
  ├─ Receives published events
  ├─ Updates UI components
  └─ Guarantees thread safety
```

### Benefits

- ✅ UI remains responsive during long operations
- ✅ Progress updates flow smoothly
- ✅ Automatic thread-safety handling
- ✅ Standard Swing pattern (no custom threading)

---

## Pattern 6: Service Layer

**Used in:** TextAnalyser-UI-swing (HTTP communication)  
**Problem:** Abstract away HTTP communication details

### Implementation

```
AnalysisServiceClient (Service Layer)
  │
  ├─→ analyze(ClassInfo)
  │   ├─ Build JSON request
  │   ├─ HTTP POST to jar module
  │   ├─ Handle response
  │   └─ Return AnalysisResult
  │
  └─→ Used by: AnalysisWorker
      ├─ AnalysisWorker doesn't know about HTTP
      ├─ AnalysisWorker doesn't know about jar module port
      ├─ AnalysisWorker just calls client.analyze()
      └─ Clean abstraction
```

### Configuration

```
Default: http://localhost:8081/analysis
Override: JAR_SERVICE_URL environment variable

Advantages:
  ├─ No hardcoded endpoints in code
  ├─ Easy to redirect to different server
  ├─ Testable (can mock service layer)
  └─ Configuration-driven flexibility
```

### Benefits

- ✅ Hides HTTP/networking complexity
- ✅ Centralizes communication logic
- ✅ Easy to swap implementation (e.g., use gRPC later)
- ✅ Promotes loose coupling between modules

---

## Pattern 7: Repository (Data Access)

**Used in:** TextAnalyser-jar (FileDB)  
**Problem:** Abstract away persistence mechanism

### Implementation

```
FileDB (Repository)
  │
  ├─→ store(className, analysisResult)
  │   └─ Write to text files in .analysis-db/
  │
  ├─→ get(className)
  │   └─ Read from .analysis-db/
  │       └─ Cache in memory for speed
  │
  └─→ query(filter)
      └─ Search across stored results
          └─ Support filtering by purpose, confidence, etc.
```

### Abstraction

```
Calling code (ClassAnalysisEngine):
  ├─ Just calls: fileDB.store(result)
  ├─ Doesn't know HOW it's stored
  ├─ Doesn't know WHERE it's stored
  └─ Doesn't know the underlying format

Implementation detail:
  └─ Could swap text files for database
  └─ Could swap for cloud storage
  └─ No calling code changes needed
```

### Benefits

- ✅ Persistence logic centralized
- ✅ Easy to change storage mechanism
- ✅ Testable (can mock FileDB)
- ✅ Supports in-memory caching transparently

---

## Pattern 8: Template Method

**Used in:** TextAnalyser-jar (linting system)  
**Problem:** Multiple linters share common validation flow

### Implementation

```
Linter (Base/Template)
  │
  ├─→ validate(javaElement)
  │   ├─ 1. Extract properties
  │   ├─ 2. Check against rules (TEMPLATE METHOD - overridden)
  │   ├─ 3. Build violations list
  │   └─ 4. Return violations
  │
└─→ Implementations:
    ├─ JavaClassLinter
    │   └─ Overrides: validateNaming()
    │       └─ Checks: PascalCase, valid identifier
    │
    ├─ JavaMethodLinter
    │   └─ Overrides: validateNaming()
    │       └─ Checks: camelCase, not reserved words
    │
    ├─ JavaImportLinter
    │   └─ Overrides: validateImports()
    │       └─ Checks: unused, redundant
    │
    └─ JavaMethodOrderLinter
        └─ Overrides: validateOrdering()
            └─ Checks: static before instance, etc.
```

### Benefits

- ✅ Common validation flow centralized
- ✅ Easy to add new linter types
- ✅ Consistent error reporting format
- ✅ Code reuse reduces duplication

---

## Pattern 9: Adapter

**Used in:** TextAnalyser-webgate (DuckDuckGo API)  
**Problem:** Adapt third-party API to our domain model

### Implementation

```
InternetSearchService (Adapter)
  │
  ├─→ Takes: QueryRequest (our domain model)
  │   └─ question: String
  │   └─ context: String
  │   └─ maxResults: int
  │
  ├─→ Calls: DuckDuckGo API (third-party)
  │   └─ Different format, different response structure
  │
  └─→ Returns: QueryResponse (our domain model)
      └─ answer: String
      └─ confidence: double
      └─ sources: String[]
```

### Adaptation Logic

```
QueryRequest
  ├─→ InternetSearchService
  │   ├─ Transforms to DuckDuckGo format
  │   ├─ Calls DuckDuckGo API
  │   ├─ Parses DuckDuckGo response
  │   └─ Transforms to QueryResponse
  └─→ QueryResponse

Result: Callers never touch DuckDuckGo API directly
        Only interact with our domain models
```

### Benefits

- ✅ Isolates third-party API changes
- ✅ Consistent interface for all queries
- ✅ Easy to swap search provider
- ✅ Cleaner calling code

---

## Pattern 10: Singleton (Implicit)

**Used in:** TextAnalyser-jar (configuration, logging)  
**Problem:** Single instance of expensive resource

### Implementation

```
PurposeAnalyser
  └─→ Loaded once at startup
  └─→ JsonConfiguredEngine[] cached in memory
  └─→ Used for every analysis
  └─→ Prevents repeated parsing of configuration
```

### Benefits

- ✅ Configuration parsed once
- ✅ Patterns cached in memory
- ✅ Subsequent analyses fast
- ✅ Memory efficient

---

## Pattern 11: Chain of Responsibility

**Used in:** TextAnalyser-jar (purpose detection)  
**Problem:** Multiple handlers process request in sequence

### Implementation

```
Purpose Detection Chain:

1. Handler: Pattern Cache
   ├─ Check in-memory cache
   └─ If match found: return result
       ↓ (else continue)

2. Handler: JsonConfiguredEngine[0]
   ├─ Try ClassNamingPatterns
   └─ If match found: return result
       ↓ (else continue)

3. Handler: JsonConfiguredEngine[1]
   ├─ Try SemanticPatterns
   └─ If match found: return result
       ↓ (else continue)

4. Handler: Extends Class Checker
   ├─ Check extendsClass keywords
   └─ If match found: return result
       ↓ (else continue)

5. Handler: Class Name Checker
   ├─ Check class name keywords
   └─ If match found: return result
       ↓ (else continue)

6. Handler: WebGate Verifier (optional)
   ├─ Query DuckDuckGo
   └─ Return verification result

7. Handler: Unknown Pattern Tracker
   └─ Log as unknown
   └─ Return low confidence
```

### Benefits

- ✅ Each handler has single responsibility
- ✅ Easy to add/remove handlers
- ✅ Priority-based processing
- ✅ Graceful degradation (each handler optional)

---

## Pattern 12: Configuration Object

**Used in:** TextAnalyser (project config, analysis config)  
**Problem:** Manage multiple related configuration values

### Implementation

```
ProjectMetadata (Configuration Object)
  ├─ projectName: String
  ├─ sourcePath: String
  ├─ reportPath: String
  └─ Created once per project
      └─ Passed through entire analysis pipeline
      └─ Avoids repeated configuration lookups
```

### Benefits

- ✅ Related configuration grouped together
- ✅ Type-safe (vs. Map<String, String>)
- ✅ Easy to add new config values
- ✅ Clear intent (not just "params" object)

---

## Pattern 13: DTO (Data Transfer Object)

**Used in:** All modules (HTTP communication)  
**Problem:** Transfer data between modules without exposing internal classes

### Implementation

```
HTTP Boundary:
  UI Module  ←→  JAR Module

UI creates JSON:
{
  "className": "UserController",
  "extendsClass": "BaseController",
  "filePath": "/src/..."
}

JAR receives, deserializes to DTO:
ClassAnalysisRequest {
  String className
  String extendsClass
  String filePath
}

JAR processes, creates DTO:
AnalysisResult {
  String actualName
  String suggestedName
  String purpose
  double confidence
}

JAR returns JSON to UI
UI deserializes to AnalysisResult
```

### Benefits

- ✅ Modules don't share internal classes
- ✅ JSON serialization/deserialization centralized
- ✅ Easy to version API (add optional fields)
- ✅ Clear request/response boundaries

---

## Pattern 14: Decorator (Implicit)

**Used in:** TextAnalyser-jar (encoding system)  
**Problem:** Add encoding detection/conversion to analysis

### Implementation

```
Base Analysis:
  └─→ ClassAnalysisEngine.analyzeClassFile()

Decorator: Encoding Detection:
  ├─→ EncodingSwitcher detects file encoding
  ├─→ AdvancedEncodingEngine analyzes encoding
  └─→ Adds encoding info to result (optional)

Decorator: Encoding Conversion:
  ├─→ Encoder converts to standard encoding
  └─→ Analysis proceeds with consistent encoding

Result: AnalysisResult includes encoding info
```

### Benefits

- ✅ Additional functionality added transparently
- ✅ Encoding system optional
- ✅ Core analysis unchanged
- ✅ Easy to add more decorators

---

## Summary of Patterns

| Pattern | Module | Purpose | Example |
|---------|--------|---------|---------|
| **MVC** | UI | Separate concerns | 5 phase controllers |
| **Observer** | UI | Event-driven updates | AnalysisProgressEvent |
| **Strategy** | JAR | Pluggable algorithms | JsonConfiguredEngine[] |
| **Factory** | JAR | Create domain objects | ClassNameSuggester |
| **SwingWorker** | UI | Async operations | AnalysisWorker |
| **Service Layer** | UI | Abstract communication | AnalysisServiceClient |
| **Repository** | JAR | Abstract persistence | FileDB |
| **Template Method** | JAR | Shared validation flow | Linter base class |
| **Adapter** | WebGate | Integrate third-party | DuckDuckGo adapter |
| **Singleton** | JAR | Single instance | Config cache |
| **Chain of Responsibility** | JAR | Sequential handlers | Purpose detection chain |
| **Configuration Object** | All | Group related data | ProjectMetadata |
| **DTO** | All | Transfer across boundaries | AnalysisResult |
| **Decorator** | JAR | Add optional features | Encoding system |

---

## Pattern Interactions

### Example: How Patterns Work Together

**User runs analysis:**

```
1. MVC Pattern: ProjectSelectionController receives user input
2. Configuration Object: ProjectMetadata created with config
3. Observer Pattern: AnalysisPanel listens for progress events
4. SwingWorker Pattern: AnalysisWorker runs in background
5. Service Layer: AnalysisServiceClient calls jar module
6. DTO Pattern: Request/response as JSON
7. Strategy Pattern: jar module uses multiple detection engines
8. Chain of Responsibility: Purpose detection tries handlers in order
9. Repository Pattern: FileDB stores results
10. Factory Pattern: ClassNameSuggester creates suggestions
11. DTO Pattern: Result returned to UI as JSON
12. Observer Pattern: AnalysisCompletedEvent published
13. MVC Pattern: ReportPanel receives event, displays results
```

All patterns work together seamlessly to create a clean, maintainable architecture.

---

## Testing Implications

### Unit Testing with Patterns

```
Because of our patterns:
├─ MVC: Controllers testable with mock views
├─ Service Layer: Easy to mock jar module calls
├─ Repository: Easy to mock FileDB
├─ Strategy: Easy to test individual strategies
└─ Factory: Easy to verify object creation

Result: High testability, good code coverage
```

### Integration Testing with Patterns

```
DTO Pattern enables clean integration tests:
├─ Can test jar module independently
├─ Can test UI independently
├─ Can test webgate independently
└─ Can combine modules and test end-to-end

Result: Clear module boundaries, independent testing
```

---

## Key Takeaways

1. **Patterns aren't over-engineered** - Each solves a real problem
2. **Multiple patterns coexist** - No conflicts, complementary
3. **Patterns promote testability** - Easy to mock, easy to test
4. **Patterns enable flexibility** - Easy to add/remove/change functionality
5. **Patterns make code readable** - Developers recognize patterns instantly

TextAnalyser demonstrates **practical pattern usage** - not academic, but solving real architectural challenges.
