# Phase 2: Analysis Execution - RED Tests Specification

**Date:** 2026-07-18  
**Phase:** 2 - Analysis Execution  
**Stage:** RED - Test Specification  
**Status:** Ready for test implementation  
**Depends on:** Phase 1 complete

---

## Overview

Phase 2 implements analysis execution with real-time progress:
- Start analysis button
- Progress bar (0-100%)
- Status messages during analysis
- Cancel analysis button
- Run analysis in background thread
- Error handling and recovery

**All behavior specified as failing tests (RED phase).**

---

## Test File 1: AnalysisControllerTest

### Purpose
Verify analysis controller manages analysis workflow.

### Test Specifications

**Test 1.1: Start Analysis**
- Description: User clicks "Start Analysis" button
- Setup: Project loaded, mock service
- Action: controller.startAnalysis()
- Expected: service.startAnalysis() called
- Verification: Service called correctly

**Test 1.2: Analysis Running Flag**
- Description: After start, isAnalysisRunning() returns true
- Setup: Call startAnalysis()
- Action: Check isAnalysisRunning()
- Expected: Returns true
- Verification: Running state tracked

**Test 1.3: Cancel Analysis**
- Description: User clicks "Cancel" during analysis
- Setup: Analysis running
- Action: controller.cancelAnalysis()
- Expected: service.cancelAnalysis() called
- Verification: Service cancel invoked

**Test 1.4: Analysis Completion**
- Description: Receive completion event from service
- Setup: Register as analysis listener
- Action: Fire onAnalysisCompleted event
- Expected: Controller notifies UI components
- Verification: UI updated on completion

**Test 1.5: Progress Updates**
- Description: Receive progress events during analysis
- Setup: Register as analysis listener
- Action: Fire onAnalysisProgress(50 files of 100)
- Expected: Controller updates progress tracking
- Verification: Progress value tracked (50%)

**Test 1.6: Error Handling**
- Description: Analysis fails or throws error
- Setup: Register listener, mock error
- Action: Fire onAnalysisError("File not found")
- Expected: Error message captured
- Verification: Error information available

**Test 1.7: Cannot Start While Running**
- Description: Prevent starting new analysis while one runs
- Setup: Analysis running
- Action: Try to start another analysis
- Expected: Throws IllegalStateException or denied
- Verification: Only one analysis at a time

**Test 1.8: Auto-Enable Cancel During Analysis**
- Description: Cancel button enabled only during analysis
- Setup: Analysis not running
- Action: Check cancel button state
- Expected: Disabled
- Action2: Start analysis
- Expected2: Cancel button enabled
- Verification: Button state tracks analysis

**Test 1.9: Completion Fires Event**
- Description: Analysis completion fires listener event
- Setup: Register completion listener
- Action: Fire onAnalysisCompleted event
- Expected: Listener.onAnalysisComplete() called
- Verification: Event propagated to listeners

**Test 1.10: Multiple Listeners**
- Description: Multiple components can listen to analysis
- Setup: Register 2 listeners
- Action: Fire progress event
- Expected: Both listeners notified
- Verification: All listeners receive events

---

## Test File 2: AnalysisPanelTest

### Purpose
Verify analysis UI component displays progress.

### Test Specifications

**Test 2.1: Panel Created**
- Description: Create AnalysisPanel
- Setup: None
- Action: Create panel
- Expected: Panel object created
- Verification: Object not null

**Test 2.2: Start Button Clicks**
- Description: Click start button
- Setup: Create panel with listener
- Action: Click start button
- Expected: onStartClicked() fired
- Verification: Button event captured

**Test 2.3: Cancel Button Clicks**
- Description: Click cancel button
- Setup: Analysis running
- Action: Click cancel
- Expected: onCancelClicked() fired
- Verification: Button event captured

**Test 2.4: Progress Bar Updates**
- Description: Update progress bar from 0% to 100%
- Setup: Create panel
- Action: Call updateProgress(50)
- Expected: Progress bar shows 50%
- Verification: Visual update correct

**Test 2.5: Status Message Displays**
- Description: Show status message
- Setup: Create panel
- Action: Call setStatusMessage("Processing file.java")
- Expected: Message displayed in label
- Verification: Message visible to user

**Test 2.6: Progress Percentage Calculation**
- Description: Show files processed / total files
- Setup: Create panel
- Action: Call updateProgress(50, 100)
- Expected: Shows "50/100" and progress bar at 50%
- Verification: Both visual and numeric progress

