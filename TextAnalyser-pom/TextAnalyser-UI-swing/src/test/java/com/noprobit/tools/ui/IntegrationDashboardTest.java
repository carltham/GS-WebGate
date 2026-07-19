package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Dashboard Tests")
class IntegrationDashboardTest {

    @Test
    void testCompleteDashboardWorkflow() {
        DashboardController controller = new DashboardController();
        DashboardPanel panel = new DashboardPanel();

        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("TestProject");
            panel.setProjectName(controller.getProjectName());
            panel.setTotalFiles(controller.getTotalFilesAnalyzed());
            panel.setTotalViolations(controller.getTotalViolations());
        });
    }

    @Test
    void testDashboardRefreshWorkflow() {
        DashboardController controller = new DashboardController();
        DashboardRefresh refresh = new DashboardRefresh();

        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("TestProject");
            refresh.manualRefresh();
            controller.refreshDashboard();
        });
    }

    @Test
    void testMultipleProjectsDisplay() {
        DashboardController controller1 = new DashboardController();
        DashboardController controller2 = new DashboardController();

        assertDoesNotThrow(() -> {
            controller1.loadProjectStatistics("Project1");
            controller2.loadProjectStatistics("Project2");

            assertNotEquals(controller1.getProjectName(), controller2.getProjectName());
        });
    }

    @Test
    void testDashboardStatisticsUpdate() {
        DashboardController controller = new DashboardController();
        StatisticsDisplay display = new StatisticsDisplay();

        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("TestProject");
            display.updateStatistics(
                controller.getTotalFilesAnalyzed(),
                controller.getTotalViolations(),
                1000
            );
        });
    }

    @Test
    void testDashboardWithProjectOverview() {
        DashboardController controller = new DashboardController();
        ProjectOverview overview = new ProjectOverview();

        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("TestProject");
            overview.displayProjectInfo(
                controller.getProjectName(),
                "/path/to/project",
                "2026-07-19"
            );
        });
    }

    @Test
    void testAutoRefreshDashboard() {
        DashboardController controller = new DashboardController();
        DashboardRefresh refresh = new DashboardRefresh();

        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("TestProject");
            refresh.enableAutoRefresh(5000);
            assertTrue(refresh.isAutoRefreshEnabled());
            refresh.disableAutoRefresh();
            assertFalse(refresh.isAutoRefreshEnabled());
        });
    }

    @Test
    void testDashboardViolationsByType() {
        DashboardController controller = new DashboardController();
        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("TestProject");
            assertNotNull(controller.getViolationsByType());
        });
    }

    @Test
    void testCompleteDashboardInitialization() {
        DashboardController controller = new DashboardController();
        DashboardPanel panel = new DashboardPanel();
        StatisticsDisplay display = new StatisticsDisplay();
        ProjectOverview overview = new ProjectOverview();
        DashboardRefresh refresh = new DashboardRefresh();

        assertDoesNotThrow(() -> {
            controller.loadProjectStatistics("CompleteTest");
            panel.setProjectName(controller.getProjectName());
            display.updateStatistics(
                controller.getTotalFilesAnalyzed(),
                controller.getTotalViolations(),
                1000
            );
            overview.displayProjectInfo(
                controller.getProjectName(),
                "/path",
                "2026-07-19"
            );
            refresh.manualRefresh();
        });
    }
}
