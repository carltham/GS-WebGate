package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project Refresh Tests")
class ProjectRefreshTest {

    private ProjectSelectionController controller;

    @BeforeEach
    void setUp() {
        controller = new ProjectSelectionController();
    }

    @Test
    @DisplayName("Refresh loads projects from filesystem")
    void testRefreshLoadsProjectsFromFilesystem() {
        assertDoesNotThrow(() -> {
            controller.refreshProjectList();
        });
    }

    @Test
    @DisplayName("Refresh returns updated project list")
    void testRefreshReturnsUpdatedList() {
        assertDoesNotThrow(() -> {
            List<ProjectMetadata> projects = controller.refreshProjectList();
            assertNotNull(projects);
        });
    }

    @Test
    @DisplayName("Refresh fires update event to listeners")
    void testRefreshFiresUpdateEvent() {
        boolean[] eventFired = {false};

        controller.addProjectListUpdateListener(() -> {
            eventFired[0] = true;
        });

        controller.refreshProjectList();

        assertTrue(eventFired[0]);
    }

    @Test
    @DisplayName("Multiple refresh calls work correctly")
    void testMultipleRefreshes() {
        assertDoesNotThrow(() -> {
            controller.refreshProjectList();
            controller.refreshProjectList();
            controller.refreshProjectList();
        });
    }

    @Test
    @DisplayName("Refresh maintains current project selection if valid")
    void testRefreshMaintainsSelection() {
        ProjectMetadata project = new ProjectMetadata("TestProject", "/path");
        controller.selectProject(project);

        assertDoesNotThrow(() -> {
            controller.refreshProjectList();
        });

        assertEquals("TestProject", controller.getCurrentProject().getProjectName());
    }
}
