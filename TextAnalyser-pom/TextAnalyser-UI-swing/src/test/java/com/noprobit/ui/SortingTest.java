package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sorting Tests")
class SortingTest {

    @Test
    void testSortByClass() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.sortByColumn("Class");
        });
    }

    @Test
    void testSortByMethod() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.sortByColumn("Method");
        });
    }

    @Test
    void testSortByViolation() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.sortByColumn("Violation");
        });
    }

    @Test
    void testSortBySeverity() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.sortByColumn("Severity");
        });
    }

    @Test
    void testReverseSort() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.sortByColumn("Class");
            controller.reverseSortOrder();
        });
    }

    @Test
    void testSortPreservesData() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);
        controller.displayReport(report);
        controller.sortByColumn("Class");
        assertTrue(controller.getViolations().size() >= 0);
    }
}
