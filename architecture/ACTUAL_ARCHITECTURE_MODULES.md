# TextAnalyser: Actual Module Architecture

**Version:** 1.0 (Based on actual code analysis)  
**Date:** 2026-07-20  

---

## TextAnalyser-jar: Core Analysis Engine

**Location:** `/TextAnalyser-pom/TextAnalyser-jar/`  
**Purpose:** Analyzes Java class files and suggests naming improvements  
**Port:** 8081 (Embedded HTTP Server)  
**Dependencies:** None (standalone, no Spring)

### Package Structure

```
com.noprobit.analyzers/
├── PurposeAnalyser                      # Main orchestrator
├── AnalysisController                   # HTTP endpoint handler
├── ClassAnalysisEngine                  # Comprehensive analysis
└── ClassFileAnalyzer                    # File parsing

com.noprobit.analyzers.config/
└── PurposeMappingLoader                 # JSON configuration loading

com.noprobit.analyzers.engine/
└── JsonConfiguredEngine                 # Pattern-based analysis engine

com.noprobit.analyzers.model/
├── AnalysisResult                       # Output model
├── PurposeMatch                         # Purpose detection result
├── PurposeType                          # Purpose enum
└── UnknownPattern                       # Pattern tracking

com.noprobit.analyzers.remote/
└── RemoteVerificationResult             # WebGate response model

com.noprobit.linting/
├── JavaClassLinter                      # Class naming validation
├── JavaMethodLinter                     # Method naming validation
├── JavaImportLinter                     # Import analysis
└── JavaMethodOrderLinter                # Method ordering validation

com.noprobit.encoding/
├── EncodingSwitcher                     # Encoding conversion
├── AdvancedEncodingEngine               # Encoding detection
└── Encoder                              # Encoding utilities

com.noprobit.db/
└── FileDB                               # Text file-based database

com.noprobit.reporters/
├── ClassNameAnalysisReporter            # Result formatting
└── ClassNameSuggester                   # Name recommendations

com.noprobit.validators/
└── ClassNameValidator                   # Naming rule validation
```

### Core Classes

#### PurposeAnalyser
**Responsibility:** Coordinate purpose detection across all engines

```java
// Key Methods:
analyzePurpose(className, extendsClass) → PurposeMatch
│ ├─→ Check learned patterns (cache)
│ ├─→ Query JsonConfiguredEngine[] (priority-ordered)
│ ├─→ Check extends class keywords
│ ├─→ Check class name keywords
│ └─→ Track unknown patterns
```

#### ClassAnalysisEngine
**Responsibility:** Full analysis workflow for a single class file

```java
// Key Methods:
analyzeClassFile(filePath) → AnalysisResult
│ ├─→ ClassFileAnalyzer.extractMetadata()
│ ├─→ PurposeAnalyser.analyzePurpose()
│ ├─→ JavaClassLinter.validate()
│ ├─→ ClassNameSuggester.suggest()
│ ├─→ (optional) RemoteVerification via WebGate
│ └─→ Store in FileDB
```

#### ClassFileAnalyzer
**Responsibility:** Parse Java source code

```java
// Key Methods:
extractClassMetadata(filePath) → ClassMetadata
│ ├─→ Read source file
│ ├─→ Parse class name
│ ├─→ Parse extends class
│ ├─→ Extract methods
│ ├─→ Extract imports
│ └─→ Return metadata object
```

#### AnalysisController
**Responsibility:** Handle HTTP requests for analysis

```java
// Endpoint:
POST /analysis/analyze
├─→ Parse JSON request
├─→ ClassAnalysisEngine.analyzeClassFile()
├─→ Return JSON response
└─→ Error handling

// Also provides:
GET /analysis/health → OK
```

### Configuration

**File:** `src/main/resources/purpose-mappings.json`

