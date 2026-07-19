package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Report Viewing Tests")
class IntegrationReportViewingTest {

    @Test
    void testCompleteWorkflow() {
        ReportController controller = new ReportController();
        ReportPanel panel = new ReportPanel();

        AnalysisReport report = new AnalysisReport("TestProject", 100, 50, 1000);
        assertDoesNotThrow(() -> {
            controller.displayReport(report);
            panel.displayReport(report);
        });
    }

    @Test
    void testFilterAndExport() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);
        controller.displayReport(report);

        assertDoesNotThrow(() -> {
            controller.filterByType("ClassNaming");
            controller.exportToCSV("/tmp/filtered.csv");
        });
    }

    @Test
    void testMultipleReports() {
        ReportController controller = new ReportController();
        ReportPanel panel = new ReportPanel();

        AnalysisReport report1 = new AnalysisReport("Project1", 50, 10, 500);
        AnalysisReport report2 = new AnalysisReport("Project2", 100, 30, 1000);

        controller.displayReport(report1);
        panel.displayReport(report1);

        controller.displayReport(report2);
        panel.displayReport(report2);

        assertNotNull(controller.getViolations());
    }

    @Test
    void testLargeReportHandling() {
        ReportController controller = new ReportController();
        ReportPanel panel = new ReportPanel();

        AnalysisReport largeReport = new AnalysisReport("LargeProject", 10000, 5000, 50000);

        assertDoesNotThrow(() -> {
            controller.displayReport(largeReport);
            panel.displayReport(largeReport);
            controller.sortByColumn("Class");
            controller.filterBySeverity("HIGH");
        });
    }

    @Test
    void testReportRefresh() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);

        controller.displayReport(report);
        controller.filterByType("ClassNaming");

        AnalysisReport newReport = new AnalysisReport("Test", 200, 100, 2000);
        controller.displayReport(newReport);

        assertNotNull(controller.getViolations());
    }
}
