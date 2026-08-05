package com.noprobit.analyzers.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProjectSelectionController {
    private static final Logger LOGGER = Logger.getLogger(ProjectSelectionController.class.getName());

    private ProjectMetadata currentProject;
    private List<ProjectMetadata> availableProjects;
    private List<ProjectSelectionListener> selectionListeners;
    private List<ProjectListUpdateListener> listUpdateListeners;

    public ProjectSelectionController() {
        this.availableProjects = new ArrayList<>();
        this.selectionListeners = new ArrayList<>();
        this.listUpdateListeners = new ArrayList<>();
        LOGGER.fine("ProjectSelectionController initialized");
    }

    public void selectProject(ProjectMetadata project) {
        if (project == null) {
            LOGGER.warning("Attempted to select null project");
            return;
        }

        this.currentProject = project;
        LOGGER.fine("Project selected: " + project.getProjectName());
        fireProjectSelectionEvent(project);
    }

    public ProjectMetadata getCurrentProject() {
        return currentProject;
    }

    public List<ProjectMetadata> refreshProjectList() {
        LOGGER.fine("Refreshing project list");
        fireProjectListUpdateEvent();
        return availableProjects;
    }

    public void addProjectSelectionListener(ProjectSelectionListener listener) {
        selectionListeners.add(listener);
    }

    public void addProjectListUpdateListener(ProjectListUpdateListener listener) {
        listUpdateListeners.add(listener);
    }

    private void fireProjectSelectionEvent(ProjectMetadata project) {
        ProjectSelectionEvent event = new ProjectSelectionEvent(this, project);
        for (ProjectSelectionListener listener : selectionListeners) {
            listener.onProjectSelected(event);
        }
    }

    private void fireProjectListUpdateEvent() {
        for (ProjectListUpdateListener listener : listUpdateListeners) {
            listener.onProjectListUpdated();
        }
    }

    public interface ProjectSelectionListener {
        void onProjectSelected(ProjectSelectionEvent event);
    }

    public interface ProjectListUpdateListener {
        void onProjectListUpdated();
    }
}
