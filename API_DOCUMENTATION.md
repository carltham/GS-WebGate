# TextAnalyser Swing UI - API Documentation

## Public APIs

### Application Entry Point

#### TextAnalyserApplication
Main application class.

**Constructor:**
```java
TextAnalyserApplication()
```
Initializes application, loads projects, creates main window.

**Methods:**
```java
MainWindow getMainWindow()                      // Get main UI window
ProjectMetadata getCurrentConfiguration()       // Get current project config
List<ProjectMetadata> getAvailableProjects()   // Get all available projects
void shutdown()                                 // Clean shutdown
```

**Example:**
```java
TextAnalyserApplication app = new TextAnalyserApplication();
MainWindow window = app.getMainWindow();
```

---

### Phase 0: Application Launcher

#### MainWindow
Main UI container with all phases in tabs.

**Constructor:**
```java
MainWindow()                                    // Default constructor
MainWindow(ProjectMetadata, List<ProjectMetadata>)  // With config
```

**Methods:**
```java
ProjectListPanel getProjectListPanel()          // Get Phase 1 panel
ConfigurationDisplayPanel getConfigDisplayPanel()
AnalysisPanel getAnalysisPanel()               // Get Phase 2 panel
ReportPanel getReportPanel()                   // Get Phase 3 panel
ConfigurationEditorPanel getConfigEditorPanel() // Get Phase 4 panel
DashboardPanel getDashboardPanel()             // Get Phase 5 panel
```

---

### Phase 1: Project Selection

#### ProjectSelectionController
Manages project selection logic.

**Constructor:**
```java
ProjectSelectionController()
```

**Methods:**
```java
void selectProject(String projectName)          // Select a project
String getSelectedProject()                     // Get current selection
void refreshProjectList()                       // Reload projects
```

#### ProjectSelectionPanel
UI for project selection.

**Constructor:**
```java
ProjectSelectionPanel()
```

**Methods:**
```java
void setProjects(List<ProjectMetadata> projects)
String getSelectedProject()
void addSelectionListener(ActionListener listener)
```

#### ProjectMetadata
Data transfer object for project information.

**Constructor:**
```java
ProjectMetadata(String name, String path)
```

**Methods:**
```java
String getProjectName()
String getProjectPath()
String getReportPath()
void setReportPath(String path)
```

---

### Phase 2: Analysis Execution

#### AnalysisController
Controls analysis workflow.

**Constructor:**
```java
AnalysisController()
```

**Methods:**
```java
void startAnalysis(String projectName)          // Start background analysis
void cancelAnalysis()                           // Cancel running analysis
AnalysisReport getLastReport()                  // Get most recent report
```

#### AnalysisPanel
UI for analysis status and control.

**Constructor:**
```java
AnalysisPanel()
```

**Methods:**
```java
void setStatusMessage(String message)
void setProgress(int percentage)
void enableStartButton()
void disableStartButton()
JButton getStartButton()
JButton getCancelButton()
```

#### AnalysisWorker
Background thread for analysis.

**Constructor:**
```java
AnalysisWorker(String projectName)
```

**Methods:**
```java
void execute()                                  // Run analysis in background
void cancel()                                   // Cancel analysis
```

#### AnalysisReport
Result of analysis.

**Methods:**
```java
List<Violation> getViolations()                 // Get all violations
int getTotalViolations()
int getTotalFilesAnalyzed()
long getAnalysisTime()
```

---

### Phase 3: Report Display

#### ReportController
Manages report display and export.

**Constructor:**
```java
ReportController()
```

**Methods:**
```java
void displayReport(AnalysisReport report)       // Display report
void filterByType(String type)                  // Filter violations
void sortByColumn(String column)                // Sort table
void exportToCSV(String filePath)               // Export as CSV
void exportToMarkdown(String filePath)          // Export as Markdown
```

#### ReportPanel
UI for report display.

**Constructor:**
```java
ReportPanel()
```

**Methods:**
```java
void displayViolations(List<Violation> violations)
void clearReport()
JTable getViolationTable()
JButton getExportButton()
```

#### ViolationTable
JTable for violation display.

**Constructor:**
```java
ViolationTable(List<Violation> violations)
```

**Methods:**
```java
void setData(List<Violation> violations)
void sortByColumn(int column)
```

#### ReportExporter
Handles export operations.

**Methods:**
```java
static void exportToCSV(AnalysisReport, String filePath)
static void exportToMarkdown(AnalysisReport, String filePath)
```

---

### Phase 4: Configuration Editor

#### ConfigurationEditorController
Manages configuration updates.

**Constructor:**
```java
ConfigurationEditorController()
```

**Methods:**
```java
void updateConfiguration(ProjectMetadata config)
void validatePath(String path)
void revertChanges()
```

#### ConfigurationEditorPanel
UI for editing configuration.

**Constructor:**
```java
ConfigurationEditorPanel()
```

**Methods:**
```java
void displayConfiguration(ProjectMetadata config)
ProjectMetadata getUpdatedConfiguration()
void save()
void cancel()
```

#### ConfigurationValidator
Validates path configurations.