```json
{
  "engines": [
    {
      "engineName": "ClassNamingPatterns",
      "priority": 100,
      "mappings": [
        { "pattern": "controller", "purpose": "CONTROLLER", "confidence": 0.95 },
        { "pattern": "panel", "purpose": "PANEL", "confidence": 0.95 },
        { "pattern": "dialog", "purpose": "DIALOG", "confidence": 0.95 },
        { "pattern": "service", "purpose": "SERVICE", "confidence": 0.90 },
        { "pattern": "listener", "purpose": "LISTENER", "confidence": 0.90 },
        { "pattern": "adapter", "purpose": "ADAPTER", "confidence": 0.85 },
        { "pattern": "factory", "purpose": "FACTORY", "confidence": 0.85 },
        { "pattern": "model", "purpose": "MODEL", "confidence": 0.80 },
        { "pattern": "repository", "purpose": "REPOSITORY", "confidence": 0.85 }
      ]
    },
    {
      "engineName": "SemanticPatterns",
      "priority": 80,
      "mappings": [...]
    }
  ]
}
```

### Data Flow in JAR

```
HTTP Request (JSON)
  ↓
AnalysisController.handleAnalysis()
  ↓
ClassAnalysisEngine.analyzeClassFile()
  ├─→ ClassFileAnalyzer.extractMetadata()
  ├─→ PurposeAnalyser.analyzePurpose()
  │   ├─→ Check patterns
  │   ├─→ Query JsonConfiguredEngine[]
  │   └─→ (optional) Call WebGate
  ├─→ JavaClassLinter.validate()
  ├─→ JavaMethodLinter.validate()
  ├─→ JavaImportLinter.validate()
  ├─→ ClassNameSuggester.suggest()
  └─→ FileDB.store(result)
  ↓
Return AnalysisResult (JSON)
```

### Persistence: FileDB

**Directory:** `.analysis-db/`

```java
// Key Methods:
store(classname, analysisResult) → void
get(classname) → analysisResult
query(filter) → List<AnalysisResult>
```

### Testing

**Unit Tests** (`*Test.java`):
- `PurposeAnalyserTest` - Purpose detection logic
- `ClassAnalysisEngineTest` - Analysis workflow
- `ClassFileAnalyzerTest` - Parsing logic
- `JavaClassLinterTest` - Naming validation

**Layer Tests** (`*LT.java`):
- `AnalysisControllerLT` - HTTP handling
- `PurposeAnalysisLT` - Detection pipeline

**Integration Tests** (`*IT.java`):
- `FullAnalysisIT` - End-to-end analysis
- `FileDBIT` - Persistence

---

## TextAnalyser-UI-swing: Desktop Application

**Location:** `/TextAnalyser-pom/TextAnalyser-UI-swing/`  
**Purpose:** Interactive desktop UI for managing analysis projects  
**Technology:** Swing MVC, SwingWorker, REST client  
**Port:** N/A (Client application)

### Package Structure

```
com.noprobit.ui/
├── TextAnalyserApplication              # Main entry point
├── MainWindow                           # Root JFrame
│
├── phase0_1/ (Project Selection)
│   ├── ProjectListPanel
│   ├── ProjectSelectionPanel
│   └── ProjectSelectionController
│
├── phase2/ (Configuration)
│   ├── ConfigurationDisplayPanel
│   ├── ConfigurationEditorPanel
│   ├── ConfigurationEditorController
│   ├── ConfigurationPersistence
│   └── ConfigurationValidator
│
├── phase3/ (Analysis Execution)
│   ├── AnalysisPanel
│   ├── AnalysisController
│   ├── AnalysisWorker
│   └── AnalysisProgressEvent
│
├── phase4/ (Report & Export)
│   ├── ReportPanel
│   ├── ReportController
│   ├── ReportExporter
│   ├── ViolationTable
│   └── FilterPanel
│
├── phase5/ (Dashboard)
│   ├── DashboardPanel
│   ├── DashboardController
│   ├── DashboardRefresh
│   ├── ProjectOverview
│   └── StatisticsDisplay
│
├── service/
│   └── AnalysisServiceClient            # REST client to jar module
│
└── utils/
    ├── UITheme                          # Material Design styling
    └── UIHelpers                        # Common UI utilities

model/
└── ProjectMetadata                      # Configuration model
```

### Core UI Flow

#### MainWindow Structure

