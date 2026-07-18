package com.noprobit.tools;

import com.noprobit.tools.db.FileDB;
import com.noprobit.tools.reporters.ClassNameAnalysisReporter;
import com.noprobit.tools.analyzers.ClassAnalysisEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class ProjectClassNameValidationTest {

    private ClassAnalysisEngine engine;
    private FileDB db;
    private final ClassNameAnalysisReporter reporter = new ClassNameAnalysisReporter();

    @BeforeEach
    public void setup() {
        db = new FileDB(".test-analysis-db");
        engine = new ClassAnalysisEngine(db);
    }

    @Test
    public void validateAllProjectClassNamesWithSuggestions() throws IOException {
        String projectName = "GSPos";
        String sourceNodePath = "GSPos-swing/src/main/java";
        Path sourceDir = Paths.get("../GSPos-swing/src/main/java");
        List<ClassAnalysisEngine.AnalysisResult> violations = engine.analyzeProjectClasses(sourceDir);

        reporter.printReport(violations);

        int dbViolationCount = engine.getStoredViolationCount();
        assertTrue(dbViolationCount == violations.size(),
                "Database stored " + dbViolationCount + " violations but analysis found " + violations.size());

        exportAnalysisReports(projectName, sourceNodePath);

        assertTrue(violations.isEmpty(), reporter.getViolationSummary(violations.size()));
    }

    private void exportAnalysisReports(String projectName, String sourceNodePath) throws IOException {
        Path analysisDir = getAnalysisDirectory();
        Files.createDirectories(analysisDir);

        String dateStamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String latestCSV = analysisDir.resolve("class-analysis-report.csv").toString();
        db.exportToCSV(latestCSV);

        String datedCSV = analysisDir.resolve("class-analysis-report-" + dateStamp + ".csv").toString();
        db.exportToCSV(datedCSV);

        String latestMarkdown = analysisDir.resolve("class-analysis-report.md").toString();
        db.exportToMarkdown(latestMarkdown, projectName, sourceNodePath);

        String datedMarkdown = analysisDir.resolve("class-analysis-report-" + dateStamp + ".md").toString();
        db.exportToMarkdown(datedMarkdown, projectName, sourceNodePath);
    }

    private Path getAnalysisDirectory() {
        return Paths.get("../../analysis");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
