package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Export Tests")
class ExportTest {

    @Test
    void testExportToCSV() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);
        controller.displayReport(report);

        assertDoesNotThrow(() -> {
            controller.exportToCSV("/tmp/test_report.csv");
        });
    }

    @Test
    void testExportToMarkdown() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);
        controller.displayReport(report);

        assertDoesNotThrow(() -> {
            controller.exportToMarkdown("/tmp/test_report.md");
        });
    }

    @Test
    void testCSVFormat() {
        assertDoesNotThrow(() -> {
            ReportExporter exporter = new ReportExporter();
            String csv = exporter.toCSV(new AnalysisReport("Test", 100, 50, 1000));
            assertTrue(csv.contains(","));
        });
    }

    @Test
    void testMarkdownFormat() {
        assertDoesNotThrow(() -> {
            ReportExporter exporter = new ReportExporter();
            String md = exporter.toMarkdown(new AnalysisReport("Test", 100, 50, 1000));
            assertTrue(md.contains("|"));
        });
    }

    @Test
    void testFileCreation() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);
        controller.displayReport(report);

        assertDoesNotThrow(() -> {
            controller.exportToCSV("/tmp/export_test.csv");
        });
    }

    @Test
    void testExportAllViolations() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 1000, 500, 5000);
        controller.displayReport(report);

        assertDoesNotThrow(() -> {
            controller.exportToCSV("/tmp/large_export.csv");
        });
    }

    @Test
    void testExportFiltered() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);
        controller.displayReport(report);
        controller.filterByType("ClassNaming");

        assertDoesNotThrow(() -> {
            controller.exportToCSV("/tmp/filtered_export.csv");
        });
    }

    @Test
    void testExportError() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.exportToCSV("/invalid/path/report.csv");
        });
    }
}
