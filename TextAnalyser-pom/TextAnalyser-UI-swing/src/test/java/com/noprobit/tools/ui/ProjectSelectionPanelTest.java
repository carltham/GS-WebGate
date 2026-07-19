package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project Selection Panel Tests")
class ProjectSelectionPanelTest {

    private ProjectSelectionPanel panel;
    private List<ProjectMetadata> testProjects;

    @BeforeEach
    void setUp() {
        testProjects = new ArrayList<>();
        testProjects.add(new ProjectMetadata("TextAnalyser", "/path1"));
        testProjects.add(new ProjectMetadata("GSPos", "/path2"));
    }

    @Test
    @DisplayName("Selection panel can be created")
    void testPanelCreated() {
        assertDoesNotThrow(() -> {
            panel = new ProjectSelectionPanel();
        });
        assertNotNull(panel);
    }

    @Test
    @DisplayName("Panel displays project dropdown")
    void testPanelDisplaysProjectDropdown() {
        panel = new ProjectSelectionPanel();
        panel.setProjects(testProjects);
        assertNotNull(panel.getProjectDropdown());
        assertTrue(panel.getProjectDropdown().getItemCount() > 0);
    }

    @Test
    @DisplayName("User can select project from panel")
    void testUserCanSelectProject() {
        panel = new ProjectSelectionPanel();
        panel.setProjects(testProjects);

        assertDoesNotThrow(() -> {
            panel.selectProject(testProjects.get(0));
        });
    }

    @Test
    @DisplayName("Selection listener is invoked on project change")
    void testSelectionListenerInvoked() {
        panel = new ProjectSelectionPanel();
        panel.setProjects(testProjects);

        ProjectMetadata[] selectedProject = new ProjectMetadata[1];
        panel.addSelectionListener(project -> {
            selectedProject[0] = project;
        });

        panel.selectProject(testProjects.get(0));

        assertNotNull(selectedProject[0]);
        assertEquals("TextAnalyser", selectedProject[0].getProjectName());
    }

    @Test
    @DisplayName("Panel updates project list when refresh called")
    void testPanelRefreshesProjectList() {
        panel = new ProjectSelectionPanel();
        List<ProjectMetadata> newProjects = new ArrayList<>();
        newProjects.add(new ProjectMetadata("NewProject", "/new/path"));

        assertDoesNotThrow(() -> {
            panel.setProjects(newProjects);
        });

        assertEquals(1, panel.getProjectDropdown().getItemCount());
    }

    @Test
    @DisplayName("Panel handles empty project list")
    void testPanelHandlesEmptyList() {
        panel = new ProjectSelectionPanel();
        List<ProjectMetadata> emptyList = new ArrayList<>();

        assertDoesNotThrow(() -> {
            panel.setProjects(emptyList);
        });

        assertNotNull(panel);
    }
}
