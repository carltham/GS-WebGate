# Code Migration Plan - Existing to Swing UI Architecture

**Date:** 2026-07-18  
**Status:** Planning Phase  
**Scope:** How to refactor existing code into 6-tier architecture

---

## Current State Analysis

### Existing Code Structure

```
TextAnalyser-jar/src/main/java/com/noprobit/tools/
├── analyzers/
│   ├── ClassAnalysisEngine.java       ← Orchestrates analysis
│   ├── ClassFileAnalyzer.java         ← Metadata extraction
│   └── PurposeAnalyser.java           ← Purpose inference
├── config/
│   └── AnalysisConfig.java            ← Configuration loading
├── db/
│   └── FileDB.java                    ← Persistence
├── encoding/
│   ├── AdvancedEncodingEngine.java    ← Encoding detection
│   ├── EncodingSwitcher.java
│   ├── CharsetEncodingStrategy.java
│   └── EncodingResult.java
├── linting/
│   ├── JavaClassLinter.java
│   ├── JavaMethodLinter.java
│   ├── JavaImportLinter.java
│   └── JavaMethodOrderLinter.java
├── reporters/
│   ├── ClassNameAnalysisReporter.java ← Report generation
│   └── ClassNameSuggester.java
├── validators/
│   └── ClassNameValidator.java
└── encoding/
    └── EncodingSwitcher.java
```

### Current Layer Mapping

```
Today's Structure:
├── Domain Logic (Analyzers, Linters, Validators)
├── Persistence (FileDB)
├── Utilities (Encoding, Config, Reporters)
└── CLI Test (ProjectClassNameValidationTest)

Issues:
- No service layer abstraction
- No clear interfaces between layers
- No event system
- Tests directly call domain classes
- Config is global/static
```

---

## Target Architecture (6-Tier)

```
Layer 6: Swing UI Components (NEW)
Layer 5: UI Controllers (NEW)
Layer 4: Application Service (NEW) - UIApplicationService
Layer 3: Domain Services (EXISTING - reuse/refactor)
Layer 2: Persistence (EXISTING - reuse)
Layer 1: External Services (EXISTING - reuse)
```

---

## Migration Strategy: Phase-by-Phase

### Phase 0: Create Service Layer Abstraction

**Goal:** Extract domain logic behind interface without changing implementation

#### Step 0.1: Create UIApplicationService Interface
**File:** `ui/service/UIApplicationService.java` (interface only)

**What goes in it:**
- `loadProject(String)`
- `switchProject(String)`
- `startAnalysis()`
- `cancelAnalysis()`
- `getLatestReport(String)`
- Event listener management

**NO code changes to existing classes yet**

#### Step 0.2: Create AnalysisService Interface
**File:** `service/AnalysisService.java` (interface)

**Wrap existing ClassAnalysisEngine:**
- `analyzeProject(String projectName, Path sourcePath)`
- `addListener(AnalysisProgressListener)`
- `cancel()`

**Purpose:** Abstract ClassAnalysisEngine behind interface

#### Step 0.3: Create ReportService Interface
**File:** `service/ReportService.java` (interface)

**Wrap existing FileDB and reporters:**
- `getReports(String projectName)`
- `generateReport(String projectName, String format)`
- `exportToCSV(String path)`
- `exportToMarkdown(String path)`

**Purpose:** Abstract report generation behind interface

#### Step 0.4: Create ConfigurationService Interface
**File:** `service/ConfigurationService.java` (interface)

**Wrap existing AnalysisConfig:**
- `loadProject(String projectName)`
- `getCurrentProject()`
- `getAvailableProjects()`
- `updateConfiguration(String name, String path)`

**Purpose:** Abstract configuration behind interface

#### Step 0.5: Create Event Classes
**Directory:** `ui/service/events/`

**Create DTOs:**
- `AnalysisStartedEvent.java`
- `AnalysisProgressEvent.java`
- `AnalysisCompletedEvent.java`
- `AnalysisErrorEvent.java`
- `ProjectLoadedEvent.java`
- `ProjectErrorEvent.java`

