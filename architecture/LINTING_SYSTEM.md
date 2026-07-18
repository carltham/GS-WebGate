# Four-Tier Linting System Architecture

## Overview

The TextAnalyser linting system validates Java code across four distinct levels:
1. **Class Naming** - Class declaration validation
2. **Method Naming** - Method signature validation
3. **Import Analysis** - Import statement validation
4. **Method Ordering** - Method organization validation

This modular design allows independent analysis of each code aspect with clear separation of concerns.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│          ClassAnalysisEngine                        │
│   (Orchestrates 4-Tier Analysis)                    │
└────────────────┬────────────────────────────────────┘
                 │
     ┌───────────┼───────────┬──────────────┐
     │           │           │              │
     ▼           ▼           ▼              ▼
┌──────────┐ ┌────────┐ ┌────────┐ ┌──────────────┐
│ Tier 1   │ │ Tier 2 │ │ Tier 3 │ │ Tier 4       │
│ Class    │ │ Method │ │ Import │ │ Method       │
│ Linter   │ │ Linter │ │ Linter │ │ Order Linter │
└──────────┘ └────────┘ └────────┘ └──────────────┘
```

---

## Tier 1: Class Naming Linter

**Location:** `com.noprobit.tools.linting.JavaClassLinter`

**Responsibility:** Validate class declarations follow naming conventions

### Rules Implemented

#### 1. PascalCaseRule
- **Check:** Class name starts with uppercase letter
- **Severity:** ERROR
- **Pattern:** `^[A-Z][a-zA-Z0-9]*$`
- **Example Valid:** `MyClass`, `ClassAnalysisEngine`
- **Example Invalid:** `myClass`, `_MyClass`

#### 2. CompilerRule
- **Check:** Class name is valid Java identifier
- **Severity:** ERROR
- **Validation:** No special characters except `_` and `$`

#### 3. SpecialCharacterRule
- **Check:** No underscores in class names (Java convention)
- **Severity:** WARNING
- **Pattern:** Must not contain `_`
- **Reason:** PascalCase preferred over snake_case

#### 4. LengthRule
- **Check:** Class name within reasonable limits
- **Severity:** WARNING
- **Limits:** 
  - Minimum: 1 character
  - Maximum: 255 characters
  - Recommendation: 2-64 characters

### Data Structures

```java
public class JavaClassLinter {
    public static enum Severity {
        ERROR,     // Critical naming violation
        WARNING,   // Convention deviation
        INFO       // Informational message
    }
    
    public static class LintIssue {
        public String ruleId;           // e.g., "PascalCaseRule"
        public Severity severity;       // ERROR, WARNING, INFO
        public String message;          // Human-readable description
    }
    
    public List<LintIssue> analyze(String className) {
        // Returns list of violations found
    }
}
```

### Analysis Example

```
Input:  "myClass"
Output: [
  {
    ruleId: "PascalCaseRule",
    severity: ERROR,
    message: "Class name must start with uppercase letter"
  }
]

Input:  "MyClass_Helper"
Output: [
  {
    ruleId: "SpecialCharacterRule",
    severity: WARNING,
    message: "Class name contains underscore - use PascalCase instead"
  }
]
```

---

## Tier 2: Method Naming Linter

**Location:** `com.noprobit.tools.linting.JavaMethodLinter`

**Responsibility:** Validate method declarations follow naming conventions

### Rules Implemented

#### 1. CamelCaseRule
- **Check:** Method name starts with lowercase, uses camelCase
- **Severity:** ERROR
- **Pattern:** `^[a-z][a-zA-Z0-9]*$`
- **Example Valid:** `getValue`, `getFirstName`
- **Example Invalid:** `GetValue`, `get_value`

#### 2. SpecialCharacterRule
- **Check:** No underscores in method names
- **Severity:** WARNING
- **Reason:** Java convention uses camelCase, not snake_case

#### 3. PrefixVerbRule
- **Check:** Method names start with action verbs (recommended)
- **Severity:** INFO
- **Common Verbs:** get, set, is, has, do, create, delete, update
- **Note:** Suggestion only, not enforced

#### 4. AcronymRule
- **Check:** Acronyms in method names handled correctly
- **Severity:** WARNING
- **Rule:** `IOError` not `IOerror`, `getHTTPStatus` not `getHTtpStatus`

### Data Structures

```java
public class JavaMethodLinter {
    public static class LintIssue {
        public String methodName;
        public String ruleId;
        public Severity severity;
        public String message;
    }
    
    public List<LintIssue> analyze(String methodName) {
        // Returns list of violations found
    }
}
```

### Analysis Example

```
Input:  "GetValue"
Output: [
  {
    methodName: "GetValue",
    ruleId: "CamelCaseRule",
    severity: ERROR,
    message: "Method name must start with lowercase letter"
  }
]

