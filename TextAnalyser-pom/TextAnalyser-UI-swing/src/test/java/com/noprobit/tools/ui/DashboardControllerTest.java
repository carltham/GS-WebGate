package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dashboard Controller Tests")
class DashboardControllerTest {

    @Test
    void testControllerInitialization() {
        DashboardController controller = new DashboardController();
        assertNotNull(controller);
    }

    @Test
    void testLoadProjectStatistics() {
        DashboardController controller = new DashboardController();
        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("TestProject");
        });
    }

    @Test
    void testGetProjectName() {
        DashboardController controller = new DashboardController();
        controller.loadProjectStatistics("MyProject");
        assertEquals("MyProject", controller.getProjectName());
    }

    @Test
    void testGetTotalFilesAnalyzed() {
        DashboardController controller = new DashboardController();
        assertDoesNotThrow(() -> {
            int total = controller.getTotalFilesAnalyzed();
            assertTrue(total >= 0);
        });
    }

    @Test
    void testGetTotalViolations() {
        DashboardController controller = new DashboardController();
        assertDoesNotThrow(() -> {
            int violations = controller.getTotalViolations();
            assertTrue(violations >= 0);
        });
    }

    @Test
    void testGetAverageViolationsPerFile() {
        DashboardController controller = new DashboardController();
        assertDoesNotThrow(() -> {
            double average = controller.getAverageViolationsPerFile();
            assertTrue(average >= 0);
        });
    }

    @Test
    void testGetLastAnalysisTime() {
        DashboardController controller = new DashboardController();
        assertNotNull(controller.getLastAnalysisTime());
    }

    @Test
    void testRefreshDashboard() {
        DashboardController controller = new DashboardController();
        assertDoesNotThrow(() -> {
            controller.refreshDashboard();
        });
    }

    @Test
    void testGetViolationsByType() {
        DashboardController controller = new DashboardController();
        assertNotNull(controller.getViolationsByType());
    }
}
