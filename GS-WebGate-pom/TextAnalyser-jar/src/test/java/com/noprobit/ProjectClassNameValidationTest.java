package com.noprobit.analyzers;

import com.noprobit.analyzers.config.AnalysisConfig;
import com.noprobit.analyzers.db.FileDB;
import com.noprobit.analyzers.reporters.ClassNameAnalysisReporter;
import com.noprobit.analyzers.analyzers.ClassAnalysisEngine;
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
        AnalysisConfig config = new AnalysisConfig();
        String projectName = config.getProjectName();
        String sourceNodePath = config.getSourceNodePath();
        System.out.println("Using config - Project: " + projectName + ", Source: " + sourceNodePath);
        Path sourceDir = Paths.get(sourceNodePath);
        System.out.println("Analyzing directory: " + sourceDir.toAbsolutePath() + " (exists: " + Files.exists(sourceDir) + ")");
        List<ClassAnalysisEngine.AnalysisResult> violations = engine.analyzeProjectClasses(sourceDir);
        System.out.println("Found " + violations.size() + " violations");
        if (!violations.isEmpty()) {
            System.out.println("First violation: " + violations.get(0).fullName);
        }

        reporter.printReport(violations);

        exportAnalysisReports(projectName, sourceNodePath);
    }

    private void exportAnalysisReports(String projectName, String sourceNodePath) throws IOException {
        Path analysisDir = getAnalysisDirectory();
        System.out.println("Creating analysis directory: " + analysisDir.toAbsolutePath());
        Files.createDirectories(analysisDir);

        String dateStamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String reportPrefix = projectName.toLowerCase() + "-analysis-report";

        String latestCSV = analysisDir.resolve(reportPrefix + ".csv").toString();
        Path csvPath = Paths.get(latestCSV);
        System.out.println("Exporting CSV to: " + latestCSV);
        System.out.println("Absolute CSV path: " + csvPath.toAbsolutePath());
        try {
            db.exportToCSV(latestCSV);
            System.out.println("CSV export successful");
            System.out.println("CSV file exists: " + Files.exists(csvPath));
            System.out.println("CSV file size: " + Files.size(csvPath));
        } catch (Exception e) {
            System.out.println("CSV export failed: " + e.getMessage());
            e.printStackTrace();
        }

        String datedCSV = analysisDir.resolve(reportPrefix + "-" + dateStamp + ".csv").toString();
        db.exportToCSV(datedCSV);

        String latestMarkdown = analysisDir.resolve(reportPrefix + ".md").toString();
        System.out.println("Exporting Markdown to: " + latestMarkdown);
        db.exportToMarkdown(latestMarkdown, projectName, sourceNodePath);

        String datedMarkdown = analysisDir.resolve(reportPrefix + "-" + dateStamp + ".md").toString();
        db.exportToMarkdown(datedMarkdown, projectName, sourceNodePath);

        System.out.println("Export complete");
    }

    private Path getAnalysisDirectory() {
        return Paths.get("../../../analysis");
    }
}
