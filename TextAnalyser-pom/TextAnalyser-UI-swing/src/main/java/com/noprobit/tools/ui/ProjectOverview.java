package com.noprobit.tools.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.util.logging.Logger;

public class ProjectOverview {
    private static final Logger LOGGER = Logger.getLogger(ProjectOverview.class.getName());

    private JPanel panel;
    private JLabel projectNameLabel;
    private JLabel projectPathLabel;
    private JLabel lastAnalysisDateLabel;

    public ProjectOverview() {
        panel = new JPanel(new GridLayout(3, 2, 5, 5));

        panel.add(new JLabel("Project Name:"));
        projectNameLabel = new JLabel("N/A");
        panel.add(projectNameLabel);

        panel.add(new JLabel("Project Path:"));
        projectPathLabel = new JLabel("N/A");
        panel.add(projectPathLabel);

        panel.add(new JLabel("Last Analysis:"));
        lastAnalysisDateLabel = new JLabel("N/A");
        panel.add(lastAnalysisDateLabel);

        LOGGER.info("ProjectOverview initialized");
    }

    public void setProjectName(String name) {
        projectNameLabel.setText(name != null ? name : "N/A");
        LOGGER.fine("Project name set: " + name);
    }

    public void setProjectPath(String path) {
        projectPathLabel.setText(path != null ? path : "N/A");
        LOGGER.fine("Project path set: " + path);
    }

    public void setLastAnalysisDate(String date) {
        lastAnalysisDateLabel.setText(date != null ? date : "N/A");
        LOGGER.fine("Last analysis date set: " + date);
    }

    public void displayProjectInfo(String name, String path, String date) {
        setProjectName(name);
        setProjectPath(path);
        setLastAnalysisDate(date);
        LOGGER.info("Project info displayed: " + name);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void updateProjectInfo(String name, String path, String date) {
        displayProjectInfo(name, path, date);
        LOGGER.info("Project info updated");
    }

    public void clearProjectInfo() {
        projectNameLabel.setText("N/A");
        projectPathLabel.setText("N/A");
        lastAnalysisDateLabel.setText("N/A");
        LOGGER.info("Project info cleared");
    }
}
