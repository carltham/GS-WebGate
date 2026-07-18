# Configuration System Architecture

## Overview

The TextAnalyser configuration system enables switching between different projects without code changes through an intelligent fallback chain. This document details the configuration management design.

---

## AnalysisConfig Class

**Location:** `com.noprobit.tools.config.AnalysisConfig`

**Responsibility:** Load project configuration from multiple sources with intelligent fallback

### Fallback Chain Strategy

Configuration is loaded in this order of precedence (first match wins):

```
1. ../config/analysis.properties
   ↓ (if not found)
2. config/analysis.properties
   ↓ (if not found)
3. /mnt/DATA/WORKSPACE/Textanalyser/analysis.properties
   ↓ (if not found)
4. /mnt/DATA/WORKSPACE/Textanalyser/*/analysis.properties (subdirs)
   ↓ (if not found)
5. Hardcoded Defaults
   - project.name = "TextAnalyser"
   - source.node.path = "src/main/java"
```

### Implementation Details

```java
public class AnalysisConfig {
    private String projectName;
    private String sourceNodePath;
    
    public AnalysisConfig() {
        loadConfiguration();  // Triggered on instantiation
    }
    
    private void loadConfiguration() {
        // Initialize with defaults
        // Then try each path in fallback order
        // First successful load stops the chain
    }
    
    public String getProjectName() { ... }
    public String getSourceNodePath() { ... }
    public String getReportPrefix() { ... }
}
```

---

## Configuration File Format

### File: `analysis.properties`

```properties
# Project name used in report filenames and metadata
project.name=ProjectName

# Source path to Java source files (relative or absolute)
# Relative paths are resolved from current working directory
# Absolute paths work across different locations
source.node.path=/absolute/path/or/relative/path
```

### Example Configurations

**TextAnalyser (Local Development)**
```properties
project.name=TextAnalyser
source.node.path=src/main/java
```

**GSPos (External Project)**
```properties
project.name=GSPos
source.node.path=/mnt/DATA/Projects/0.present-projects/Active/GSPos/GSPos-pom/GSPos-swing/src/main/java
```

**Multiple Projects (Workspace)**
```properties
# Project A
project.name=ProjectA
source.node.path=/workspace/projecta/src/main/java

# For GSPos in workspace
project.name=GSPos
source.node.path=/workspace/gspos-swing/src/main/java
```

---

## Multi-Module Maven Builds

### Problem
In a Maven multi-module build, the working directory during test execution might not be the project root. The fallback chain addresses this:

**Directory Structure:**
```
TextAnalyser/
  ├── config/analysis.properties          ← Root config
  ├── TextAnalyser-pom/
  │   ├── config/analysis.properties      ← Module-level config
  │   ├── TextAnalyser-jar/
  │   │   └── src/test/java/...
  │   └── TextAnalyser-UI-swing/
  └── analysis/
      ├── textanalyser-analysis-report.csv
      └── textanalyser-analysis-report.md
```

### Solution
1. First tries `../config/analysis.properties` (found in TextAnalyser-pom/)
2. Falls back to `config/analysis.properties` if step 1 fails
3. Ensures project root config is always accessible regardless of execution directory

---

## Runtime Configuration Detection

### How Configuration is Used

```
1. Test Startup
   └─> ProjectClassNameValidationTest.setup()
   
2. Configuration Load
   └─> new AnalysisConfig()
       └─> Attempts fallback chain
   
3. Configuration Access
   ├─> config.getProjectName()        → "GSPos"
   ├─> config.getSourceNodePath()     → absolute path
   └─> config.getReportPrefix()       → "gspos-analysis-report"
   
4. Report Generation
   └─> Uses projectName in filenames
       - gspos-analysis-report.csv
       - gspos-analysis-report.md
       - gspos-analysis-report-20260718.csv (dated)
       - gspos-analysis-report-20260718.md (dated)
```

### Test Code Example

