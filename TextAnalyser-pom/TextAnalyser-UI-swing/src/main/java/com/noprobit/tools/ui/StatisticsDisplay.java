package com.noprobit.tools.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.util.logging.Logger;

public class StatisticsDisplay {
    private static final Logger LOGGER = Logger.getLogger(StatisticsDisplay.class.getName());

    private JPanel panel;
    private JLabel totalFilesLabel;
    private JLabel totalViolationsLabel;
    private JLabel violationPercentageLabel;
    private JLabel analysisTimeLabel;

    public StatisticsDisplay() {
        panel = new JPanel(new GridLayout(4, 2, 5, 5));

        panel.add(new JLabel("Total Files:"));
        totalFilesLabel = new JLabel("0");
        panel.add(totalFilesLabel);

        panel.add(new JLabel("Total Violations:"));
        totalViolationsLabel = new JLabel("0");
        panel.add(totalViolationsLabel);

        panel.add(new JLabel("Violation %:"));
        violationPercentageLabel = new JLabel("0.0%");
        panel.add(violationPercentageLabel);

        panel.add(new JLabel("Analysis Time:"));
        analysisTimeLabel = new JLabel("0ms");
        panel.add(analysisTimeLabel);

        LOGGER.info("StatisticsDisplay initialized");
    }

    public void displayTotalFiles(int count) {
        totalFilesLabel.setText(String.valueOf(count));
        LOGGER.fine("Total files displayed: " + count);
    }

    public void displayTotalViolations(int count) {
        totalViolationsLabel.setText(String.valueOf(count));
        LOGGER.fine("Total violations displayed: " + count);
    }

    public void displayViolationPercentage(double percentage) {
        violationPercentageLabel.setText(String.format("%.1f%%", percentage));
        LOGGER.fine("Violation percentage displayed: " + percentage);
    }

    public void displayAnalysisTime(long milliseconds) {
        analysisTimeLabel.setText(milliseconds + "ms");
        LOGGER.fine("Analysis time displayed: " + milliseconds);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void clearStatistics() {
        totalFilesLabel.setText("0");
        totalViolationsLabel.setText("0");
        violationPercentageLabel.setText("0.0%");
        analysisTimeLabel.setText("0ms");
        LOGGER.info("Statistics cleared");
    }

    public void updateStatistics(int totalFiles, int totalViolations, long analysisTime) {
        displayTotalFiles(totalFiles);
        displayTotalViolations(totalViolations);
        double percentage = totalFiles > 0 ? (100.0 * totalViolations / totalFiles) : 0.0;
        displayViolationPercentage(percentage);
        displayAnalysisTime(analysisTime);
        LOGGER.info("Statistics updated");
    }
}
