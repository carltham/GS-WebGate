package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Project Switching Tests")
class IntegrationProjectSwitchingTest {

    private ProjectSelectionController controller;
    private ProjectSelectionPanel panel;
    private List<ProjectMetadata> projects;

    @BeforeEach
    void setUp() {
        controller = new ProjectSelectionController();
        panel = new ProjectSelectionPanel();
        projects = new ArrayList<>();
        projects.add(new ProjectMetadata("TextAnalyser", "/path/ta"));
        projects.add(new ProjectMetadata("GSPos", "/path/gs"));
    }

    @Test
    @DisplayName("Complete project switch workflow")
    void testCompleteProjectSwitchWorkflow() {
        panel.setProjects(projects);

        assertDoesNotThrow(() -> {
            panel.selectProject(projects.get(0));
            controller.selectProject(projects.get(0));
        });

        assertEquals("TextAnalyser", controller.getCurrentProject().getProjectName());
    }

    @Test
    @DisplayName("Switch between multiple projects")
    void testSwitchBetweenMultipleProjects() {
        controller.selectProject(projects.get(0));
        assertEquals("TextAnalyser", controller.getCurrentProject().getProjectName());

        controller.selectProject(projects.get(1));
        assertEquals("GSPos", controller.getCurrentProject().getProjectName());

        controller.selectProject(projects.get(0));
        assertEquals("TextAnalyser", controller.getCurrentProject().getProjectName());
    }

    @Test
    @DisplayName("Configuration updates when project changes")
    void testConfigurationUpdatesOnProjectChange() {
        ProjectMetadata project1 = new ProjectMetadata("Project1", "/path1");
        ProjectMetadata project2 = new ProjectMetadata("Project2", "/path2");

        controller.selectProject(project1);
        assertEquals("/path1", controller.getCurrentProject().getSourcePath());

        controller.selectProject(project2);
        assertEquals("/path2", controller.getCurrentProject().getSourcePath());
    }

    @Test
    @DisplayName("Project list refresh updates panel")
    void testProjectListRefreshUpdatesPanel() {
        panel.setProjects(projects);

        List<ProjectMetadata> newProjects = new ArrayList<>();
        newProjects.add(new ProjectMetadata("NewProject", "/new"));

        assertDoesNotThrow(() -> {
            panel.setProjects(newProjects);
            controller.refreshProjectList();
        });
    }

    @Test
    @DisplayName("Events fire during project switching")
    void testEventsFiringSwitching() {
        boolean[] eventFired = {false};

        controller.addProjectSelectionListener(event -> {
            eventFired[0] = true;
        });

        controller.selectProject(projects.get(0));

        assertTrue(eventFired[0]);
    }

    @Test
    @DisplayName("No exceptions during normal project switching")
    void testNoExceptionsDuringSwitching() {
        assertDoesNotThrow(() -> {
            controller.selectProject(projects.get(0));
            panel.selectProject(projects.get(0));
            controller.selectProject(projects.get(1));
            controller.refreshProjectList();
        });
    }
}
