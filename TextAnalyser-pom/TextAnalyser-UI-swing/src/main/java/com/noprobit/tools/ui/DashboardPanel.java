package com.noprobit.tools.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.GridLayout;

public class DashboardPanel extends JPanel {
    private JLabel projectNameLabel;
    private JLabel totalFilesLabel;
    private JLabel totalViolationsLabel;
    private JLabel averageViolationsLabel;
    private JButton refreshButton;

    public DashboardPanel() {
        setLayout(new GridLayout(5, 2, 5, 5));

        add(new JLabel("Project:"));
        projectNameLabel = new JLabel("N/A");
        add(projectNameLabel);

        add(new JLabel("Total Files:"));
        totalFilesLabel = new JLabel("0");
        add(totalFilesLabel);

        add(new JLabel("Total Violations:"));
        totalViolationsLabel = new JLabel("0");
        add(totalViolationsLabel);

        add(new JLabel("Average Violations:"));
        averageViolationsLabel = new JLabel("0.0");
        add(averageViolationsLabel);

        refreshButton = new JButton("Refresh");
        add(refreshButton);
    }

    public JLabel getProjectNameLabel() {
        return projectNameLabel;
    }

    public JLabel getTotalFilesLabel() {
        return totalFilesLabel;
    }

    public JLabel getTotalViolationsLabel() {
        return totalViolationsLabel;
    }

    public JLabel getAverageViolationsLabel() {
        return averageViolationsLabel;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public void setProjectName(String name) {
        projectNameLabel.setText(name != null ? name : "N/A");
    }

    public void setTotalFiles(int count) {
        totalFilesLabel.setText(String.valueOf(count));
    }

    public void setTotalViolations(int count) {
        totalViolationsLabel.setText(String.valueOf(count));
    }

    public void setAverageViolations(double average) {
        averageViolationsLabel.setText(String.format("%.2f", average));
    }
}
