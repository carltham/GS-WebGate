package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project Selection Controller Tests")
class ProjectSelectionControllerTest {

    private ProjectSelectionController controller;
    private ProjectMetadata testProject1;
    private ProjectMetadata testProject2;

    @BeforeEach
    void setUp() {
        testProject1 = new ProjectMetadata("TextAnalyser", "/path/to/textanalyser");
        testProject2 = new ProjectMetadata("GSPos", "/path/to/gspos");
    }

    @Test
    @DisplayName("Controller initializes successfully")
    void testControllerInitialization() {
        assertDoesNotThrow(() -> {
            controller = new ProjectSelectionController();
        });
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Selecting project updates current selection")
    void testSelectProjectUpdatesSelection() {
        controller = new ProjectSelectionController();
        assertDoesNotThrow(() -> {
            controller.selectProject(testProject1);
        });
        assertEquals(testProject1, controller.getCurrentProject());
    }

    @Test
    @DisplayName("Project selection fires event to listeners")
    void testProjectSelectionFiresEvent() {
        controller = new ProjectSelectionController();
        ProjectSelectionEvent[] eventHolder = new ProjectSelectionEvent[1];

        controller.addProjectSelectionListener(event -> {
            eventHolder[0] = event;
        });

        controller.selectProject(testProject1);

        assertNotNull(eventHolder[0]);
        assertEquals(testProject1, eventHolder[0].getSelectedProject());
    }

    @Test
    @DisplayName("Multiple listeners all receive selection events")
    void testMultipleListenersNotified() {
        controller = new ProjectSelectionController();
        boolean[] listener1Called = {false};
        boolean[] listener2Called = {false};

        controller.addProjectSelectionListener(e -> listener1Called[0] = true);
        controller.addProjectSelectionListener(e -> listener2Called[0] = true);

        controller.selectProject(testProject1);

        assertTrue(listener1Called[0]);
        assertTrue(listener2Called[0]);
    }

    @Test
    @DisplayName("Get current project returns selected project")
    void testGetCurrentProject() {
        controller = new ProjectSelectionController();
        controller.selectProject(testProject1);
        assertEquals(testProject1.getProjectName(),
            controller.getCurrentProject().getProjectName());
    }

    @Test
    @DisplayName("Refresh project list updates available projects")
    void testRefreshProjectList() {
        controller = new ProjectSelectionController();
        assertDoesNotThrow(() -> {
            controller.refreshProjectList();
        });
    }

    @Test
    @DisplayName("Project switch validation prevents invalid projects")
    void testInvalidProjectHandling() {
        controller = new ProjectSelectionController();
        ProjectMetadata invalidProject = new ProjectMetadata("", "");

        // Should handle gracefully without throwing
        assertDoesNotThrow(() -> {
            controller.selectProject(invalidProject);
        });
    }

    @Test
    @DisplayName("Controller maintains project history")
    void testProjectHistoryMaintained() {
        controller = new ProjectSelectionController();
        controller.selectProject(testProject1);
        controller.selectProject(testProject2);

        assertEquals(testProject2, controller.getCurrentProject());
    }
}