**Test 2.7: Analysis Complete State**
- Description: Show completion summary
- Setup: Analysis completed
- Action: Call showCompletion(analyzedFiles, violations)
- Expected: Shows summary with numbers
- Verification: Completion info displayed

**Test 2.8: Error Display**
- Description: Show error message if analysis fails
- Setup: Analysis failed
- Action: Call showError("Analysis failed: bad path")
- Expected: Error message displayed
- Verification: Error visible to user

**Test 2.9: Disable Start Button During Analysis**
- Description: Start button disabled while running
- Setup: Not running - start enabled
- Action: Start analysis
- Expected: Start button disabled
- Verification: Cannot click start again

**Test 2.10: Enable Start After Completion**
- Description: Start button enabled after analysis ends
- Setup: Analysis running with start disabled
- Action: Analysis completes
- Expected: Start button re-enabled
- Verification: Can start new analysis

---

## Test File 3: AnalysisThreadingTest

### Purpose
Verify analysis runs in background without blocking UI.

### Test Specifications

**Test 3.1: Analysis Runs in Background**
- Description: Long analysis doesn't block main thread
- Setup: Create analysis that takes 1 second
- Action: Start analysis, perform UI operation immediately
- Expected: UI operation completes while analysis runs
- Verification: Concurrent execution

**Test 3.2: Progress Updates on Event Thread**
- Description: Progress events come on event dispatch thread
- Setup: Mock progress event
- Action: Fire progress event during analysis
- Expected: Update happens on EDT
- Verification: Thread safety verified

**Test 3.3: Cancellation Thread-Safe**
- Description: Cancel signal safely stops background analysis
- Setup: Analysis running in background
- Action: Call cancel from main thread
- Expected: Analysis stops cleanly
- Verification: No thread corruption

**Test 3.4: Multiple Events Queued Safely**
- Description: Rapid progress events handled correctly
- Setup: Rapid progress updates (every 10ms)
- Action: Fire 100 progress events quickly
- Expected: All handled without errors
- Verification: No dropped events, thread safe

**Test 3.5: Error in Background Caught**
- Description: If background task crashes, error reported
- Setup: Mock task that throws exception
- Action: Start analysis
- Expected: onAnalysisError event fired
- Verification: Error properly reported

**Test 3.6: Shutdown Waits for Analysis**
- Description: Closing app waits for running analysis
- Setup: Analysis running
- Action: Close application
- Expected: App waits max 5 seconds for analysis
- Verification: Clean shutdown

---

## Test File 4: AnalysisProgressEventTest

### Purpose
Verify analysis progress event structure.

### Test Specifications

**Test 4.1: Create Progress Event**
- Description: Create progress event
- Setup: 50 files processed of 100 total
- Action: Create AnalysisProgressEvent(50, 100, "file.java", 5000)
- Expected: Event created
- Verification: Object not null

**Test 4.2: Get Processed Count**
- Description: Retrieve processed file count
- Setup: Event with 50 processed
- Action: getProcessedFiles()
- Expected: Returns 50
- Verification: Correct value

**Test 4.3: Get Total Count**
- Description: Retrieve total file count
- Setup: Event with 100 total
- Action: getTotalFiles()
- Expected: Returns 100
- Verification: Correct value

**Test 4.4: Calculate Percentage**
- Description: Automatically calculate percentage
- Setup: 50 of 100 processed
- Action: getPercentage()
- Expected: Returns 50
- Verification: Correct calculation

**Test 4.5: Zero Division Safe**
- Description: Handle zero total files
- Setup: 0 files total
- Action: getPercentage()
- Expected: Returns 0 (not NaN)
- Verification: No division by zero

**Test 4.6: Get Current File**
- Description: Get name of file being processed
- Setup: Event for "MyClass.java"
- Action: getCurrentFile()
- Expected: Returns "MyClass.java"
- Verification: Correct file name

**Test 4.7: Get Elapsed Time**
- Description: Get milliseconds elapsed
- Setup: Analysis started 5 seconds ago
- Action: getElapsedMillis()
- Expected: Returns ~5000 (may vary)
- Verification: Reasonable time value

---

## Test File 5: AnalysisErrorHandlingTest

### Purpose
Verify error handling during analysis.

### Test Specifications

**Test 5.1: Invalid Source Path**
- Description: Analysis fails with invalid path
- Setup: Project config points to non-existent path
- Action: Start analysis
- Expected: onAnalysisError fired with message
- Verification: Error properly reported

