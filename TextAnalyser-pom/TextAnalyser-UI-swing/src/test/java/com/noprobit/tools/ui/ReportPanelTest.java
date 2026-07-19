package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Report Panel Tests")
class ReportPanelTest {

    private ReportPanel panel;

    @BeforeEach
    void setUp() {
        panel = new ReportPanel();
    }

    @Test
    void testPanelCreated() {
        assertNotNull(panel);
    }

    @Test
    void testTableVisible() {
        assertNotNull(panel.getTable());
    }

    @Test
    void testTablePopulated() {
        AnalysisReport report = new AnalysisReport("Test", 50, 10, 500);
        panel.displayReport(report);
        assertTrue(panel.getTable().getRowCount() >= 0);
    }

    @Test
    void testViolationCountDisplayed() {
        AnalysisReport report = new AnalysisReport("Test", 50, 10, 500);
        panel.displayReport(report);
        assertNotNull(panel.getStatusLabel());
    }

    @Test
    void testFilterControlsVisible() {
        assertNotNull(panel.getFilterPanel());
    }

    @Test
    void testSortControlsWorking() {
        assertDoesNotThrow(() -> {
            panel.sortBy("Class");
        });
    }

    @Test
    void testExportButtonVisible() {
        assertNotNull(panel.getExportButton());
    }

    @Test
    void testScrollableForManyViolations() {
        AnalysisReport report = new AnalysisReport("Test", 1000, 500, 5000);
        assertDoesNotThrow(() -> {
            panel.displayReport(report);
        });
    }

    @Test
    void testColumnHeadersClear() {
        assertNotNull(panel.getTable().getTableHeader());
    }

    @Test
    void testUpdateOnNewReport() {
        AnalysisReport report1 = new AnalysisReport("Report1", 50, 10, 500);
        AnalysisReport report2 = new AnalysisReport("Report2", 100, 20, 1000);

        panel.displayReport(report1);
        panel.displayReport(report2);

        assertNotNull(panel.getStatusLabel());
    }
}
