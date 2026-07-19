package com.noprobit.analyzers.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;

public class ConfigurationDisplayPanel extends JPanel {
    private JLabel nameLabel;
    private JLabel sourcePathLabel;
    private JLabel reportPathLabel;
    private String displayedConfiguration;

    public ConfigurationDisplayPanel() {
        setLayout(new GridLayout(3, 1));

        nameLabel = new JLabel("Project: ");
        sourcePathLabel = new JLabel("Source: ");
        reportPathLabel = new JLabel("Reports: ");

        add(nameLabel);
        add(sourcePathLabel);
        add(reportPathLabel);
    }

    public void displayConfiguration(ProjectMetadata config) {
        if (config != null) {
            nameLabel.setText("Project: " + config.getProjectName());
            sourcePathLabel.setText("Source: " + config.getSourcePath());
            String reportPath = config.getReportPath() != null ?
                config.getReportPath() : "Not configured";
            reportPathLabel.setText("Reports: " + reportPath);

            StringBuilder sb = new StringBuilder();
            sb.append("Project: ").append(config.getProjectName()).append("\n");
            sb.append("Source: ").append(config.getSourcePath()).append("\n");
            sb.append("Reports: ").append(reportPath);
            displayedConfiguration = sb.toString();
        }
    }

    public String getDisplayedConfiguration() {
        return displayedConfiguration != null ? displayedConfiguration : "";
    }
}
