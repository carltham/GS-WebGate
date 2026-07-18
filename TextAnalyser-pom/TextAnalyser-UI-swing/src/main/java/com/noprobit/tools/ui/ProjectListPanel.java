package com.noprobit.tools.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.List;

public class ProjectListPanel extends JPanel {
    private JComboBox<String> projectDropdown;
    private List<ProjectMetadata> projects;

    public ProjectListPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        projectDropdown = new JComboBox<>();
        add(projectDropdown);
    }

    public void setProjects(List<ProjectMetadata> projects) {
        this.projects = projects;
        projectDropdown.removeAllItems();
        for (ProjectMetadata project : projects) {
            projectDropdown.addItem(project.getProjectName());
        }
    }

    public ProjectMetadata getSelectedProject() {
        if (projects != null && projects.size() > 0) {
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
}
