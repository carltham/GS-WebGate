package com.noprobit.tools.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class AnalysisController {
    private static final Logger LOGGER = Logger.getLogger(AnalysisController.class.getName());

    private boolean analysisRunning = false;
    private AnalysisWorker worker;
    private List<Consumer<AnalysisProgressEvent>> progressListeners = new ArrayList<>();
    private List<Consumer<AnalysisReport>> completionListeners = new ArrayList<>();
    private List<Consumer<String>> errorListeners = new ArrayList<>();

    public void startAnalysis(ProjectMetadata project) {
        if (analysisRunning) {
            LOGGER.fine("Analysis already running, ignoring start request");
            return;
        }

        if (project == null) {
            fireErrorEvent("Invalid project");
            return;
        }

        analysisRunning = true;
        LOGGER.info("Starting analysis for project: " + project.getProjectName());

        worker = new AnalysisWorker(project);
        worker.addProgressListener(this::fireProgressEvent);
        worker.addCompletionListener(report -> {
            analysisRunning = false;
            fireCompletionEvent(report);
        });
        worker.execute();
    }

    public void cancelAnalysis() {
        if (worker != null && analysisRunning) {
            LOGGER.info("Cancelling analysis");
            worker.cancel(true);
            analysisRunning = false;
        }
    }

    public boolean isAnalysisRunning() {
        return analysisRunning;
    }

    public void addProgressListener(Consumer<AnalysisProgressEvent> listener) {
        progressListeners.add(listener);
    }

    public void addCompletionListener(Consumer<AnalysisReport> listener) {
        completionListeners.add(listener);
    }

    public void addErrorListener(Consumer<String> listener) {
        errorListeners.add(listener);
    }

    private void fireProgressEvent(AnalysisProgressEvent event) {
        for (Consumer<AnalysisProgressEvent> listener : progressListeners) {
            listener.accept(event);
        }
    }

    private void fireCompletionEvent(AnalysisReport report) {
        for (Consumer<AnalysisReport> listener : completionListeners) {
            listener.accept(report);
        }
    }

    private void fireErrorEvent(String error) {
        analysisRunning = false;
        for (Consumer<String> listener : errorListeners) {
            listener.accept(error);
        }
    }
}
