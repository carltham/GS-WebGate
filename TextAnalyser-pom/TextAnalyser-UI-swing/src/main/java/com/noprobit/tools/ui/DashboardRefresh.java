package com.noprobit.tools.ui;

import java.util.logging.Logger;

public class DashboardRefresh {
    private static final Logger LOGGER = Logger.getLogger(DashboardRefresh.class.getName());

    private boolean autoRefreshEnabled = false;
    private long refreshInterval = 5000;
    private long lastRefreshTime = System.currentTimeMillis();

    public DashboardRefresh() {
        LOGGER.info("DashboardRefresh initialized");
    }

    public void manualRefresh() {
        this.lastRefreshTime = System.currentTimeMillis();
        LOGGER.info("Manual refresh performed at: " + lastRefreshTime);
    }

    public void enableAutoRefresh(long intervalMillis) {
        this.autoRefreshEnabled = true;
        this.refreshInterval = intervalMillis;
        LOGGER.info("Auto refresh enabled with interval: " + intervalMillis + "ms");
    }

    public void disableAutoRefresh() {
        this.autoRefreshEnabled = false;
        LOGGER.info("Auto refresh disabled");
    }

    public boolean isAutoRefreshEnabled() {
        return autoRefreshEnabled;
    }

    public void setRefreshInterval(long intervalMillis) {
        this.refreshInterval = intervalMillis;
        LOGGER.fine("Refresh interval set to: " + intervalMillis + "ms");
    }

    public String getLastRefreshTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new java.util.Date(lastRefreshTime));
    }
}