Input:  "getValue"
Output: [] // Compliant

Input:  "get_current_value"
Output: [
  {
    methodName: "get_current_value",
    ruleId: "SpecialCharacterRule",
    severity: WARNING,
    message: "Method name contains underscore - use camelCase instead"
  }
]
```

---

## Tier 3: Import Analysis Linter

**Location:** `com.noprobit.tools.linting.JavaImportLinter`

**Responsibility:** Validate import statements follow best practices

### Rules Implemented

#### 1. NoWildcardImportRule
- **Check:** No wildcard imports (`import java.util.*`)
- **Severity:** WARNING
- **Reason:** Reduces clarity; unknown which classes are imported
- **Fix:** Import specific classes only

#### 2. DuplicateImportRule
- **Check:** No duplicate import statements
- **Severity:** WARNING
- **Example Issue:** Importing same class twice
- **Fix:** Remove duplicates

#### 3. ForbiddenPackageRule
- **Check:** No imports from forbidden packages (configurable)
- **Severity:** ERROR
- **Default Forbidden:**
  - `sun.*` (internal JVM packages)
  - `com.sun.*` (implementation-specific)

#### 4. ImportGroupingRule
- **Check:** Imports organized by package group
- **Severity:** INFO
- **Standard Order:**
  1. Java standard library (`java.*`)
  2. Third-party libraries (`org.*`, `com.*`)
  3. Project packages (`com.noprobit.*`)
  4. Static imports (at end)

### Data Structures

```java
public class JavaImportLinter {
    public static class LintIssue {
        public String importStatement;
        public String ruleId;
        public Severity severity;
        public String message;
    }
    
    public List<LintIssue> analyze(List<String> imports) {
        // Returns list of violations found
    }
}
```

### Analysis Example

```
Inputs:
  [
    "import java.util.*;",           // Wildcard
    "import java.util.List;",
    "import java.util.List;",        // Duplicate
    "import sun.misc.Unsafe;",       // Forbidden
    "import org.junit.jupiter.api.*;" // Wildcard
  ]

Output:
  [
    {
      importStatement: "import java.util.*;",
      ruleId: "NoWildcardImportRule",
      severity: WARNING,
      message: "Avoid wildcard imports"
    },
    {
      importStatement: "import java.util.List;",
      ruleId: "DuplicateImportRule",
      severity: WARNING,
      message: "Duplicate import statement"
    },
    {
      importStatement: "import sun.misc.Unsafe;",
      ruleId: "ForbiddenPackageRule",
      severity: ERROR,
      message: "Import from forbidden package: sun.*"
    }
  ]
```

---

## Tier 4: Method Ordering Linter

**Location:** `com.noprobit.tools.linting.JavaMethodOrderLinter`

**Responsibility:** Validate method organization within class

### Rules Implemented

#### 1. ConstructorPositionRule
- **Check:** Constructors appear before other methods
- **Severity:** WARNING
- **Order:** Constructors first (public → protected → private)

#### 2. VisibilityStepDownRule
- **Check:** Methods organized by visibility level
- **Severity:** WARNING
- **Order:** 
  1. Public methods
  2. Protected methods
  3. Package-private methods
  4. Private methods

#### 3. AccessorPositionRule
- **Check:** Getters/setters at end of class
- **Severity:** INFO
- **Pattern:** Methods starting with `get`, `set`, `is`, `has`
- **Reason:** Separates core logic from accessors

### MethodMetadata Class

```java
public static class MethodMetadata {
    public String methodName;
    public Visibility visibility;        // PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE
    public boolean isConstructor;
    public boolean isGetterOrSetter;
    public int lineNumber;               // Position in source file
}

public enum Visibility {
    PUBLIC,
    PROTECTED,
    PACKAGE_PRIVATE,
    PRIVATE
}
```

### Analysis Result

```java
public static class LintIssue {
    public String methodName;
    public String ruleId;
    public Severity severity;
    public String message;
    public int expectedLine;
    public int actualLine;
}
```

### Analysis Example

```
Source Order:
  1. public void getter()
  2. private void helper()
  3. public MyClass()           // Constructor after methods!
  4. private int field;

Issues Found:
  [
    {
      methodName: "MyClass",
      ruleId: "ConstructorPositionRule",
      severity: WARNING,
      message: "Constructor should appear before other methods",
      expectedLine: 3,    // Should be here
      actualLine: 3       // Constructor found here
    }
  ]

Recommended Order:
  1. public MyClass()           // Constructor first
  2. public void getter()       // Public methods
  3. private void helper()      // Private methods
  4. // Getters/setters at end (if present)
