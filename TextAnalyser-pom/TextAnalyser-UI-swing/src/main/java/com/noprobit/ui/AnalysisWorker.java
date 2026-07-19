package com.noprobit.analyzers.ui;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class AnalysisWorker extends SwingWorker<AnalysisReport, AnalysisProgressEvent> {
    private static final Logger LOGGER = Logger.getLogger(AnalysisWorker.class.getName());

    private final ProjectMetadata project;
    private final List<Consumer<AnalysisProgressEvent>> progressListeners = new ArrayList<>();
    private final List<Consumer<AnalysisReport>> completionListeners = new ArrayList<>();

    public AnalysisWorker(ProjectMetadata project) {
        this.project = project;
    }

    @Override
    protected AnalysisReport doInBackground() throws Exception {
        LOGGER.info("Analysis worker starting for project: " + project.getProjectName());

        long startTime = System.currentTimeMillis();
        int totalFiles = 100;

        for (int i = 0; i <= totalFiles; i++) {
            if (isCancelled()) {
                LOGGER.info("Analysis cancelled");
                return null;
            }

            int progress = (i * 100) / totalFiles;
            String fileName = "File" + i + ".java";

            AnalysisProgressEvent event = new AnalysisProgressEvent(this, progress, fileName, totalFiles, i);
            publish(event);

            Thread.sleep(10); // Simulate work
        }

        long endTime = System.currentTimeMillis();
        long analysisTime = endTime - startTime;

        AnalysisReport report = new AnalysisReport(
                project.getProjectName(),
                totalFiles,
                totalFiles / 10,
                analysisTime
        );

        LOGGER.info("Analysis complete: " + report);
        return report;
    }

    @Override
    protected void process(List<AnalysisProgressEvent> chunks) {
        for (AnalysisProgressEvent event : chunks) {
            for (Consumer<AnalysisProgressEvent> listener : progressListeners) {
                listener.accept(event);
            }
        }
    }

    @Override
    protected void done() {
        try {
            AnalysisReport report = get();
            if (report != null) {
                for (Consumer<AnalysisReport> listener : completionListeners) {
                    listener.accept(report);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Analysis error: " + e.getMessage());
        }
    }

    public void addProgressListener(Consumer<AnalysisProgressEvent> listener) {
        progressListeners.add(listener);
    }

    public void addCompletionListener(Consumer<AnalysisReport> listener) {
        completionListeners.add(listener);
    }
}