**Purpose:** Define event contracts

**Status After Phase 0:**
- ✅ Interfaces exist
- ✅ No existing code changed
- ✅ Tests still pass
- ❌ No implementations yet

---

### Phase 1: Create Service Implementations

**Goal:** Implement service interfaces, wrapping existing classes

#### Step 1.1: Implement UIApplicationService
**File:** `ui/service/impl/UIApplicationServiceImpl.java`

**Dependencies:**
- `AnalysisService analysisService`
- `ReportService reportService`
- `ConfigurationService configService`
- `List<AnalysisListener> listeners`

**Implementation approach:**
```
public void startAnalysis() {
    try {
        ProjectMetadata project = configService.getCurrentProject();
        analysisService.analyzeProject(project.getName(), project.getPath());
        listeners.forEach(l -> l.onAnalysisStarted(...));
    } catch (Exception e) {
        listeners.forEach(l -> l.onAnalysisError(...));
    }
}
```

**Key points:**
- Thin wrapper around existing services
- Fire events when transitions occur
- No business logic change
- Just orchestration

#### Step 1.2: Implement AnalysisService
**File:** `service/impl/AnalysisServiceImpl.java`

**Wraps:** `ClassAnalysisEngine` (existing)

**Key points:**
```
public AnalysisResult analyzeProject(String name, Path path) {
    // Minimal wrapping
    return this.engine.analyzeProjectClasses(path);
}
```

**No changes to ClassAnalysisEngine itself yet**

#### Step 1.3: Implement ReportService
**File:** `service/impl/ReportServiceImpl.java`

**Wraps:**
- `FileDB` (existing persistence)
- `ClassNameAnalysisReporter` (existing reporter)

**Key points:**
```
public List<AnalysisReport> getReports(String projectName) {
    List<FileDB.AnalysisRecord> dbRecords = fileDB.getAllAnalysisResults();
    return convert(dbRecords); // Convert DB format to DTO format
}
```

#### Step 1.4: Implement ConfigurationService
**File:** `service/impl/ConfigurationServiceImpl.java`

**Wraps:** `AnalysisConfig` (existing)

**Key points:**
```
public void loadProject(String projectName) {
    this.config = new AnalysisConfig(); // Load from files
    // No change to AnalysisConfig
}
```

**Status After Phase 1:**
- ✅ Service implementations exist
- ✅ Existing classes wrapped
- ✅ No existing code changed
- ✅ Tests still pass
- ✅ Can now add UI layer

---

### Phase 2: Wire Services into UI Layer

**Goal:** Connect new Swing UI to services

#### Step 2.1: Create UI Controllers
**Files:** 
- `ui/controller/ProjectSelectionController.java`
- `ui/controller/AnalysisController.java`
- `ui/controller/ReportController.java`

**Pattern:**
```
public class ProjectSelectionController {
    private UIApplicationService service;
    private ProjectListPanel panel;
    
    public ProjectSelectionController(UIApplicationService service, 
                                     ProjectListPanel panel) {
        this.service = service;
        this.panel = panel;
        
        // Wire panel events to service calls
        panel.onProjectSelected(projectName -> {
            service.switchProject(projectName);
        });
        
        // Wire service events to panel updates
        service.addProjectListener(event -> {
            panel.updateConfiguration(event.getMetadata());
        });
    }
}
```

#### Step 2.2: Create UI Components
**Files:**
- `ui/component/ProjectListPanel.java`
- `ui/component/AnalysisPanel.java`
- `ui/component/ReportPanel.java`
- `ui/MainWindow.java`

**Pattern:** Simple Swing components, no business logic

#### Step 2.3: Wire Everything Together
**File:** `TextAnalyserApplication.java`

**Sequence:**
```
1. Create service implementations
2. Create UI components  
3. Create controllers (wire UI to services)
4. Create main window (assemble components)
5. Show window
```

**Status After Phase 2:**
- ✅ Full Swing UI functional
- ✅ UI connected to services
- ✅ Services using existing domain code
- ✅ Backward compatible (old tests still work)

