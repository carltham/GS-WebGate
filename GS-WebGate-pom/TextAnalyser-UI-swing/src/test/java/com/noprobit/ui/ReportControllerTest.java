package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Report Controller Tests")
class ReportControllerTest {

    private ReportController controller;
    private AnalysisReport testReport;

    @BeforeEach
    void setUp() {
        controller = new ReportController();
        testReport = new AnalysisReport("TestProject", 100, 15, 1000);
    }

    @Test
    void testControllerInitialization() {
        assertNotNull(controller);
    }

    @Test
    void testDisplayReport() {
        assertDoesNotThrow(() -> {
            controller.displayReport(testReport);
        });
    }

    @Test
    void testGetViolationList() {
        controller.displayReport(testReport);
        assertNotNull(controller.getViolations());
    }

    @Test
    void testFilterByType() {
        controller.displayReport(testReport);
        assertDoesNotThrow(() -> {
            controller.filterByType("ClassNaming");
        });
    }

    @Test
    void testFilterBySeverity() {
        controller.displayReport(testReport);
        assertDoesNotThrow(() -> {
            controller.filterBySeverity("HIGH");
        });
    }

    @Test
    void testSortByColumn() {
        controller.displayReport(testReport);
        assertDoesNotThrow(() -> {
            controller.sortByColumn("Class");
        });
    }

    @Test
    void testExportReport() {
        controller.displayReport(testReport);
        assertDoesNotThrow(() -> {
            controller.exportToCSV("/tmp/report.csv");
        });
    }

    @Test
    void testUpdateOnNewAnalysis() {
        controller.displayReport(testReport);
        AnalysisReport newReport = new AnalysisReport("NewProject", 50, 5, 500);
        assertDoesNotThrow(() -> {
            controller.displayReport(newReport);
        });
    }
}
