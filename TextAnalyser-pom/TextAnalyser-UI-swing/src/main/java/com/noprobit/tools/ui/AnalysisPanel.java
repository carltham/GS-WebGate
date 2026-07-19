package com.noprobit.tools.ui;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.util.function.Consumer;

public class AnalysisPanel extends JPanel {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton cancelButton;
    private Consumer<Void> cancelListener;

    public AnalysisPanel() {
        setLayout(new BorderLayout());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.SOUTH);

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> {
            if (cancelListener != null) {
                cancelListener.accept(null);
            }
        });
        add(cancelButton, BorderLayout.EAST);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void updateProgress(AnalysisProgressEvent event) {
        progressBar.setValue(event.getProgressPercentage());
        statusLabel.setText("Processing: " + event.getCurrentFileName() + " (" +
                event.getFilesProcessed() + "/" + event.getTotalFiles() + ")");
    }

    public void displayError(String error) {
        statusLabel.setText("Error: " + error);
        cancelButton.setEnabled(false);
    }

    public void resetProgress() {
        progressBar.setValue(0);
        statusLabel.setText("Ready");
        cancelButton.setEnabled(false);
    }

    public void setAnalysisRunning(boolean running) {
        cancelButton.setEnabled(running);
    }

    public void setCancelListener(Consumer<Void> listener) {
        this.cancelListener = listener;
    }
}