```

---

## Integration with ClassAnalysisEngine

### Analysis Pipeline

```java
public AnalysisResult analyzeClassFile(String fileContent) {
    String className = fileAnalyzer.extractClassName(fileContent);
    
    // Tier 1: Class Naming
    result.lintIssues = classLinter.analyze(className);
    
    // Tier 2: Method Naming
    List<String> methodNames = fileAnalyzer.extractMethodNames(fileContent);
    result.methodLintIssues = new HashMap<>();
    for (String methodName : methodNames) {
        List<JavaMethodLinter.LintIssue> methodIssues = methodLinter.analyze(methodName);
        if (!methodIssues.isEmpty()) {
            result.methodLintIssues.put(methodName, methodIssues);
        }
    }
    
    // Tier 3: Import Analysis
    List<String> imports = fileAnalyzer.extractImports(fileContent);
    result.importLintIssues = importLinter.analyze(imports);
    
    // Tier 4: Method Ordering
    List<JavaMethodOrderLinter.MethodMetadata> methodMetadata = 
        fileAnalyzer.extractMethodMetadata(className, fileContent);
    result.methodOrderIssues = methodOrderLinter.analyze(methodMetadata);
    
    return result;
}
```

---

## Report Generation

### CSV Format

Tier 1 (Class Linting):
```
Lint Errors, Lint Warnings, Lint Issues
0,           0,            None
```

Tier 2-4 (Methods, Imports, Ordering):
```
Total Violations = 
  methodLintIssues.sum() + 
  importLintIssues.count() + 
  methodOrderIssues.count()
```

### Markdown Format

```markdown
## Naming Violations & Linting Issues

| Class | Suggested | Issues |
|-------|-----------|--------|
| MyClass | MyClass | 0 errors, 1 warning |

### Linting Details
- **Class Linting:** [Issues found]
- **Method Linting:** [Issues found]  
- **Import Linting:** [Issues found]
- **Method Ordering:** [Issues found]
```

---

## Extensibility

### Adding a New Rule

1. **Create LintIssue in existing linter**
2. **Implement rule logic in analyze() method**
3. **Add test coverage**
4. **Update documentation**

### Example: Adding MaxMethodLengthRule

```java
// In JavaMethodLinter class
private List<LintIssue> checkMethodLength(String methodName) {
    if (methodName.length() > 128) {
        return Arrays.asList(
            new LintIssue(
                "MaxMethodLengthRule",
                Severity.WARNING,
                "Method name exceeds 128 characters"
            )
        );
    }
    return Collections.emptyList();
}
```

---

## Performance Characteristics

### Analysis Time Complexity
- **Tier 1:** O(1) - single string validation
- **Tier 2:** O(n) - n methods × rules
- **Tier 3:** O(m) - m imports × rules
- **Tier 4:** O(m log m) - sorting methods by visibility/position

**Total:** O(n + m) for typical files (n = methods, m = imports)

### Typical Performance
- **Per Class:** 1-5ms
- **Per Project:** 200+ classes in <5 seconds
- **Bottleneck:** File I/O and regex extraction, not linting

---

## Severity Levels

### ERROR (Critical)
- Breaks Java conventions
- Must be fixed
- Examples: PascalCase violation, camelCase violation, forbidden imports

### WARNING (Important)
- Violates best practices
- Should be fixed
- Examples: Wildcard imports, underscores, constructor ordering

### INFO (Informational)
- Suggestions for improvement
- Nice to have
- Examples: Verb prefixes, accessor positioning

---

## Configuration Points

### Per-Linter Configuration (Future)

```properties
# JavaClassLinter
linting.class.max_length=255
linting.class.require_prefix=false

# JavaMethodLinter
linting.method.min_length=1
linting.method.require_verb_prefix=false

# JavaImportLinter
linting.import.forbidden_packages=sun.*,com.sun.*

# JavaMethodOrderLinter
linting.method_order.enforce_grouping=true
```

---

## Known Limitations

1. **Regex-Based Extraction**
   - Cannot handle all Java syntax edge cases
   - Fails on malformed code
   - No AST-based analysis

2. **Method Position Tracking**
   - Line numbers approximate
   - Inner classes not separately tracked
   - Anonymous classes not analyzed

3. **Import Organization**
   - Single pass analysis
   - No reordering capability
   - Only detects violations, doesn't fix

4. **No Type Analysis**
   - Cannot validate method return types
   - Cannot check parameter types
   - No inheritance analysis

---

## Future Enhancements

1. **AST-Based Analysis**
   - Use JavaParser or similar
   - Accurate syntax tree analysis
   - Better error recovery

2. **Auto-Fix Capability**
   - Generate corrected code
   - Batch rename operations
   - Automatic import reorganization

3. **Configurable Rules**
   - Enable/disable individual rules
   - Adjust severity levels
   - Custom rule definitions

4. **IDE Integration**
   - Real-time linting feedback
   - Quick-fix suggestions
   - Inline error display

5. **Metrics Collection**
   - Trend analysis over time
   - Compliance rate tracking
   - Per-module statistics