**Test 5.2: Encoding Error**
- Description: File with bad encoding throws error
- Setup: Source contains file with invalid UTF-8
- Action: Start analysis
- Expected: Error event, analysis continues if possible
- Verification: Error handled gracefully

**Test 5.3: Permission Denied**
- Description: Cannot read source directory
- Setup: Source path not readable (permission denied)
- Action: Start analysis
- Expected: onAnalysisError("Permission denied")
- Verification: Clear error message

**Test 5.4: Cancel Shows as Cancellation**
- Description: User cancel shown as cancellation, not error
- Setup: Analysis running
- Action: Cancel, check error event
- Expected: isCancelled() returns true
- Verification: Cancel vs error distinguished

**Test 5.5: Retry After Error**
- Description: Can retry analysis after error
- Setup: Analysis fails
- Action: Fix issue, start analysis again
- Expected: New analysis starts successfully
- Verification: Recovery works

**Test 5.6: No Analysis Running on Error**
- Description: isAnalysisRunning() returns false after error
- Setup: Analysis fails
- Action: Check isAnalysisRunning()
- Expected: Returns false
- Verification: State correctly updated

**Test 5.7: Error UI Dismissed Allows Retry**
- Description: After error shown, can start new analysis
- Setup: Show error dialog
- Action: Dismiss error, click start
- Expected: New analysis starts
- Verification: UI recovers

---

## Test File 6: IntegrationAnalysisWorkflowTest

### Purpose
Verify complete analysis workflow end-to-end.

### Test Specifications

**Test 6.1: Complete Analysis Workflow**
- Description: User starts analysis and watches completion
- Setup: Project loaded, service ready
- Action: Click start → watch progress → completion
- Expected: Progress goes 0% → 100% → completion shown
- Verification: Full workflow works

**Test 6.2: Cancel Mid-Analysis**
- Description: Start analysis, cancel halfway
- Setup: Analysis running
- Action: Wait for ~50%, click cancel
- Expected: Analysis stops, shows cancelled message
- Verification: Cancellation works mid-workflow

**Test 6.3: Rapid Start-Cancel**
- Description: Start and immediately cancel
- Setup: Create panel
- Action: Click start, immediately click cancel
- Expected: Analysis cancels cleanly
- Verification: No errors on rapid cancel

**Test 6.4: Multiple Analyses Sequential**
- Description: Run analysis, complete, run again
- Setup: Complete one analysis
- Action: Start another analysis
- Expected: Second analysis runs correctly
- Verification: Can run multiple in sequence

**Test 6.5: Analysis With Different Projects**
- Description: Analyze project A, switch to B, analyze B
- Setup: 2 different projects
- Action: Analyze A, complete, switch, analyze B
- Expected: Both analyses run with correct data
- Verification: Project isolation verified

**Test 6.6: Long-Running Analysis**
- Description: Analysis taking 30+ seconds
- Setup: Large project (1000+ files)
- Action: Start and monitor progress
- Expected: Progress updates received, completes
- Verification: Handles long analyses

**Test 6.7: Progress Events in Order**
- Description: Progress events arrive in order (not 50%, 30%, 70%)
- Setup: Rapid analysis with many files
- Action: Capture all progress events
- Expected: Events in increasing percentage order
- Verification: Monotonic progress

---

## Test Dependencies

### Mock Objects
- Mock UIApplicationService
- Mock AnalysisListener
- Mock AnalysisPanel

### Test Fixtures
- Sample project configuration
- Mock slow-running analysis service

### Threading Utilities
- SwingUtilities for EDT operations
- CountDownLatch for thread synchronization

---

## Test Execution Order

1. AnalysisProgressEventTest (DTO)
2. AnalysisPanelTest (UI)
3. AnalysisControllerTest (Controller)
4. AnalysisThreadingTest (Threading)
5. AnalysisErrorHandlingTest (Error paths)
6. IntegrationAnalysisWorkflowTest (Full workflow)

---

## Acceptance Criteria (Phase 2 Complete)

- [ ] All test files created
- [ ] All tests written (RED)
- [ ] All tests pass (GREEN)
- [ ] Can start analysis from UI
- [ ] Progress bar updates in real-time
- [ ] Status messages show current file
- [ ] Can cancel running analysis
- [ ] Error messages display clearly
- [ ] Analysis runs in background (non-blocking)
- [ ] Can run multiple analyses sequentially
- [ ] Phase 0-1 tests still pass

---

**Status:** Ready for test creation  
**Next Step:** Write Phase 2 tests