```
MainWindow (JFrame)
├─ Title: "TextAnalyser"
├─ 5 Tabs (TabbedPane)
│  ├─ Tab 0: "Project Selection"
│  ├─ Tab 1: "Project Overview"
│  ├─ Tab 2: "Configuration"
│  ├─ Tab 3: "Analysis"
│  ├─ Tab 4: "Report"
│  └─ Tab 5: "Dashboard"
└─ Status bar with current project info
```

#### Phase 0-1: Project Selection

**Components:**
- `ProjectListPanel` - Display available projects
- `ProjectSelectionPanel` - Project chooser UI
- `ProjectSelectionController` - Handle selection events
- `ProjectMetadata` - Project configuration object

**Flow:**
```
User sees project list
  ↓
User clicks a project
  ↓
ProjectSelectionController.onProjectSelected()
  ↓
Load ProjectMetadata
  └─→ name, sourcePath, reportPath
  ↓
Enable next tab
```

#### Phase 2: Configuration

**Components:**
- `ConfigurationDisplayPanel` - Show current config
- `ConfigurationEditorPanel` - Edit config UI
- `ConfigurationEditorController` - Handle changes
- `ConfigurationPersistence` - Load/save .properties files
- `ConfigurationValidator` - Validate config values

**Flow:**
```
User clicks "Edit Configuration"
  ↓
ConfigurationEditorPanel opens
  ├─→ Load current config
  ├─→ Display in form fields
  └─→ Allow editing
  ↓
User clicks "Save"
  ↓
ConfigurationValidator.validate()
  ↓
ConfigurationPersistence.save()
  ├─→ Write to analysis.properties
  └─→ Update ProjectMetadata
  ↓
Show success message
```

#### Phase 3: Analysis Execution

**Components:**
- `AnalysisPanel` - Progress display
- `AnalysisController` - Orchestrate analysis
- `AnalysisWorker` (SwingWorker) - Background processing
- `AnalysisProgressEvent` - Progress notifications

**Flow:**
```
User clicks "Analyze"
  ↓
AnalysisController.startAnalysis()
  ↓
Create AnalysisWorker
  └─→ Non-blocking execution
  ↓
AnalysisWorker.doInBackground()
  ├─→ Read source directory
  └─→ For each .java file:
      ├─→ Call AnalysisServiceClient.analyze()
      │   └─→ HTTP POST to jar module (port 8081)
      ├─→ Publish AnalysisProgressEvent
      │   └─→ Update UI (file count, progress)
      └─→ Continue to next file
  ↓
AnalysisWorker.done()
  └─→ Fire AnalysisCompletedEvent
      └─→ Activate Report tab
```

**Key Design: SwingWorker**
- Long-running analysis happens off EDT
- UI stays responsive during analysis
- Progress events update GUI safely

#### Phase 4: Report & Export

**Components:**
- `ReportPanel` - Display results
- `ReportController` - Report logic
- `ReportExporter` - Export to files
- `ViolationTable` (JTable) - Sortable/filterable results
- `FilterPanel` - Filter options

**Flow:**
```
Analysis completes
  ↓
ReportPanel displays results
  ├─→ ViolationTable loads analysis data
  │   ├─→ Columns: ClassFile, ActualName, SuggestedName, Purpose
  │   └─→ Sortable/filterable
  └─→ FilterPanel allows filtering
  ↓
User can:
  ├─→ Sort columns
  ├─→ Filter by purpose
  ├─→ Export to CSV/PDF
  └─→ Print report
```

#### Phase 5: Dashboard (NEW)

**Components:**
- `DashboardPanel` - Main dashboard UI
- `DashboardController` - Statistics management
- `DashboardRefresh` - Auto-refresh mechanism
- `ProjectOverview` - Project information
- `StatisticsDisplay` - Statistics visualization

**Flow:**
```
User opens Dashboard tab
  ↓
DashboardController loads statistics
  ├─→ Number of classes analyzed
  ├─→ Number of issues found
  ├─→ Confidence scores
  ├─→ Purpose distribution
  └─→ Naming violations count
  ↓
StatisticsDisplay renders charts/tables
  ↓
DashboardRefresh auto-updates (if analysis runs)
```

