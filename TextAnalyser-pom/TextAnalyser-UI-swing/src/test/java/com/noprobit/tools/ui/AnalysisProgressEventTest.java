package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Analysis Progress Event Tests")
class AnalysisProgressEventTest {

    @Test
    @DisplayName("Progress event can be created")
    void testEventCreation() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 50, "test.java", 100, 50);
        assertNotNull(event);
    }

    @Test
    @DisplayName("Progress percentage is correct")
    void testProgressPercentage() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 75, "file.java", 100, 75);
        assertEquals(75, event.getProgressPercentage());
    }

    @Test
    @DisplayName("Current file name is returned")
    void testCurrentFileName() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 50, "MyClass.java", 100, 50);
        assertEquals("MyClass.java", event.getCurrentFileName());
    }

    @Test
    @DisplayName("Total files count is correct")
    void testTotalFilesCount() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 30, "file.java", 200, 60);
        assertEquals(200, event.getTotalFiles());
    }

    @Test
    @DisplayName("Files processed count is correct")
    void testFilesProcessedCount() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 40, "file.java", 100, 40);
        assertEquals(40, event.getFilesProcessed());
    }

    @Test
    @DisplayName("Progress values are within valid range")
    void testProgressValuesInRange() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 50, "file.java", 100, 50);

        assertTrue(event.getProgressPercentage() >= 0);
        assertTrue(event.getProgressPercentage() <= 100);
        assertTrue(event.getFilesProcessed() <= event.getTotalFiles());
    }

    @Test
    @DisplayName("Event toString provides readable output")
    void testEventToString() {
        AnalysisProgressEvent event = new AnalysisProgressEvent(this, 50, "test.java", 100, 50);
        String output = event.toString();

        assertNotNull(output);
        assertTrue(output.length() > 0);
    }
}
