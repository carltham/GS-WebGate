# TextAnalyser Swing UI - User Guide

## Overview

TextAnalyser Swing UI is a comprehensive code analysis tool with a graphical interface for analyzing Java projects, generating violation reports, and tracking compliance metrics over time.

---

## Installation

### Prerequisites
- Java 11+
- Maven 3.6+

### Build from Source
```bash
cd TextAnalyser-pom/TextAnalyser-UI-swing
mvn clean package
```

### Running the Application
```bash
java -jar target/TextAnalyser-UI-swing-*.jar
```

Or directly:
```bash
mvn clean compile exec:java -Dexec.mainClass="com.noprobit.tools.ui.TextAnalyserApplication"
```

---

## Getting Started

### 1. **Project Selection**
   - Launch the application
   - The project selector appears at the top
   - Select a project from the dropdown
   - Configuration automatically loads

### 2. **Configuration**
   - View project configuration in the "Configuration" tab
   - Edit paths and settings in the "Settings" tab
   - Save configuration changes

### 3. **Run Analysis**
   - Go to "Analysis" tab
   - Click "Start Analysis"
   - Monitor progress bar
   - Wait for completion

### 4. **View Reports**
   - Results appear in "Reports" tab
   - Violations displayed in searchable table
   - Filter by severity or type
   - Sort by any column
   - Export to CSV or Markdown

### 5. **Dashboard**
   - View aggregated metrics across all projects
   - See trends over time
   - Compare projects side-by-side
   - Export aggregated reports

---

## Features

### Phase 1: Project Selection
- Multi-project support
- Quick project switching
- Configuration auto-loading

### Phase 2: Analysis Execution
- Background analysis (non-blocking UI)
- Real-time progress updates
- Cancellation support
- Error handling

### Phase 3: Report Display
- Violation table with all details
- Filtering by severity, type, category
- Sorting by any column
- CSV export
- Markdown export

### Phase 4: Configuration Editor
- Edit project paths
- Validation of path existence
- Save/Cancel workflow
- Cross-platform path handling

### Phase 5: Dashboard
- Multi-project aggregated metrics
- Compliance rate calculations
- Trend analysis
- Date range filtering
- Aggregated export

---

## Common Tasks

### Analyzing Multiple Projects
1. Select project 1, run analysis
2. View reports
3. Switch to project 2
4. Run analysis
5. Go to Dashboard to compare

### Filtering Violations
1. Go to Reports tab
2. Use the Filter panel
3. Select severity: All, Critical, Warning, Info
4. Results update in real-time

### Exporting Data
1. Go to Reports tab
2. Click "Export to CSV" or "Export to Markdown"
3. Choose location
4. File saved

### Tracking Progress Over Time
1. Run analyses periodically
2. Go to Dashboard
3. View trend chart
4. Export aggregated metrics

---

## Troubleshooting

### Application Won't Start
- Ensure Java 11+ installed: `java -version`
- Check Maven build: `mvn clean compile`
- Review logs for errors

### Analysis Fails
- Verify project path is correct
- Check file permissions
- Ensure Java files are in configured source directory

### Reports Show No Data
- Confirm analysis completed (check status message)
- Verify violations exist in project
- Try running analysis again

### Export Fails
- Ensure write permissions to target directory
- Check disk space available
- Verify file path is valid

---

## System Requirements

| Component | Requirement |
|-----------|------------|
| Java | 11 or higher |
| Memory | 512 MB minimum |
| Disk Space | 100 MB for application + data |
| Display | 1024x768 minimum |
| OS | Windows, macOS, Linux |

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Run Analysis | Alt+A |
| Export Report | Ctrl+E |
| Refresh Dashboard | F5 |
| Settings | Ctrl+, |

---

## Support

For issues or feature requests, contact the development team or check the DEVELOPER_GUIDE.md for contribution guidelines.
