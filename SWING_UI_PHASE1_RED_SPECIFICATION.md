# Phase 1: Project Selection - RED Tests Specification

**Date:** 2026-07-18  
**Phase:** 1 - Project Management  
**Stage:** RED - Test Specification (What tests to write)  
**Status:** Ready for test implementation  
**Depends on:** Phase 0 complete

---

## Overview

Phase 1 enables switching between projects and managing configuration through the UI:
- Select project from dropdown
- Load configuration for selected project
- Display configuration (read-only in this phase)
- Handle invalid projects gracefully
- Refresh project list

**All behavior specified as failing tests (RED phase).**

---

## Test File 1: ProjectSelectionControllerTest

### Purpose
Verify controller correctly handles project selection events.

### Test Specifications

**Test 1.1: Select Project Loads Configuration**
- Description: User selects "GSPos" from dropdown
- Setup: Mock UIApplicationService, provide 2 projects
- Action: Call controller.selectProject("GSPos")
- Expected: service.switchProject("GSPos") called
- Verification: Correct method invoked with correct parameter

**Test 1.2: Invalid Project Throws Exception**
- Description: User tries to select non-existent project
- Setup: Mock service to throw IOException for invalid project
- Action: Call controller.selectProject("NonExistent")
- Expected: IOException propagated
- Verification: Exception thrown to caller

**Test 1.3: Selected Project Displayed in UI**
- Description: After selection, UI shows selected project
- Setup: Mock panel, select "TextAnalyser"
- Action: Call controller.selectProject("TextAnalyser")
- Expected: Panel.showSelectedProject("TextAnalyser") called
- Verification: UI updated with selection

**Test 1.4: Configuration Updates After Selection**
- Description: Configuration panel reflects new project settings
- Setup: "TextAnalyser" config differs from "GSPos" config
- Action: Select "GSPos"
- Expected: Configuration panel shows GSPos paths/settings
- Verification: Panel displays correct values

**Test 1.5: Project List Refreshes**
- Description: Manually refresh project list from UI
- Setup: Mock service returns ["Project1", "Project2"]
- Action: Call controller.refreshProjectList()
- Expected: Panel.updateProjectList(["Project1", "Project2"]) called
- Verification: List correctly populated

**Test 1.6: Refresh Fires Update Event**
- Description: When projects refreshed, listeners notified
- Setup: Register listener
- Action: Refresh projects
- Expected: onProjectListUpdated() called
- Verification: Event fired correctly

**Test 1.7: Switch Project Event Fired**
- Description: Project switch fires event to all listeners
- Setup: Register project listener
- Action: Switch project
- Expected: onProjectSwitched(projectName) called
- Verification: Correct event with correct project name

**Test 1.8: Multiple Controllers Share Same Service**
- Description: Multiple controllers can listen to same service events
- Setup: Create 2 controllers with same service
- Action: Switch project via controller 1
- Expected: Controller 2 also notified via service events
- Verification: Both controllers in sync

---

## Test File 2: ProjectSelectionPanelTest

### Purpose
Verify project selection UI component works correctly.

### Test Specifications

**Test 2.1: Dropdown Populated on Init**
- Description: Panel created with project list
- Setup: Provide ["TextAnalyser", "GSPos", "OtherProject"]
- Action: Create panel
- Expected: Dropdown shows all 3 projects
- Verification: All items visible in dropdown

**Test 2.2: Selection Listener Fires**
- Description: User selects different project
- Setup: Add selection listener
- Action: Select "GSPos" from dropdown
- Expected: Listener.onProjectSelected("GSPos") called
- Verification: Event fired with correct project

**Test 2.3: Current Selection Remembered**
- Description: After selection, getSelectedProject returns selected
- Setup: Select "TextAnalyser"
- Action: Call getSelectedProject()
- Expected: Returns "TextAnalyser"
- Verification: Correct value returned

**Test 2.4: Update Project List**
- Description: Update list after initial load
- Setup: Panel created with 2 projects
- Action: Call updateProjectList(["New1", "New2", "New3"])
- Expected: Dropdown now shows 3 new projects
- Verification: Old items replaced, new items visible

**Test 2.5: Empty List Handled**
- Description: Handle empty project list gracefully
- Setup: Panel with empty list
- Action: Create panel
- Expected: Shows message "No projects available"
- Verification: No crash, helpful message

**Test 2.6: Disable Selection During Load**
- Description: While loading projects, selection disabled
- Setup: Mock slow service
- Action: While loading, try to select
- Expected: Selection disabled until load complete
- Verification: Cannot change selection during load

