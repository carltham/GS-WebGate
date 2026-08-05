package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dashboard Refresh Tests")
class DashboardRefreshTest {

    @Test
    void testRefreshInitialization() {
        DashboardRefresh refresh = new DashboardRefresh();
        assertNotNull(refresh);
    }

    @Test
    void testManualRefresh() {
        DashboardRefresh refresh = new DashboardRefresh();
        assertDoesNotThrow(() -> {
            refresh.manualRefresh();
        });
    }

    @Test
    void testAutoRefreshEnable() {
        DashboardRefresh refresh = new DashboardRefresh();
        assertDoesNotThrow(() -> {
            refresh.enableAutoRefresh(5000);
        });
    }

    @Test
    void testAutoRefreshDisable() {
        DashboardRefresh refresh = new DashboardRefresh();
        assertDoesNotThrow(() -> {
            refresh.disableAutoRefresh();
        });
    }

    @Test
    void testIsAutoRefreshEnabled() {
        DashboardRefresh refresh = new DashboardRefresh();
        assertFalse(refresh.isAutoRefreshEnabled());
    }

    @Test
    void testSetRefreshInterval() {
        DashboardRefresh refresh = new DashboardRefresh();
        assertDoesNotThrow(() -> {
            refresh.setRefreshInterval(10000);
        });
    }

    @Test
    void testGetLastRefreshTime() {
        DashboardRefresh refresh = new DashboardRefresh();
        assertNotNull(refresh.getLastRefreshTime());
    }
}
