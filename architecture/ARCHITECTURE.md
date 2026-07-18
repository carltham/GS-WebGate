# TextAnalyser Architecture Documentation

## System Overview

TextAnalyser is a comprehensive Java code quality analysis and linting platform that validates Java classes, methods, imports, and method organization across Maven projects. It generates detailed reports in CSV and Markdown formats with intelligent configuration management and multi-project support.

**Key Capabilities:**
- Four-tier linting system (class, method, import, method ordering)
- Automatic encoding detection and conversion
- Configuration-driven multi-project analysis
- FileDB persistence layer for analysis results
- Detailed report generation (CSV & Markdown)

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    TextAnalyser Platform                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐         ┌──────────────────────────┐  │
│  │  AnalysisConfig  │         │ ClassAnalysisEngine      │  │
│  │  (Properties)    │────────▶│ (Orchestrator)           │  │
│  └──────────────────┘         └───────────┬──────────────┘  │
│                                           │                  │
│                  ┌────────────────────────┴──────────────┐  │
│                  │                                       │   │
│        ┌─────────▼─────────┐              ┌──────────────▼──┐
│        │ ClassFileAnalyzer │              │ Linting System  │
│        │ (Extract metadata)│              │ (4-tier)        │
│        └────────┬──────────┘              └────────┬─────────┘
│                 │                                  │          │
│        ┌────────▼──────────┐           ┌──────────▼────────┐ │
│        │AdvancedEncoding   │           │ JavaClassLinter  │ │
│        │ Engine            │           │ JavaMethodLinter │ │
│        │ (BOM Detection)   │           │ JavaImportLinter │ │
│        └───────────────────┘           │ JavaMethodOrder  │ │
│                                        │ Linter           │ │
│                                        └──────────┬───────┘ │
│                                                   │          │
│                                        ┌──────────▼────────┐ │
│                                        │ FileDB            │ │
│                                        │ (Persistence)     │ │
│                                        └──────────┬────────┘ │
│                                                   │          │
│                                        ┌──────────▼────────┐ │
│                                        │ Report Exporters  │ │
│                                        │ - CSV Export      │ │
│                                        │ - Markdown Export │ │
│                                        └───────────────────┘ │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Core Modules

### 1. **Configuration Management** (`config/`)
**Responsible for:** Loading project-specific analysis parameters with intelligent fallback chains

**Classes:**
- `AnalysisConfig` - Configuration loader with 3-level fallback strategy

**Fallback Chain:**
1. `../config/analysis.properties` (parent directory for multi-module builds)
2. `config/analysis.properties` (project root)
3. `/mnt/DATA/WORKSPACE/Textanalyser/analysis.properties` (workspace)
4. Hardcoded defaults (TextAnalyser, src/main/java)

**Properties Supported:**
- `project.name` - Used in report filenames and metadata
- `source.node.path` - Path to Java source directory (relative or absolute)

---

### 2. **Encoding System** (`encoding/`)
**Responsible for:** Detecting and converting file encodings transparently

**Classes:**
- `AdvancedEncodingEngine` - BOM detection and charset conversion
- `EncodingSwitcher` - Legacy encoding utilities
- `CharsetEncodingStrategy` - Strategy pattern for encoding operations
- `EncodingResult` - Result wrapper for encoding operations

**Features:**
- Automatic BOM (Byte Order Mark) detection
- Support for UTF-8, UTF-16BE, UTF-16LE
- Strict error handling with `CodingErrorAction.REPORT`
- 64KB buffered NIO channel reading for efficiency
- Safe character encoding/decoding with error recovery

---

### 3. **Analysis Engine** (`analyzers/`)
**Responsible for:** Orchestrating code analysis across all four linting tiers

**Classes:**
- `ClassAnalysisEngine` - Main orchestrator, coordinates all linters
- `ClassFileAnalyzer` - Regex-based metadata extraction from source files
- `PurposeAnalyser` - Infers class purpose from naming patterns
- `ClassAnalysisEngine.AnalysisResult` - Unified result object combining all analysis tiers

