package com.noprobit.analyzers.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.util.List;

public class MainWindow extends JFrame {
    private ProjectListPanel projectListPanel;
    private ConfigurationDisplayPanel configDisplayPanel;
    private AnalysisPanel analysisPanel;
    private ReportPanel reportPanel;
    private ConfigurationEditorPanel configEditorPanel;
    private DashboardPanel dashboardPanel;
    private JTabbedPane tabbedPane;

    public MainWindow() {
        this(null, new java.util.ArrayList<>());
    }

    public MainWindow(ProjectMetadata currentConfig, List<ProjectMetadata> availableProjects) {
        applyModernStyling();

        setTitle("TextAnalyser - Code Analysis Tool");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        // Phase 0 & 1: Project Selection
        JPanel topPanel = new JPanel(new BorderLayout());
        projectListPanel = new ProjectListPanel();
        projectListPanel.setProjects(availableProjects);
        topPanel.add(projectListPanel, BorderLayout.NORTH);
        contentPane.add(topPanel, BorderLayout.NORTH);

        // Phases 2-5: Tabbed Interface
        tabbedPane = new JTabbedPane();

        configDisplayPanel = new ConfigurationDisplayPanel();
        if (currentConfig != null) {
            configDisplayPanel.displayConfiguration(currentConfig);
        }
        tabbedPane.addTab("Configuration", configDisplayPanel);

        analysisPanel = new AnalysisPanel();
        tabbedPane.addTab("Analysis", analysisPanel);

        reportPanel = new ReportPanel();
        tabbedPane.addTab("Reports", reportPanel);

        configEditorPanel = new ConfigurationEditorPanel();
        tabbedPane.addTab("Settings", configEditorPanel);

        dashboardPanel = new DashboardPanel();
        tabbedPane.addTab("Dashboard", dashboardPanel);

        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

    private void applyModernStyling() {
        UITheme.applyTheme();
    }

    public ProjectListPanel getProjectListPanel() {
        return projectListPanel;
    }

    public ConfigurationDisplayPanel getConfigDisplayPanel() {
        return configDisplayPanel;
    }

    public AnalysisPanel getAnalysisPanel() {
        return analysisPanel;
    }

    public ReportPanel getReportPanel() {
        return reportPanel;
    }

    public ConfigurationEditorPanel getConfigEditorPanel() {
        return configEditorPanel;
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }
}
