package com.noprobit.tools.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

public class MainWindow extends JFrame {
    private ProjectListPanel projectListPanel;
    private ConfigurationDisplayPanel configDisplayPanel;

    public MainWindow() {
        this(null, new java.util.ArrayList<>());
    }

    public MainWindow(ProjectMetadata currentConfig, List<ProjectMetadata> availableProjects) {
        setTitle("TextAnalyser - Code Analysis Tool");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create layout
        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        // Add project list panel
        projectListPanel = new ProjectListPanel();
        projectListPanel.setProjects(availableProjects);
        contentPane.add(projectListPanel, BorderLayout.NORTH);

        // Add configuration display panel
        configDisplayPanel = new ConfigurationDisplayPanel();
        if (currentConfig != null) {
            configDisplayPanel.displayConfiguration(currentConfig);
        }
        contentPane.add(configDisplayPanel, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }
}