**Data Flow:**
1. Read source file with encoding detection
2. Extract class/package/method metadata
3. Run class naming validation
4. Run method naming linting
5. Run import analysis
6. Run method ordering analysis
7. Combine results into AnalysisResult
8. Store in FileDB and collect for reporting

---

### 4. **Linting System** (4-Tier Architecture) (`linting/`)
**Responsible for:** Rule-based validation at each code level

#### Tier 1: Class Naming (`JavaClassLinter`)
- PascalCase validation
- Reserved keyword detection
- Special character checking
- Length constraints
- Severity levels: ERROR, WARNING, INFO

#### Tier 2: Method Naming (`JavaMethodLinter`)
- camelCase validation
- Acronym handling
- Special character validation
- Verb prefix recommendations
- Per-method issue tracking

#### Tier 3: Import Analysis (`JavaImportLinter`)
- Wildcard import detection
- Duplicate import detection
- Forbidden package blocking
- Import grouping enforcement
- List-based issue collection

#### Tier 4: Method Ordering (`JavaMethodOrderLinter`)
- Constructor position validation
- Visibility step-down enforcement
- Accessor positioning at end
- Method metadata tracking (visibility, type, position)

**Common Interface Pattern:**
```java
public List<LintIssue> analyze(String|List input)
```

---

### 5. **Validation System** (`validators/`)
**Responsible for:** Name validation and convention checking

**Classes:**
- `ClassNameValidator` - PascalCase validation with detailed reasoning
- `ClassNameValidator.ValidationResult` - Validation outcome with human-readable messages

**Validation Output Format:** `ClassName - [OK|ERROR] Reason`

---

### 6. **Reporting System** (`reporters/`)
**Responsible for:** Report generation and analysis presentation

**Classes:**
- `ClassNameAnalysisReporter` - Console output formatter
- `ClassNameSuggester` - Intelligent naming suggestions
- `ClassNameAnalysisReporter.SuggestionResult` - Suggestion wrapper

**Output Formats:**
- Console: Formatted violation display
- CSV: Machine-readable tabular format
- Markdown: Human-readable with metrics and tables

---

### 7. **Persistence Layer** (`db/`)
**Responsible for:** Storing and retrieving analysis results

**Classes:**
- `FileDB` - File-based database using text serialization
- `FileDB.AnalysisRecord` - Individual analysis result record

**Features:**
- Text-based storage in `.analysis.txt` format
- In-memory caching for performance
- UTF-8 encoding with proper BOM handling
- CSV/Markdown export with all analysis data
- Timestamp tracking for each record

**Storage Structure:**
```
.analysis-db/
  ├── com_noprobit_tools_analyzers_ClassAnalysisEngine.analysis.txt
  ├── com_noprobit_tools_linting_JavaClassLinter.analysis.txt
  └── ...
```

---

## Data Flow: End-to-End

### Analysis Pipeline

```
1. Configuration Loading
   └─> AnalysisConfig detects project name and source path
   
2. Source Discovery
   └─> Walk filesystem for *.java files
   
3. For Each Java File
   ├─> Encoding Detection (AdvancedEncodingEngine)
   ├─> Content Reading (with automatic charset conversion)
   ├─> Metadata Extraction (ClassFileAnalyzer)
   ├─> Class Analysis
   │   ├─> Class Naming Validation (JavaClassLinter)
   │   ├─> Method Name Linting (JavaMethodLinter)
   │   ├─> Import Analysis (JavaImportLinter)
   │   └─> Method Ordering (JavaMethodOrderLinter)
   ├─> Purpose Analysis (PurposeAnalyser)
   ├─> Suggestion Generation (ClassNameSuggester)
   └─> Result Storage (FileDB)
   
4. Report Generation
   ├─> CSV Export with metrics
   ├─> Markdown Export with detailed analysis
   └─> Dated backups (YYYYMMDD suffix)
```

### Report Structure

**CSV Columns:**
- Full Name, Current Name, Suggested Name
- Extends Class, Purpose
- Validation Result, Lint Errors, Lint Warnings
- Lint Issues Summary, Timestamp