---

### Phase 3: Optional - Refactor Existing Classes

**Goal:** Improve existing code now that interfaces are in place

**Only if time permits and tests pass**

#### Option 3.1: Refactor ClassAnalysisEngine
**Changes:**
- Accept AnalysisService interface instead of direct instantiation
- Better separation of concerns

**Risk:** Must update all test references

#### Option 3.2: Refactor FileDB
**Changes:**
- Better error handling
- Thread safety for background analysis

**Risk:** Persistence layer behavior must not change

#### Option 3.3: Add Logging
**Changes:**
- Add structured logging per project standards
- Add correlation IDs for debugging

**Risk:** None - logging is additive

**Decision:** Only refactor if tests fail or architecture violated

---

## Existing Code - What Changes

### Classes That Stay Unchanged (Wrap Only)

```
✅ ClassAnalysisEngine - Wrapped by AnalysisService
✅ ClassFileAnalyzer - Used by ClassAnalysisEngine
✅ JavaClassLinter - Used by ClassAnalysisEngine  
✅ JavaMethodLinter - Used by ClassAnalysisEngine
✅ JavaImportLinter - Used by ClassAnalysisEngine
✅ JavaMethodOrderLinter - Used by ClassAnalysisEngine
✅ ClassNameValidator - Used by linters
✅ PurposeAnalyser - Used by engine
✅ FileDB - Wrapped by ReportService
✅ AnalysisConfig - Wrapped by ConfigurationService
✅ AdvancedEncodingEngine - Used by ClassFileAnalyzer
✅ EncodingSwitcher - Legacy utility (unchanged)
✅ ClassNameAnalysisReporter - Wrapped by ReportService
```

### Minimal Test Changes

**Current Test:**
```java
// ProjectClassNameValidationTest.java
AnalysisConfig config = new AnalysisConfig();
ClassAnalysisEngine engine = new ClassAnalysisEngine();
List<AnalysisResult> results = engine.analyzeProjectClasses(sourceDir);
```

**After Migration (can keep or update):**
```java
// Option 1: Keep using domain directly (legacy path)
AnalysisConfig config = new AnalysisConfig();
ClassAnalysisEngine engine = new ClassAnalysisEngine();
List<AnalysisResult> results = engine.analyzeProjectClasses(sourceDir);
// Still works! No change needed

// Option 2: Use new service layer
UIApplicationService service = new UIApplicationServiceImpl(...);
service.startAnalysis();
service.addAnalysisListener(event -> {
    List<AnalysisReport> reports = service.getLatestReport(project);
});
```

**Both paths work - backward compatible**

---

## Integration Points

### Where Existing Code Plugs In

```
UI Layer (NEW)
    ↓ calls
UIApplicationService (NEW interface)
    ↓ calls
├─ AnalysisService (NEW wrapper)
│   ↓ uses
│   └─ ClassAnalysisEngine (EXISTING)
│       ↓ uses
│       ├─ ClassFileAnalyzer (EXISTING)
│       ├─ Linters (EXISTING)
│       └─ AdvancedEncodingEngine (EXISTING)
│
├─ ReportService (NEW wrapper)
│   ↓ uses
│   ├─ FileDB (EXISTING)
│   └─ ClassNameAnalysisReporter (EXISTING)
│
└─ ConfigurationService (NEW wrapper)
    ↓ uses
    └─ AnalysisConfig (EXISTING)
```

**Every existing class still used exactly as before**

---

## Testing During Migration

### Phase 0: No test changes
- Old tests still pass
- New service interfaces have no implementation yet

### Phase 1: New service tests
- Write tests for service implementations
- Old tests continue to pass
- New and old tests run together

### Phase 2: UI tests
- Write tests for controllers/panels
- Mock services for UI tests
- All three levels of tests pass together

### Phase 3+: Add more tests as refactoring
- No test removal
- Only additions

---

## Rollback Strategy

### If Phase 0 fails
- Delete new interface files
- No impact on existing code

