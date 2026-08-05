package com.noprobit.analyzers.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProjectSelectionPanel extends JPanel {
    private JComboBox<String> projectDropdown;
    private List<ProjectMetadata> projects;
    private List<Consumer<ProjectMetadata>> selectionListeners;

    public ProjectSelectionPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        projectDropdown = new JComboBox<>();
        projects = new ArrayList<>();
        selectionListeners = new ArrayList<>();
        add(projectDropdown);
    }

    public void setProjects(List<ProjectMetadata> projects) {
        this.projects = new ArrayList<>(projects);
        projectDropdown.removeAllItems();
        for (ProjectMetadata project : projects) {
            projectDropdown.addItem(project.getProjectName());
        }
    }

    public void selectProject(ProjectMetadata project) {
        if (project != null && projects.contains(project)) {
            projectDropdown.setSelectedItem(project.getProjectName());
            fireSelectionEvent(project);
        }
    }

    public ProjectMetadata getSelectedProject() {
        if (projects.size() > 0) {
            int selectedIndex = projectDropdown.getSelectedIndex();
            if (selectedIndex >= 0) {
                return projects.get(selectedIndex);
            }
            return projects.get(0);
        }
        return null;
    }

    public JComboBox<String> getProjectDropdown() {
        return projectDropdown;
    }

    public void addSelectionListener(Consumer<ProjectMetadata> listener) {
        selectionListeners.add(listener);
    }

    private void fireSelectionEvent(ProjectMetadata project) {
        for (Consumer<ProjectMetadata> listener : selectionListeners) {
            listener.accept(project);
        }
    }
}
