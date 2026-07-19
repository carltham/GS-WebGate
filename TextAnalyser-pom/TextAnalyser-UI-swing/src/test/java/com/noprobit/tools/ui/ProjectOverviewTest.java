package com.noprobit.tools.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project Overview Tests")
class ProjectOverviewTest {

    @Test
    void testProjectOverviewInitialization() {
        ProjectOverview overview = new ProjectOverview();
        assertNotNull(overview);
    }

    @Test
    void testSetProjectName() {
        ProjectOverview overview = new ProjectOverview();
        assertDoesNotThrow(() -> {
            overview.setProjectName("TestProject");
        });
    }

    @Test
    void testSetProjectPath() {
        ProjectOverview overview = new ProjectOverview();
        assertDoesNotThrow(() -> {
            overview.setProjectPath("/home/user/projects/test");
        });
    }

    @Test
    void testSetLastAnalysisDate() {
        ProjectOverview overview = new ProjectOverview();
        assertDoesNotThrow(() -> {
            overview.setLastAnalysisDate("2026-07-19");
        });
    }

    @Test
    void testDisplayProjectInfo() {
        ProjectOverview overview = new ProjectOverview();
        assertDoesNotThrow(() -> {
            overview.displayProjectInfo("MyProject", "/path/to/project", "2026-07-19");
        });
    }

    @Test
    void testGetProjectInfoPanel() {
        ProjectOverview overview = new ProjectOverview();
        assertNotNull(overview.getPanel());
    }

    @Test
    void testUpdateProjectInfo() {
        ProjectOverview overview = new ProjectOverview();
        assertDoesNotThrow(() -> {
            overview.updateProjectInfo("UpdatedName", "/new/path", "2026-07-20");
        });
    }

    @Test
    void testClearProjectInfo() {
        ProjectOverview overview = new ProjectOverview();
        assertDoesNotThrow(() -> {
            overview.clearProjectInfo();
        });
    }
}
