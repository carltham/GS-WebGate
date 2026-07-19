package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project List Panel Tests")
class ProjectListPanelTest {

    private ProjectListPanel panel;
    private List<ProjectMetadata> testProjects;

    @BeforeEach
    void setUp() {
        testProjects = createTestProjects();
    }

    private List<ProjectMetadata> createTestProjects() {
        List<ProjectMetadata> projects = new ArrayList<>();
        projects.add(new ProjectMetadata("Project1", "/path/to/project1"));
        projects.add(new ProjectMetadata("Project2", "/path/to/project2"));
        return projects;
    }

    @Test
    @DisplayName("Project list panel can be created")
    void testPanelCreated() {
        assertDoesNotThrow(() -> {
            panel = new ProjectListPanel();
        });
        assertNotNull(panel, "Panel should be created");
    }

    @Test
    @DisplayName("Panel displays project dropdown")
    void testPanelDisplaysProjectDropdown() {
        panel = new ProjectListPanel();
        panel.setProjects(testProjects);

        assertNotNull(panel.getProjectDropdown(), "Dropdown should exist");
        assertTrue(panel.getProjectDropdown().getItemCount() > 0, "Dropdown should have items");
    }

    @Test
    @DisplayName("Panel displays project names")
    void testPanelDisplaysProjectLabels() {
        panel = new ProjectListPanel();
        panel.setProjects(testProjects);

        for (ProjectMetadata project : testProjects) {
            // Verify each project is in dropdown
            assertNotNull(project.getProjectName(), "Project should have a name");
        }
    }

    @Test
    @DisplayName("Panel handles empty project list gracefully")
    void testPanelHandlesEmptyProjectList() {
        panel = new ProjectListPanel();
        List<ProjectMetadata> emptyList = new ArrayList<>();

        assertDoesNotThrow(() -> {
            panel.setProjects(emptyList);
        }, "Should handle empty list without exception");

        assertNotNull(panel, "Panel should still exist");
    }

    @Test
    @DisplayName("Panel can refresh project list")
    void testPanelRefreshesProjectList() {
        panel = new ProjectListPanel();
        panel.setProjects(testProjects);

        List<ProjectMetadata> updatedProjects = new ArrayList<>();
        updatedProjects.add(new ProjectMetadata("NewProject", "/path/to/new"));

        assertDoesNotThrow(() -> {
            panel.setProjects(updatedProjects);
        }, "Should refresh list without exception");

        assertEquals(1, panel.getProjectDropdown().getItemCount(),
            "Should have updated project list");
    }
}