### If Phase 1 fails  
- Delete service implementations
- Keep interfaces
- Revert to using domain directly

### If Phase 2 fails
- Delete UI layer
- Services still work for CLI
- Keep backward compatibility

**Each phase independent and reversible**

---

## Timeline & Dependencies

```
Phase 0 (Week 1)
└─ Create interfaces and DTOs
   No existing code touched
   Risk: NONE

Phase 1 (Week 2)  
├─ Depends on: Phase 0
├─ Implement services
├─ Wrap existing classes
└─ Risk: LOW (just wrappers)

Phase 2 (Week 2)
├─ Depends on: Phase 1  
├─ Create UI layer
├─ Wire everything
└─ Risk: MEDIUM (integration)

Phase 3+ (Optional)
├─ Depends on: Phase 2
├─ Refactor if needed
└─ Risk: MEDIUM (behavior changes)
```

---

## Success Criteria

### Phase 0: Complete
- [ ] All interfaces exist
- [ ] All DTOs exist
- [ ] All event classes exist
- [ ] No existing code changed
- [ ] Compile succeeds

### Phase 1: Complete
- [ ] Service implementations exist
- [ ] All existing classes wrapped
- [ ] Old tests still pass
- [ ] New service tests pass
- [ ] No breaking changes

### Phase 2: Complete
- [ ] UI components created
- [ ] Controllers created
- [ ] Main window works
- [ ] All tests pass
- [ ] Can analyze projects from UI

### Overall Success
- [ ] Original domain logic unchanged
- [ ] Original tests still pass
- [ ] New UI functional
- [ ] Backward compatible
- [ ] Can still use CLI (tests)
- [ ] Can use new Swing UI

---

## Risk Assessment

| Risk | Likelihood | Severity | Mitigation |
|------|-----------|----------|-----------|
| Breaking existing API | Low | High | Interfaces are new, don't change old classes |
| Tests fail after migration | Medium | Medium | Each phase tested independently |
| Performance regression | Low | Medium | Services are thin wrappers only |
| Threading issues | Medium | High | Use volatile fields, proper sync |
| Event loop deadlock | Low | High | Use SwingUtilities.invokeLater() |
| File persistence corruption | Low | High | FileDB unchanged, only wrapped |

---

## Code Review Checklist per Phase

### Phase 0 Review
- [ ] Interfaces are clear and minimal
- [ ] DTOs are immutable (or thread-safe)
- [ ] Events carry only needed data
- [ ] No business logic in interfaces
- [ ] Compile succeeds

### Phase 1 Review  
- [ ] Services are thin wrappers
- [ ] No code duplication
- [ ] Event firing is correct
- [ ] Error handling in place
- [ ] Existing tests still pass
- [ ] New tests added for services

### Phase 2 Review
- [ ] UI components have no business logic
- [ ] Controllers properly wire UI to services
- [ ] Events used correctly (not polling)
- [ ] Threading handled properly
- [ ] All UI components testable in isolation
- [ ] Main window assembles correctly

---

## Deployment Sequence

### MVP Deployment (After Phase 2)
1. Deploy updated jar with:
   - New service interfaces
   - Service implementations
   - Swing UI layer
   - Controllers
   - Main application class

2. Run existing tests first
   - Verify backward compatibility
   - Verify domain logic unchanged

3. Deploy new Swing UI
   - Run in parallel with CLI tools
   - Both paths work simultaneously

4. Gradual migration
   - Users switch to Swing UI when ready
   - Old CLI tools still available
   - No forced migration

---

## What This Approach Achieves

✅ **Zero changes to existing domain logic**  
✅ **Backward compatible - old tests still pass**  
✅ **New Swing UI built without touching core code**  
✅ **Each phase independently testable**  
✅ **Each phase independently reversible**  
✅ **Can deploy UI without changing CLI**  
✅ **Clear separation of concerns**  
✅ **Minimal risk to production**  

---

**Status:** Ready for Phase 0 implementation  
**Next Step:** Write service interfaces (no code changes to existing classes)

