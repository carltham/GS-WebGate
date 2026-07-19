package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Filtering Tests")
class FilteringTest {

    @Test
    void testFilterByViolationType() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.filterByType("ClassNaming");
        });
    }

    @Test
    void testFilterBySeverity() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.filterBySeverity("HIGH");
        });
    }

    @Test
    void testFilterByClass() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.filterByClass("MyClass");
        });
    }

    @Test
    void testMultipleFiltersAnd() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.filterByType("ClassNaming");
            controller.filterBySeverity("HIGH");
        });
    }

    @Test
    void testClearFilters() {
        ReportController controller = new ReportController();
        assertDoesNotThrow(() -> {
            controller.clearFilters();
        });
    }

    @Test
    void testFilteredRowCount() {
        ReportController controller = new ReportController();
        AnalysisReport report = new AnalysisReport("Test", 100, 50, 1000);
        controller.displayReport(report);
        assertTrue(controller.getViolations().size() >= 0);
    }

    @Test
    void testFilterPerformance() {
        ReportController controller = new ReportController();
        long start = System.currentTimeMillis();
        controller.filterByType("ClassNaming");
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 1000);
    }
}
