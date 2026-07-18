# TextAnalyser Architecture Documentation

Welcome to the TextAnalyser architecture documentation. This folder contains detailed documentation of the system design, components, and operational characteristics.

---

## Documentation Structure

### 1. **[ARCHITECTURE.md](ARCHITECTURE.md)** - Main Architecture Overview
**Start here for a complete system understanding**

Contains:
- System overview and capabilities
- High-level architecture diagram
- Core module descriptions (7 main systems)
- End-to-end data flow
- Java compatibility and design decisions
- Testing strategy and performance characteristics
- Error handling approach

**Read this if:** You need to understand how all the pieces fit together, or you're onboarding to the project.

---

### 2. **[CONFIGURATION_SYSTEM.md](CONFIGURATION_SYSTEM.md)** - Configuration Management
**For analyzing different projects without code changes**

Contains:
- AnalysisConfig class design
- Fallback chain strategy (5-level priority)
- Configuration file format and examples
- Multi-module Maven build support
- Runtime configuration detection
- Project switching instructions
- Path resolution (relative/absolute)
- Error handling and best practices
- Future enhancement ideas

**Read this if:** You need to analyze a new project, switch between projects, or understand how configuration loading works.

**Quick Start:**
```properties
# Edit: TextAnalyser-pom/config/analysis.properties
project.name=GSPos
source.node.path=/path/to/gspos/src/main/java
# Run: mvn clean test
```

---

### 3. **[ENCODING_SYSTEM.md](ENCODING_SYSTEM.md)** - Automatic Encoding Detection
**For handling legacy projects with mixed encodings**

Contains:
- Problem solved (encoding issues in legacy code)
- AdvancedEncodingEngine design
- BOM (Byte Order Mark) detection algorithm
- File reading with error handling
- NIO channel-based conversion
- Supported charsets and error actions
- Maven compiler UTF-8 configuration
- Performance characteristics
- Usage examples and testing approach

**Read this if:** You're analyzing projects with non-UTF-8 encodings, or you want to understand how special characters are handled.

**Key Feature:**
- Automatic detection of UTF-8, UTF-16BE/LE
- 64KB buffered NIO processing
- Transparent to rest of system

---

### 4. **[LINTING_SYSTEM.md](LINTING_SYSTEM.md)** - Four-Tier Code Analysis
**For understanding the core validation logic**

Contains:
- Four-tier architecture (Class, Method, Import, Ordering)
- Tier 1: Class Naming (PascalCase, special chars, length)
- Tier 2: Method Naming (camelCase, verbs, acronyms)
- Tier 3: Import Analysis (wildcards, duplicates, forbidden)
- Tier 4: Method Ordering (constructors, visibility, accessors)
- LintIssue data structures with severity levels
- Integration with ClassAnalysisEngine
- Report generation format
- Extensibility points for new rules
- Performance analysis
- Configuration possibilities
- Known limitations and future enhancements

**Read this if:** You need to add new linting rules, understand validation logic, or modify analysis behavior.

**Severity Levels:**
- ERROR: Critical violations (must fix)
- WARNING: Best practice violations (should fix)
- INFO: Suggestions (nice to have)

---

## Quick Reference

### System Components at a Glance

| Component | Purpose | Location |
|-----------|---------|----------|
| **AnalysisConfig** | Configuration management | `config/` |
| **AdvancedEncodingEngine** | Encoding detection/conversion | `encoding/` |
| **ClassAnalysisEngine** | Analysis orchestration | `analyzers/` |
| **ClassFileAnalyzer** | Metadata extraction | `analyzers/` |
| **JavaClassLinter** | Class naming validation | `linting/` |
| **JavaMethodLinter** | Method naming validation | `linting/` |
| **JavaImportLinter** | Import validation | `linting/` |
| **JavaMethodOrderLinter** | Method ordering validation | `linting/` |
| **FileDB** | Result persistence | `db/` |
| **ClassNameValidator** | Naming convention checking | `validators/` |
| **ClassNameAnalysisReporter** | Report generation | `reporters/` |

---

### Data Flow Summary

```
Configuration
     ↓
Source Files (auto-detected encoding)
     ↓
Metadata Extraction (via regex)
     ↓
4-Tier Linting Analysis
  ├─ Class Naming (Tier 1)
  ├─ Method Naming (Tier 2)
  ├─ Import Analysis (Tier 3)
  └─ Method Ordering (Tier 4)
     ↓
Persistence (FileDB)
     ↓
Report Generation (CSV & Markdown)
```

---

## Getting Started

### Step 1: Analyze TextAnalyser (Default)
```bash
cd TextAnalyser-pom
mvn clean test
# Reports: analysis/textanalyser-analysis-report.*
```

### Step 2: Switch to GSPos Project
```bash
# Edit: config/analysis.properties
project.name=GSPos
source.node.path=/mnt/DATA/.../GSPos-swing/src/main/java

# Run analysis
mvn clean test
# Reports: analysis/gspos-analysis-report.*
```

### Step 3: Review Reports
- **CSV:** Machine-readable, all metrics
- **Markdown:** Human-readable, visualized tables
- **Dated:** Historical backups with timestamps

