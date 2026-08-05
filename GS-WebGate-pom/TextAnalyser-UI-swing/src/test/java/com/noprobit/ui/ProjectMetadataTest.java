package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project Metadata Tests")
class ProjectMetadataTest {

    @Test
    @DisplayName("ProjectMetadata can be created with name and path")
    void testMetadataCreation() {
        ProjectMetadata metadata = new ProjectMetadata("TestProject", "/test/path");
        assertNotNull(metadata);
    }

    @Test
    @DisplayName("Getter methods return correct values")
    void testGetters() {
        ProjectMetadata metadata = new ProjectMetadata("TestProject", "/test/path");
        assertEquals("TestProject", metadata.getProjectName());
        assertEquals("/test/path", metadata.getSourcePath());
    }

    @Test
    @DisplayName("Setter methods update values")
    void testSetters() {
        ProjectMetadata metadata = new ProjectMetadata("Original", "/original");
        metadata.setProjectName("Updated");
        metadata.setSourcePath("/updated");

        assertEquals("Updated", metadata.getProjectName());
        assertEquals("/updated", metadata.getSourcePath());
    }

    @Test
    @DisplayName("Report path can be set and retrieved")
    void testReportPathSetterGetter() {
        ProjectMetadata metadata = new ProjectMetadata("Project", "/path");
        metadata.setReportPath("/reports");

        assertEquals("/reports", metadata.getReportPath());
    }

    @Test
    @DisplayName("Metadata toString provides readable output")
    void testToString() {
        ProjectMetadata metadata = new ProjectMetadata("TestProject", "/test/path");
        String output = metadata.toString();

        assertNotNull(output);
        assertTrue(output.contains("TestProject"));
        assertTrue(output.contains("/test/path"));
    }
}
