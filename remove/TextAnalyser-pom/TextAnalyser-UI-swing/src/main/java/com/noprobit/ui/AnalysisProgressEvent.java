package com.noprobit.analyzers.ui;

import java.util.EventObject;

public class AnalysisProgressEvent extends EventObject {
    private final int progressPercentage;
    private final String currentFileName;
    private final int totalFiles;
    private final int filesProcessed;

    public AnalysisProgressEvent(Object source, int progressPercentage, String currentFileName,
                                 int totalFiles, int filesProcessed) {
        super(source);
        this.progressPercentage = progressPercentage;
        this.currentFileName = currentFileName;
        this.totalFiles = totalFiles;
        this.filesProcessed = filesProcessed;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }

    @Override
    public String toString() {
        return "AnalysisProgressEvent{" +
                "progress=" + progressPercentage + "%" +
                ", file='" + currentFileName + '\'' +
                ", processed=" + filesProcessed + "/" + totalFiles +
                '}';
    }
}