---

## Architecture Principles

### 1. **Modularity**
- Clear separation of concerns (4 tiers)
- Each linter is independent
- Easy to extend with new rules

### 2. **Configuration-Driven**
- No code changes to analyze different projects
- Fallback chain ensures robustness
- Properties-based configuration

### 3. **Encoding-Safe**
- Automatic BOM detection
- Support for legacy charsets
- NIO-based efficient processing

### 4. **Transparent Persistence**
- Text-based FileDB (inspectable)
- Automatic caching
- Clear record structure

### 5. **Multi-Format Reports**
- CSV for tools/integration
- Markdown for humans
- Dated backups for history

### 6. **Error Resilience**
- Non-blocking analysis (skip unreadable files)
- Graceful degradation (defaults if config missing)
- Detailed error reporting

---

## Key Statistics

### TextAnalyser Project
- **Classes Analyzed:** 16
- **Total Methods:** 200+
- **Import Statements:** 50+
- **Analysis Time:** <1 second
- **Report Size (CSV):** 2.7 KB
- **Report Size (Markdown):** 3.5 KB

### GSPos Project
- **Classes Analyzed:** 216
- **Methods Analyzed:** 1000+
- **Import Statements:** 500+
- **Analysis Time:** ~2 seconds
- **Report Size (CSV):** 32 KB
- **Report Size (Markdown):** 23 KB
- **Naming Violations:** 142 (34% compliance)

---

## Deployment & Operations

### Build
```bash
mvn clean install
# All modules compile to target/
# JAR: TextAnalyser-jar-1.0-SNAPSHOT.jar
```

### Test
```bash
mvn clean test
# Runs: ProjectClassNameValidationTest
# Runs: EncodingSwitcherTest
# Generates: analysis/reports
```

### Configuration
- Root: `config/analysis.properties`
- Per-module: `{module}/config/analysis.properties`
- Workspace: `/mnt/DATA/WORKSPACE/Textanalyser/analysis.properties`

### Reports Location
```
TextAnalyser/
  └── analysis/
      ├── textanalyser-analysis-report.csv
      ├── textanalyser-analysis-report.md
      ├── textanalyser-analysis-report-20260718.csv
      ├── textanalyser-analysis-report-20260718.md
      ├── gspos-analysis-report.csv
      ├── gspos-analysis-report.md
      └── ...
```

---

## Troubleshooting

### Reports Not Generated
1. Check if source directory exists
2. Verify configuration file loaded (check console output)
3. Check file permissions in analysis/ directory

### Encoding Issues
1. Check file actual encoding (file command)
2. Verify UTF-8 in Maven config
3. Check for mixed encodings in project

### Analysis Takes Too Long
1. Reduce project size (limit to directory)
2. Check for large binary files in source tree
3. Consider parallel analysis (future feature)

### Missing Classes in Results
1. Verify source path points to correct directory
2. Check if Java files are in correct package structure
3. Ensure files use .java extension

---

## Future Roadmap

### Phase 1: Enhanced Detection
- [ ] Heuristic encoding detection (no BOM)
- [ ] Language-based rule configuration
- [ ] Per-team configuration profiles

### Phase 2: Auto-Fix
- [ ] Generate corrected source code
- [ ] Batch rename operations
- [ ] Automatic import organization

### Phase 3: Integration
- [ ] REST API wrapper
- [ ] IDE plugin (VS Code, IntelliJ)
- [ ] GitHub Actions integration
- [ ] SonarQube plugin

### Phase 4: Advanced Analysis
- [ ] AST-based analysis
- [ ] Type checking
- [ ] Dependency analysis
- [ ] Complexity metrics

---

## Contributing

To extend TextAnalyser:

1. **New Linting Rule:** Add to existing Linter class
2. **New Linter Tier:** Implement LintRule interface
3. **New Report Format:** Extend FileDB export method
4. **New Encoding Support:** Add BOM to AdvancedEncodingEngine

See individual architecture docs for specific extension points.

---

## Contact & Support

- **Documentation:** This folder (`architecture/`)
- **Source Code:** `TextAnalyser-pom/TextAnalyser-jar/src/main/java/`
- **Tests:** `TextAnalyser-pom/TextAnalyser-jar/src/test/java/`
- **Configuration:** `TextAnalyser-pom/config/analysis.properties`

---

## Document Versions

| File | Last Updated | Version |
|------|--------------|---------|
| ARCHITECTURE.md | 2026-07-18 | 1.0 |
| CONFIGURATION_SYSTEM.md | 2026-07-18 | 1.0 |
| ENCODING_SYSTEM.md | 2026-07-18 | 1.0 |
| LINTING_SYSTEM.md | 2026-07-18 | 1.0 |
| README.md | 2026-07-18 | 1.0 |

---

## License

TextAnalyser Architecture Documentation  
Part of the TextAnalyser project  
Documentation created: 2026-07-18

---

**Happy analyzing!** 🚀

Start with [ARCHITECTURE.md](ARCHITECTURE.md) for a complete overview, then dive into specific components based on your needs.