### AnalysisServiceClient: REST Communication

**Responsibility:** Call jar module's analysis endpoint

```java
public AnalysisResult analyze(ClassInfo classInfo) {
    // Build JSON request
    // POST to http://localhost:8081/analysis/analyze
    // Parse JSON response
    // Return AnalysisResult
}

// Default: http://localhost:8081/analysis
// Override: JAR_SERVICE_URL environment variable
```

### UITheme: Material Design Styling

**Responsibility:** Consistent modern look and feel

```java
public class UITheme {
    public static void applyTheme(JFrame frame) {
        // Set modern fonts
        // Set color scheme
        // Apply to all components
    }
}
```

### Testing

**Unit Tests:**
- `ProjectSelectionControllerTest`
- `ConfigurationEditorControllerTest`
- `AnalysisControllerTest`
- `ReportControllerTest`
- `DashboardControllerTest`

**Layer Tests:**
- `UIControllerLayerTests`
- `AnalysisServiceClientLT`

**Integration Tests:**
- `FullUIWorkflowIT`
- `ProjectAnalysisEndToEndIT`

---

## TextAnalyser-webgate: Spring Boot Gateway

**Location:** `/TextAnalyser-pom/TextAnalyser-webgate/`  
**Purpose:** Internet search verification for class purposes  
**Port:** 8080  
**Technology:** Spring Boot, REST API  
**External API:** DuckDuckGo

### Package Structure

```
com.noprobit.analyzers.webgate/
├── WebGateApplication                   # Spring Boot entry point
├── PurposeVerificationController        # REST endpoints
│   ├── POST /api/verify-purpose
│   └── POST /api/query (generic queries)
├── InternetSearchService                # DuckDuckGo integration
├── QueryRequest                         # Request model
├── QueryResponse                        # Response model
└── SearchResult                         # Search result model
```

### Core Classes

#### WebGateApplication
**Responsibility:** Spring Boot application bootstrap

```java
@SpringBootApplication
public class WebGateApplication {
    // Embedded Tomcat on port 8080
    // Context path: /webgate
}
```

#### PurposeVerificationController
**Responsibility:** Handle REST requests for verification

```java
@RestController
@RequestMapping("/api")
public class PurposeVerificationController {

    @PostMapping("/verify-purpose")
    public RemoteVerificationResult verifyPurpose(
        String className,
        String detectedPurpose,
        String keyword
    )

    @PostMapping("/query")
    public QueryResponse queryGeneric(QueryRequest request)

    @GetMapping("/health")
    public String health()
}
```

#### InternetSearchService
**Responsibility:** Query DuckDuckGo API

```java
public class InternetSearchService {

    public RemoteVerificationResult verifyPurpose(
        String className,
        String detectedPurpose,
        String keyword
    )
    // └─→ Query DuckDuckGo
    // └─→ Parse response
    // └─→ Score confidence
    // └─→ Return result

    public QueryResponse queryGeneric(QueryRequest request)
    // └─→ Query DuckDuckGo with context
    // └─→ Extract answer
    // └─→ Score confidence
    // └─→ Return response
}
```

### REST Endpoints

#### Purpose Verification

```
POST /api/verify-purpose
Content-Type: application/json

Request:
{
  "className": "UserController",
  "detectedPurpose": "CONTROLLER",
  "keyword": "controller"
}

Response:
{
  "className": "UserController",
  "purpose": "CONTROLLER",
  "verified": true,
  "confidence": 0.92,
  "sources": ["DuckDuckGo Instant Answer"]
}
```

#### Generic Queries (Phase 2)

```
POST /api/query
Content-Type: application/json

Request:
{
  "question": "What is REST API?",
  "context": "java spring boot",
  "maxResults": 5,
  "timeout": 5000
}

Response:
{
  "question": "What is REST API?",
  "answerFound": true,
  "answer": "REST is an architectural style for web services...",
  "confidence": 0.85,
  "summary": "Direct answer found",
  "processingTime": 245,
  "sources": ["DuckDuckGo", "Wikipedia"]
}
```