```java
@Test
public void validateAllProjectClassNamesWithSuggestions() throws IOException {
    AnalysisConfig config = new AnalysisConfig();
    String projectName = config.getProjectName();      // From config
    String sourceNodePath = config.getSourceNodePath(); // From config
    
    Path sourceDir = Paths.get(sourceNodePath);        // Create path
    List<ClassAnalysisEngine.AnalysisResult> violations = 
        engine.analyzeProjectClasses(sourceDir);       // Analyze
    
    exportAnalysisReports(projectName, sourceNodePath); // Export with project name
}
```

---

## Switching Between Projects

### Step 1: Update Configuration
Edit `TextAnalyser-pom/config/analysis.properties`:

```properties
# To analyze TextAnalyser (local)
project.name=TextAnalyser
source.node.path=src/main/java

# OR to analyze GSPos (external)
project.name=GSPos
source.node.path=/mnt/DATA/Projects/0.present-projects/Active/GSPos/GSPos-pom/GSPos-swing/src/main/java
```

### Step 2: Run Analysis
```bash
mvn clean test
```

### Step 3: Reports Generated
Reports appear in:
- `analysis/{project-name}-analysis-report.csv`
- `analysis/{project-name}-analysis-report.md`
- `analysis/{project-name}-analysis-report-YYYYMMDD.csv` (dated)
- `analysis/{project-name}-analysis-report-YYYYMMDD.md` (dated)

---

## Configuration Loading Debug

To debug configuration loading, enable verbose output:

```java
// In AnalysisConfig.loadConfiguration()
System.out.println("DEBUG: Trying config path: " + projectRootConfig.toAbsolutePath());
```

Output example:
```
DEBUG: Trying config path: /mnt/DATA/.../TextAnalyser-pom/TextAnalyser-jar/../../../config/analysis.properties
Using config - Project: GSPos, Source: /mnt/DATA/.../GSPos-swing/src/main/java
Analyzing directory: /mnt/DATA/.../GSPos-swing/src/main/java (exists: true)
Found 214 violations
```

---

## Path Resolution

### Relative Paths
Resolved relative to current working directory during execution:

```properties
source.node.path=src/main/java
# Resolves to: {cwd}/src/main/java
```

### Absolute Paths
Used directly without modification:

```properties
source.node.path=/absolute/path/to/src/main/java
# Resolves to: /absolute/path/to/src/main/java
```

### Maven Working Directory
Maven typically runs from the module directory:
- Test execution: `TextAnalyser-pom/TextAnalyser-jar/`
- Config lookup: Checks `../config/analysis.properties` first

---

## Error Handling

### Missing Configuration File
If no configuration file is found at any location:
- Uses hardcoded defaults
- project.name = "TextAnalyser"
- source.node.path = "src/main/java"
- No error thrown (graceful degradation)

### Missing Source Directory
If source.node.path doesn't exist:
- Analysis completes with 0 violations
- Reports generated with empty data
- No error thrown (non-blocking behavior)

### Invalid Properties
If properties are missing or empty:
- Uses defaults for missing properties
- Empty strings treated as missing
- Validation uses trim() before comparison

---

## Best Practices

1. **Keep Local Config Updated**
   - Always update `config/analysis.properties` before switching projects
   - Use absolute paths for external projects
   - Use relative paths for projects within the workspace

2. **Document Your Projects**
   - Add comments to config explaining each project
   - Version control the configuration
   - Keep example configurations

3. **Test Configuration Changes**
   - Run a quick test after config change
   - Check if source directory exists
   - Verify reports are generated with correct project name

4. **Organize External Project Paths**
   - Use consistent path prefixes
   - Avoid hardcoding user-specific paths
   - Consider using environment variables for shared configs

---

## Future Enhancements

1. **Environment Variable Support**
   ```properties
   source.node.path=${PROJECT_ROOT}/src/main/java
   ```

2. **Multiple Project Configuration**
   ```properties
   projects.textanalyser.name=TextAnalyser
   projects.textanalyser.path=src/main/java
   
   projects.gspos.name=GSPos
   projects.gspos.path=/path/to/gspos/src/main/java
   ```

3. **Configuration UI**
   - GUI for selecting projects
   - Dynamic configuration without file editing

4. **Workspace Configuration**
   - Central workspace config for all projects
   - Profile-based configurations
   - Environment-specific settings