**Test 2.7: Keyboard Navigation Works**
- Description: Select project using arrow keys
- Setup: Dropdown focused
- Action: Press down arrow to navigate
- Expected: Next project selected
- Verification: Keyboard interaction works

---

## Test File 3: ConfigurationDisplayPanelTest

### Purpose
Verify configuration display shows current project settings.

### Test Specifications

**Test 3.1: Project Name Displayed**
- Description: Show current project name
- Setup: Load project "GSPos"
- Action: Display panel with GSPos metadata
- Expected: "Project: GSPos" shown in label
- Verification: Project name visible

**Test 3.2: Source Path Displayed**
- Description: Show source directory path
- Setup: GSPos has path "/path/to/gspos/src"
- Action: Display panel
- Expected: Path label shows full path
- Verification: Correct path visible

**Test 3.3: Update on Project Change**
- Description: Panel updates when project changes
- Setup: Display TextAnalyser config
- Action: Change to GSPos
- Expected: Panel shows GSPos config (different values)
- Verification: Panel refreshes correctly

**Test 3.4: Read-Only Fields**
- Description: Configuration fields cannot be edited in this phase
- Setup: Display panel
- Action: Try to click/edit name field
- Expected: Field disabled or not editable
- Verification: Cannot modify display

**Test 3.5: Null Handling**
- Description: If no project loaded, show empty state
- Setup: No project selected
- Action: Display panel
- Expected: Shows "No project loaded" or similar
- Verification: Graceful empty state

**Test 3.6: Format Paths Nicely**
- Description: Long paths truncated for display
- Setup: Path is "/very/long/path/to/project/src/main/java"
- Action: Display panel
- Expected: Shows "...project/src/main/java" or similar
- Verification: Readable without scrolling

---

## Test File 4: IntegrationProjectSwitchingTest

### Purpose
Verify complete project switching workflow.

### Test Specifications

**Test 4.1: Full Switch Sequence**
- Description: User selects project → config loads → display updates
- Setup: 2 projects available
- Action: Select first project, wait for load, select second
- Expected: All 3 steps complete successfully
- Verification: Both projects' configs displayed correctly in sequence

**Test 4.2: Error During Switch**
- Description: Project switch fails (bad path, etc)
- Setup: Mock service throws exception
- Action: Try to switch
- Expected: Error dialog shown
- Verification: User informed of error, UI remains stable

**Test 4.3: Rapid Switching**
- Description: User rapidly clicks between projects
- Setup: Slow service (200ms per switch)
- Action: Click project 1, immediately click project 2, 3, 4
- Expected: Eventually stabilizes on project 4
- Verification: UI handles rapid clicks without crash

**Test 4.4: Cancel Pending Switch**
- Description: While switching, user switches again
- Setup: Slow switch (500ms)
- Action: Start switch, click different project before complete
- Expected: New project takes precedence
- Verification: Ends on final selected, not intermediate

**Test 4.5: Persistence After Restart**
- Description: Selected project persists after app restart
- Setup: Select "GSPos", close app, reopen
- Action: Reopen application
- Expected: "GSPos" still selected
- Verification: Configuration persisted

**Test 4.6: Multiple Projects Correctly Isolated**
- Description: Projects don't interfere with each other
- Setup: Switch between TextAnalyser and GSPos multiple times
- Action: Switch 5 times back and forth
- Expected: Each project configuration correct every time
- Verification: No cross-contamination between projects

**Test 4.7: Network Path Support** (optional)
- Description: Support network/UNC paths
- Setup: Path is "\\\\network\\share\\project\\src"
- Action: Switch to project with network path
- Expected: Path loads correctly
- Verification: Network paths work like local paths

---

## Test File 5: ProjectRefreshTest

### Purpose
Verify project list refresh functionality.

### Test Specifications

**Test 5.1: Refresh Button Initiates Reload**
- Description: Click refresh button
- Setup: Projects list displayed
- Action: Click refresh button
- Expected: service.getAvailableProjects() called
- Verification: Service called to reload list

**Test 5.2: Refresh Updates Dropdown**
- Description: New projects appear after refresh
- Setup: Initial projects: ["A", "B"]
- Action: Add project "C" to config, refresh
- Expected: Dropdown now shows ["A", "B", "C"]
- Verification: New project visible

**Test 5.3: Refresh Preserves Selection**
- Description: Selected project unchanged by refresh
- Setup: "TextAnalyser" selected
- Action: Refresh project list
- Expected: "TextAnalyser" still selected
- Verification: Selection preserved

