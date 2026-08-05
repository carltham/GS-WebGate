package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Statistics Display Tests")
class StatisticsDisplayTest {

    @Test
    void testStatisticsDisplayInitialization() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertNotNull(display);
    }

    @Test
    void testDisplayTotalFiles() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertDoesNotThrow(() -> {
            display.displayTotalFiles(100);
        });
    }

    @Test
    void testDisplayTotalViolations() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertDoesNotThrow(() -> {
            display.displayTotalViolations(50);
        });
    }

    @Test
    void testDisplayViolationPercentage() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertDoesNotThrow(() -> {
            display.displayViolationPercentage(50.0);
        });
    }

    @Test
    void testDisplayAnalysisTime() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertDoesNotThrow(() -> {
            display.displayAnalysisTime(1500);
        });
    }

    @Test
    void testGetStatisticsPanel() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertNotNull(display.getPanel());
    }

    @Test
    void testClearStatistics() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertDoesNotThrow(() -> {
            display.clearStatistics();
        });
    }

    @Test
    void testUpdateStatistics() {
        StatisticsDisplay display = new StatisticsDisplay();
        assertDoesNotThrow(() -> {
            display.updateStatistics(100, 50, 1500);
        });
    }
}
