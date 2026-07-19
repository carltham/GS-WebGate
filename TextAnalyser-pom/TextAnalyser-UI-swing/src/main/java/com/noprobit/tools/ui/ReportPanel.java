package com.noprobit.tools.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class ReportPanel extends JPanel {
    private ViolationTable table;
    private JLabel statusLabel;
    private JButton exportButton;
    private JPanel filterPanel;

    public ReportPanel() {
        setLayout(new BorderLayout());

        table = new ViolationTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.SOUTH);

        filterPanel = new JPanel();
        add(filterPanel, BorderLayout.NORTH);

        exportButton = new JButton("Export");
        add(exportButton, BorderLayout.EAST);
    }

    public void displayReport(AnalysisReport report) {
        statusLabel.setText("Loaded: " + report.getProjectName());
    }

    public void sortBy(String column) {
        // Sorting logic
    }

    public JTable getTable() {
        return table;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JButton getExportButton() {
        return exportButton;
    }

    public JPanel getFilterPanel() {
        return filterPanel;
    }
}