### Configuration

**File:** `application.properties`

```properties
spring.application.name=TextAnalyser-WebGate
server.port=8080
server.servlet.context-path=/webgate
webgate.search.enabled=true
webgate.search.timeout=5000
webgate.duckduckgo.endpoint=https://api.duckduckgo.com/
```

### Data Models

**RemoteVerificationResult** (Purpose verification)
```java
String className
String purpose
boolean verified
double confidence
List<String> sources
```

**QueryRequest** (Generic queries)
```java
String question        // Required
String context         // Optional
int maxResults         // Optional, default 5
long timeout           // Optional, default 5000ms
```

**QueryResponse** (Generic query response)
```java
String question
boolean answerFound
String answer
double confidence      // Clamped to 0.0-1.0
String summary
long processingTime
List<String> sources
```

### DuckDuckGo Integration

**Flow:**
```
QueryRequest
  ↓
InternetSearchService.queryGeneric()
  ├─→ Build DuckDuckGo API call
  ├─→ Add context to query
  ├─→ Set timeout
  └─→ HTTP GET to api.duckduckgo.com
  ↓
Parse Response
  ├─→ Extract instant answer (confidence 0.95)
  ├─→ Extract abstract (confidence 0.80)
  ├─→ Extract related topics (confidence 0.70)
  └─→ Handle no results (confidence 0.20)
  ↓
Confidence Scoring
  ├─→ Direct answer: 0.90-1.0
  ├─→ Summary: 0.75-0.90
  ├─→ Related topic: 0.60-0.75
  └─→ No results: 0.0-0.20
  ↓
QueryResponse (with sources)
```

### Testing

**Unit Tests:**
- `InternetSearchServiceTest`
- `PurposeVerificationTest`
- `GenericQueryServiceTest`

**Layer Tests:**
- `PurposeVerificationControllerLT`
- `GenericQueryControllerLT`

**Integration Tests:**
- `PurposeVerificationIT`
- `GenericQueryServiceIT`
- `DuckDuckGoIntegrationIT`

---

## TextAnalyser-mq: Message Queue (Future)

**Location:** `/TextAnalyser-pom/TextAnalyser-mq/`  
**Status:** Empty directory, planned for Phase 6+  
**Purpose:** Decoupled communication between jar and webgate  
**Technology:** TCP-based JSON protocol

### Planned Architecture

**Server:** Message Queue Server (runs locally)
```
TCP Port: 9999 (default)
Protocol: JSON over TCP
Storage: In-memory (TTL-based auto-cleanup)
```

**Commands:**
1. `enqueue_request` - Add analysis request
2. `dequeue_request` - Retrieve analysis request
3. `enqueue_response` - Store analysis result
4. `dequeue_response` - Retrieve analysis result
5. `has_response` - Check if result ready
6. `stats` - Get queue statistics

**Planned:** Full documentation when implementation begins

---

## Module Dependencies

```
TextAnalyser-UI-swing
  ├─ (depends on) TextAnalyser-jar
  │  └─ (via HTTP REST: localhost:8081)
  └─ (depends on) TextAnalyser-webgate
     └─ (via HTTP REST: localhost:8080)

TextAnalyser-jar
  ├─ (no internal dependencies)
  └─ (calls) DuckDuckGo API (if verification enabled)

TextAnalyser-webgate
  ├─ (no internal dependencies)
  └─ (calls) DuckDuckGo API

TextAnalyser-mq (Phase 6+)
  ├─ (independent)
  └─ (acts as) central message hub
```

---

## Summary

**JAR Module:** Core analysis engine
- Stateless HTTP service
- Analyzes Java classes via pattern matching
- Optional internet verification
- Lightweight, fast, self-contained

**UI Module:** Desktop application
- Rich Swing interface
- Project/configuration management
- Async analysis via SwingWorker
- Reports and dashboards

**WebGate Module:** External verification
- Spring Boot microservice
- DuckDuckGo API integration
- Generic query support
- Confidence scoring

**MQ Module:** Future infrastructure
- Planned for decoupled communication
- TCP-based message queue
- In-memory storage with TTL
