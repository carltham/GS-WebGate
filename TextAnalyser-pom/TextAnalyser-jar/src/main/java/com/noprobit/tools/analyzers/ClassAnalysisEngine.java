package com.noprobit.tools.analyzers;

import com.noprobit.tools.db.FileDB;
import com.noprobit.tools.linting.JavaClassLinter;
import com.noprobit.tools.reporters.ClassNameSuggester;
import com.noprobit.tools.validators.ClassNameValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ClassAnalysisEngine {

    private final ClassFileAnalyzer fileAnalyzer = new ClassFileAnalyzer();
    private final ClassNameValidator validator = new ClassNameValidator();
    private final ClassNameSuggester suggester = new ClassNameSuggester();
    private final PurposeAnalyser purposeAnalyser = new PurposeAnalyser();
    private final JavaClassLinter linter = new JavaClassLinter();
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

        result.lintIssues = linter.analyze(className);
        result.hasErrors = result.lintIssues.stream()
                .anyMatch(issue -> issue.severity == JavaClassLinter.Severity.ERROR);

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