**Markdown Sections:**
- Executive Summary with Key Metrics
- Analysis Scope and Methodology
- Naming Violations Table
- Remediation Guidance
- Compliant Classes List
- Conclusion with Action Items

---

## Configuration & Multi-Project Support

### Configuration File Format
```properties
# Project name used in report filenames
project.name=ProjectName

# Source path (relative or absolute)
source.node.path=/path/to/src/main/java
```

### Report File Naming
```
{project-name}-analysis-report.csv
{project-name}-analysis-report.md
{project-name}-analysis-report-YYYYMMDD.csv
{project-name}-analysis-report-YYYYMMDD.md
```

### Switching Projects
Simply update `config/analysis.properties`:
- **TextAnalyser:** `project.name=TextAnalyser`, `source.node.path=src/main/java`
- **GSPos:** `project.name=GSPos`, `source.node.path=/mnt/DATA/.../GSPos-swing/src/main/java`

---

## Java Compatibility

- **Target Version:** Java 11+
- **Build Tool:** Maven 3.6.3+
- **Character Encoding:** UTF-8 (enforced via pom.xml)
- **Source Encoding:** `-Dproject.build.sourceEncoding=UTF-8`

**Maven Configuration:**
```xml
<maven.compiler.source>11</maven.compiler.source>
<maven.compiler.target>11</maven.compiler.target>
<encoding>UTF-8</encoding>
```

---

## Key Design Decisions

1. **Modular Rule-Based Linting**
   - Four separate linters for different code aspects
   - Easy to extend with new rules
   - Clear separation of concerns

2. **Configuration-Driven Analysis**
   - No code changes needed to analyze different projects
   - Fallback chain ensures robustness
   - Multi-project support via simple config updates

3. **File-Based Persistence**
   - Simple text-based database (not SQL)
   - Easy to inspect and debug
   - UTF-8 encoding throughout

4. **Dual Report Formats**
   - CSV for data processing and integration
   - Markdown for human readability
   - Dated backups for historical tracking

5. **Encoding Auto-Detection**
   - Handles legacy projects with mixed encodings
   - BOM detection prevents double-encoding issues
   - Strict error handling with detailed diagnostics

---

## Testing Strategy

**Test Coverage:**
- `ProjectClassNameValidationTest` - End-to-end analysis verification
- `EncodingSwitcherTest` - Encoding conversion validation

**Test Database:**
- Isolated `.test-analysis-db` directory
- Clean state per test run
- No interference with production database

---

## Performance Characteristics

- **File Reading:** 64KB buffered NIO channels (AdvancedEncodingEngine)
- **Caching:** In-memory cache in FileDB for repeated lookups
- **Report Generation:** Streaming to file (no memory accumulation)
- **Typical Analysis:** 200+ classes in <5 seconds

---

## Error Handling

1. **Encoding Errors:** `CharacterCodingException` with `CodingErrorAction.REPORT`
2. **IO Errors:** Skipped with logging (non-blocking)
3. **Malformed Code:** Gracefully handled with fallback patterns
4. **Missing Config:** Uses hardcoded defaults
5. **Lint Violations:** Reported in analysis results (not exceptions)

---

## Future Extension Points

1. **New Linting Rules:** Implement `LintRule` interface
2. **New Encodings:** Add BOM patterns to `AdvancedEncodingEngine`
3. **Custom Reports:** Extend `FileDB` export methods
4. **Database Backends:** Replace FileDB with SQL persistence
5. **Remote Analysis:** REST API wrapper around ClassAnalysisEngine
6. **IDE Integration:** LSP or plugin architecture

---

## Architecture Validation Checklist

- [x] Modular design with clear responsibilities
- [x] Configuration-driven for multi-project support
- [x] Encoding handling for legacy codebases
- [x] Persistent storage of results
- [x] Multiple report formats
- [x] Testable with isolated test database
- [x] Java 11 compatible
- [x] UTF-8 safe throughout pipeline
- [x] Extensible linting system
- [x] Performance optimized (NIO, caching)

