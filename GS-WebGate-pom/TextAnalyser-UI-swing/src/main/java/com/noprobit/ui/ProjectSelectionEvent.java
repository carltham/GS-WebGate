package com.noprobit.analyzers.ui;

import java.util.EventObject;

public class ProjectSelectionEvent extends EventObject {
    private final ProjectMetadata selectedProject;

    public ProjectSelectionEvent(Object source, ProjectMetadata selectedProject) {
        super(source);
        this.selectedProject = selectedProject;
    }

    public ProjectMetadata getSelectedProject() {
        return selectedProject;
    }
}