**Test 5.4: Refresh Shows Loading State**
- Description: Show "Loading..." during refresh
- Setup: Mock slow service
- Action: Click refresh
- Expected: Loading indicator shown
- Verification: User sees loading feedback

**Test 5.5: Refresh Timeout**
- Description: If refresh takes too long, show timeout
- Setup: Mock service hangs for 10 seconds
- Action: Refresh with 5-second timeout
- Expected: Timeout error shown
- Verification: App doesn't hang forever

---

## Test File 6: ProjectMetadataTest

### Purpose
Verify project metadata DTO.

### Test Specifications

**Test 6.1: Create Metadata**
- Description: Create ProjectMetadata object
- Setup: name="GSPos", path="/path/to/src"
- Action: Create metadata
- Expected: Object created without error
- Verification: Object not null

**Test 6.2: Get Project Name**
- Description: Retrieve project name
- Setup: Metadata with name "TextAnalyser"
- Action: Call getProjectName()
- Expected: Returns "TextAnalyser"
- Verification: Correct value returned

**Test 6.3: Get Source Path**
- Description: Retrieve source path
- Setup: Metadata with path "/home/user/projects/src"
- Action: Call getSourcePath()
- Expected: Returns "/home/user/projects/src"
- Verification: Correct path returned

**Test 6.4: Immutable After Creation**
- Description: Cannot modify metadata after creation
- Setup: Created metadata
- Action: Try to setProjectName("Different")
- Expected: No setter method available
- Verification: Immutable (read-only)

**Test 6.5: Timestamp Recorded**
- Description: Track when metadata was created
- Setup: Create metadata
- Action: Get timestamp
- Expected: Returns System.currentTimeMillis() ish value
- Verification: Timestamp recorded

---

## Test Dependencies

### Mock Objects Needed
- Mock UIApplicationService
- Mock ProjectListener
- Mock ProjectSelectionPanel
- Mock ConfigurationDisplayPanel

### Test Fixtures
- 2 sample projects with different configs
- ProjectMetadata objects for testing
- Event objects (ProjectLoadedEvent, etc)

### Assertions Needed
- assertNotNull()
- assertEquals()
- assertTrue()
- assertFalse()
- verify() (Mockito)
- assertThrows()

---

## Test Execution Order

1. ProjectMetadataTest (DTO tests - no dependencies)
2. ProjectSelectionPanelTest (UI component - isolated)
3. ConfigurationDisplayPanelTest (UI component - isolated)
4. ProjectSelectionControllerTest (Controller - mocked service)
5. ProjectRefreshTest (Refresh functionality)
6. IntegrationProjectSwitchingTest (Full workflow)

---

## Expected Test Results

### RED Phase (before implementation)
```
ApplicationStartupTest (Phase 0) - ✅ PASS (from Phase 0)
ProjectSelectionControllerTest - ❌ FAIL (class doesn't exist)
ProjectSelectionPanelTest - ❌ FAIL
ConfigurationDisplayPanelTest - ❌ FAIL
ProjectRefreshTest - ❌ FAIL
IntegrationProjectSwitchingTest - ❌ FAIL
```

### GREEN Phase (after implementation)
```
All tests above - ✅ PASS
```

---

## Acceptance Criteria (Phase 1 Complete)

- [ ] All 6 test files created
- [ ] All tests written (RED - all fail)
- [ ] All tests pass after implementation (GREEN)
- [ ] Can select project from dropdown
- [ ] Configuration updates on selection
- [ ] Invalid projects show error
- [ ] Project list can be refreshed
- [ ] Selection persists across operations
- [ ] Clean compilation
- [ ] Phase 0 tests still pass

---

## Files to Create (Phase 1)

**Tests:** (to be written in RED phase)
- `ProjectSelectionControllerTest.java`
- `ProjectSelectionPanelTest.java`
- `ConfigurationDisplayPanelTest.java`
- `ProjectRefreshTest.java`
- `ProjectMetadataTest.java`
- `IntegrationProjectSwitchingTest.java`

**Implementation:** (to be written in GREEN phase)
- `ProjectSelectionController.java`
- `ProjectSelectionPanel.java` (Swing JPanel)
- `ConfigurationDisplayPanel.java` (Swing JPanel)
- `ProjectMetadata.java` (DTO - may already exist from Phase 0)

**Event Classes:** (should exist from Phase 0)
- `ProjectLoadingEvent.java`
- `ProjectLoadedEvent.java`
- `ProjectConfigurationChangedEvent.java`
- `ProjectErrorEvent.java`

---

**Status:** Ready for test creation  
**Next Step:** Write tests based on specifications above, watch them fail (RED)

