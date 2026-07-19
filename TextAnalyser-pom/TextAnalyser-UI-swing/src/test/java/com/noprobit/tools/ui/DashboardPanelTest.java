package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dashboard Panel Tests")
class DashboardPanelTest {

    @Test
    void testPanelInitialization() {
        DashboardPanel panel = new DashboardPanel();
        assertNotNull(panel);
    }

    @Test
    void testProjectNameLabelExists() {
        DashboardPanel panel = new DashboardPanel();
        assertNotNull(panel.getProjectNameLabel());
    }

    @Test
    void testTotalFilesLabelExists() {
        DashboardPanel panel = new DashboardPanel();
        assertNotNull(panel.getTotalFilesLabel());
    }

    @Test
    void testTotalViolationsLabelExists() {
        DashboardPanel panel = new DashboardPanel();
        assertNotNull(panel.getTotalViolationsLabel());
    }

    @Test
    void testAverageViolationsLabelExists() {
        DashboardPanel panel = new DashboardPanel();
        assertNotNull(panel.getAverageViolationsLabel());
    }

    @Test
    void testSetProjectName() {
        DashboardPanel panel = new DashboardPanel();
        assertDoesNotThrow(() -> {
            panel.setProjectName("TestProject");
        });
    }

    @Test
    void testSetTotalFiles() {
        DashboardPanel panel = new DashboardPanel();
        assertDoesNotThrow(() -> {
            panel.setTotalFiles(100);
        });
    }

    @Test
    void testSetTotalViolations() {
        DashboardPanel panel = new DashboardPanel();
        assertDoesNotThrow(() -> {
            panel.setTotalViolations(50);
        });
    }

    @Test
    void testSetAverageViolations() {
        DashboardPanel panel = new DashboardPanel();
        assertDoesNotThrow(() -> {
            panel.setAverageViolations(0.5);
        });
    }

    @Test
    void testRefreshButtonExists() {
        DashboardPanel panel = new DashboardPanel();
        assertNotNull(panel.getRefreshButton());
    }
}
