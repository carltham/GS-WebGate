package com.noprobit.tools.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.GridLayout;

public class ConfigurationEditorPanel extends JPanel {
    private JTextField projectNameField;
    private JTextField sourcePathField;
    private JTextField reportPathField;
    private JButton saveButton;
    private JButton resetButton;

    public ConfigurationEditorPanel() {
        setLayout(new GridLayout(4, 2, 5, 5));

        add(new JLabel("Project Name:"));
        projectNameField = new JTextField();
        add(projectNameField);

        add(new JLabel("Source Path:"));
        sourcePathField = new JTextField();
        add(sourcePathField);

        add(new JLabel("Report Path:"));
        reportPathField = new JTextField();
        add(reportPathField);

        saveButton = new JButton("Save");
        add(saveButton);

        resetButton = new JButton("Reset");
        add(resetButton);
    }

    public JTextField getProjectNameField() {
        return projectNameField;
    }

    public JTextField getSourcePathField() {
        return sourcePathField;
    }

    public JTextField getReportPathField() {
        return reportPathField;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public void setProjectName(String name) {
        projectNameField.setText(name);
    }

    public void setSourcePath(String path) {
        sourcePathField.setText(path);
    }

    public void setReportPath(String path) {
        reportPathField.setText(path);
    }

    public String getProjectName() {
        return projectNameField.getText();
    }

    public String getSourcePath() {
        return sourcePathField.getText();
    }

    public String getReportPath() {
        return reportPathField.getText();
    }
}