**Methods:**
```java
static boolean isValidPath(String path)
static boolean pathExists(String path)
static String getErrorMessage(String path)
```

#### ConfigurationPersistence
Handles configuration file I/O.

**Methods:**
```java
static void saveConfiguration(ProjectMetadata, String filePath)
static ProjectMetadata loadConfiguration(String filePath)
```

---

### Phase 5: Results Dashboard

#### DashboardController
Aggregates metrics across projects.

**Constructor:**
```java
DashboardController()
```

**Methods:**
```java
void loadProjectStatistics(String projectName)
String getProjectName()
int getTotalFilesAnalyzed()
int getTotalViolations()
double getAverageViolationsPerFile()
String getLastAnalysisTime()
void refreshDashboard()
Map<String, Integer> getViolationsByType()
```

#### DashboardPanel
UI for dashboard display.

**Constructor:**
```java
DashboardPanel()
```

**Methods:**
```java
JLabel getProjectNameLabel()
JLabel getTotalFilesLabel()
JLabel getTotalViolationsLabel()
JLabel getAverageViolationsLabel()
JButton getRefreshButton()

void setProjectName(String name)
void setTotalFiles(int count)
void setTotalViolations(int count)
void setAverageViolations(double average)
```

#### DashboardRefresh
Manages dashboard refresh intervals.

**Constructor:**
```java
DashboardRefresh()
```

**Methods:**
```java
void manualRefresh()
void enableAutoRefresh(long intervalMillis)
void disableAutoRefresh()
boolean isAutoRefreshEnabled()
void setRefreshInterval(long intervalMillis)
String getLastRefreshTime()
```

#### ProjectOverview
Displays project information.

**Constructor:**
```java
ProjectOverview()
```

**Methods:**
```java
void setProjectName(String name)
void setProjectPath(String path)
void setLastAnalysisDate(String date)
void displayProjectInfo(String name, String path, String date)
void updateProjectInfo(String name, String path, String date)
void clearProjectInfo()
JPanel getPanel()
```

#### StatisticsDisplay
Displays analysis statistics.

**Constructor:**
```java
StatisticsDisplay()
```

**Methods:**
```java
void displayTotalFiles(int count)
void displayTotalViolations(int count)
void displayViolationPercentage(double percentage)
void displayAnalysisTime(long milliseconds)
void clearStatistics()
void updateStatistics(int files, int violations, long time)
JPanel getPanel()
```

---

## Usage Examples

### Example 1: Run Analysis and Display Report
```java
// Start application
TextAnalyserApplication app = new TextAnalyserApplication();

// Get analysis panel
AnalysisPanel panel = app.getMainWindow().getAnalysisPanel();

// Start analysis
AnalysisController controller = new AnalysisController();
controller.startAnalysis("MyProject");

// Wait for completion...

// Display report
ReportController reportCtrl = new ReportController();
reportCtrl.displayReport(controller.getLastReport());
```

### Example 2: Export Analysis Results
```java
ReportController controller = new ReportController();
AnalysisReport report = ...;

// Export to CSV
controller.displayReport(report);
controller.exportToCSV("/path/to/results.csv");

// Export to Markdown
controller.exportToMarkdown("/path/to/results.md");
```

### Example 3: View Dashboard Metrics
```java
DashboardController dashboard = new DashboardController();

// Load project statistics
dashboard.loadProjectStatistics("MyProject");

// Get metrics
int files = dashboard.getTotalFilesAnalyzed();
int violations = dashboard.getTotalViolations();
double average = dashboard.getAverageViolationsPerFile();

// Display in UI
DashboardPanel panel = new DashboardPanel();
panel.setTotalFiles(files);
panel.setTotalViolations(violations);
panel.setAverageViolations(average);
```

---

## Dependencies

### Runtime
- `javax.swing` - UI framework
- `java.util.logging` - Logging

### Test
- JUnit 5 (junit-jupiter)
- Mockito

### Build
- Maven 3.6+
- Java 11+

---

## Thread Safety

### EDT Operations
All UI updates must be on EDT:
```java
SwingUtilities.invokeLater(() -> {
    panel.updateDisplay(data);
});
```

### Worker Operations
Long operations in background:
```java
AnalysisWorker worker = new AnalysisWorker(projectName);
worker.execute();  // Runs on worker thread
```

### Synchronization
Volatile fields for thread-safe visibility:
```java
private volatile boolean autoRefreshEnabled = false;
```

---

## Error Handling

### Logging
```java
Logger logger = Logger.getLogger(ClassName.class.getName());
logger.info("Operation successful");
logger.warning("Warning message");
logger.severe("Error message");
```

### Validation
```java
if (!ConfigurationValidator.isValidPath(path)) {
    throw new IllegalArgumentException("Invalid path: " + path);
}
```

---

## Version History

- **1.0** - Initial release with all 6 phases
  - Phase 0: Application Launcher
  - Phase 1: Project Selection
  - Phase 2: Analysis Execution
  - Phase 3: Report Display
  - Phase 4: Configuration Editor
  - Phase 5: Results Dashboard
