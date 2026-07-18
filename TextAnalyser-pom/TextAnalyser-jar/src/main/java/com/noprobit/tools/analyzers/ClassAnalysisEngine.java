package com.noprobit.tools.analyzers;

import com.noprobit.tools.db.FileDB;
import com.noprobit.tools.linting.JavaClassLinter;
import com.noprobit.tools.linting.JavaMethodLinter;
import com.noprobit.tools.linting.JavaImportLinter;
import com.noprobit.tools.linting.JavaMethodOrderLinter;
import com.noprobit.tools.reporters.ClassNameSuggester;
import com.noprobit.tools.validators.ClassNameValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ClassAnalysisEngine {

    private final ClassFileAnalyzer fileAnalyzer = new ClassFileAnalyzer();
    private final ClassNameValidator validator = new ClassNameValidator();
    private final ClassNameSuggester suggester = new ClassNameSuggester();
    private final PurposeAnalyser purposeAnalyser = new PurposeAnalyser();
    private final JavaClassLinter classLinter = new JavaClassLinter();
    private final JavaMethodLinter methodLinter = new JavaMethodLinter();
    private final JavaImportLinter importLinter = new JavaImportLinter();
    private final JavaMethodOrderLinter methodOrderLinter = new JavaMethodOrderLinter();
    private final FileDB database;

    public ClassAnalysisEngine() {
        this(new FileDB());
    }

    public ClassAnalysisEngine(FileDB database) {
        this.database = database;
    }

    public static class AnalysisResult {
        public String fullName;
        public String currentName;
        public String suggestedName;
        public String purpose;
        public String extendsClass;
        public ClassNameValidator.ValidationResult validationResult;
        public ClassNameSuggester.SuggestionResult suggestionResult;
        public List<JavaClassLinter.LintIssue> lintIssues;
        public boolean hasErrors;
        public Map<String, List<JavaMethodLinter.LintIssue>> methodLintIssues;
        public int totalMethodIssues;
        public List<JavaImportLinter.LintIssue> importLintIssues;
        public int totalImportIssues;
        public List<JavaMethodOrderLinter.LintIssue> methodOrderIssues;
        public int totalMethodOrderIssues;
    }

    public List<AnalysisResult> analyzeProjectClasses(Path sourceDir) throws IOException {
        List<AnalysisResult> violations = new ArrayList<>();

        if (!Files.exists(sourceDir)) {
            System.out.println("Source directory not found: " + sourceDir);
            return violations;
        }

        try (var stream = Files.walk(sourceDir)) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String content = new String(Files.readAllBytes(path));
                            AnalysisResult result = analyzeClassFile(content);
                            if (result != null) {
                                violations.add(result);
                                database.storeAnalysisResult(result);
                            }
                        } catch (IOException e) {
                            // Skip files with read errors
                        }
                    });
        }

        return violations;
    }

    private AnalysisResult analyzeClassFile(String fileContent) {
        String className = fileAnalyzer.extractClassName(fileContent);

        if (className == null) {
            return null;
        }

        String packageName = fileAnalyzer.extractPackageName(fileContent);
        String extendsClass = fileAnalyzer.extractExtendsClass(fileContent);

        AnalysisResult result = new AnalysisResult();
        result.fullName = fileAnalyzer.getFullyQualifiedName(packageName, className);
        result.currentName = className;
        result.extendsClass = extendsClass;

        result.validationResult = validator.validate(className);
        result.suggestionResult = suggester.suggest(className, extendsClass);
        result.suggestedName = result.suggestionResult.suggestedName;
        result.purpose = purposeAnalyser.analyzePurpose(className, extendsClass);

        result.lintIssues = classLinter.analyze(className);
        result.hasErrors = result.lintIssues.stream()
                .anyMatch(issue -> issue.severity == JavaClassLinter.Severity.ERROR);

        result.methodLintIssues = new HashMap<>();
        List<String> methodNames = fileAnalyzer.extractMethodNames(fileContent);
        for (String methodName : methodNames) {
            List<JavaMethodLinter.LintIssue> methodIssues = methodLinter.analyze(methodName);
            if (!methodIssues.isEmpty()) {
                result.methodLintIssues.put(methodName, methodIssues);
            }
        }
        result.totalMethodIssues = result.methodLintIssues.values().stream()
                .mapToInt(List::size).sum();

        result.importLintIssues = new ArrayList<>();
        List<String> imports = fileAnalyzer.extractImports(fileContent);
        if (!imports.isEmpty()) {
            result.importLintIssues = importLinter.analyze(imports);
        }
        result.totalImportIssues = result.importLintIssues.size();

        result.methodOrderIssues = new ArrayList<>();
        List<JavaMethodOrderLinter.MethodMetadata> methodMetadata = fileAnalyzer.extractMethodMetadata(className, fileContent);
        if (!methodMetadata.isEmpty()) {
            result.methodOrderIssues = methodOrderLinter.analyze(methodMetadata);
        }
        result.totalMethodOrderIssues = result.methodOrderIssues.size();

        return result;
    }

    public ClassNameValidator getValidator() {
        return validator;
    }

    public ClassNameSuggester getSuggester() {
        return suggester;
    }

    public PurposeAnalyser getPurposeAnalyser() {
        return purposeAnalyser;
    }

    public FileDB getDatabase() {
        return database;
    }

    public List<FileDB.AnalysisRecord> getStoredViolations() throws IOException {
        return database.getViolations();
    }

    public int getStoredViolationCount() throws IOException {
        return database.getViolationCount();
    }
}
