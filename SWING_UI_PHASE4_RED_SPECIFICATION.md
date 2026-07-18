# Phase 4: Configuration Editor - RED Tests Specification

**Date:** 2026-07-18  
**Phase:** 4 - Configuration Management  
**Stage:** RED - Test Specification  
**Depends on:** Phase 1 complete

---

## Overview

Phase 4 enables editing project configuration from UI:
- Edit project name
- Edit source path
- Validate paths (non-empty, exists)
- Save changes
- Cancel without saving
- Show error on invalid path

---

## Test File 1: ConfigurationEditorControllerTest

**Test 1.1-1.8:** (8 tests)
- Open editor for current project
- Load configuration into editor
- Save changes to service
- Cancel without saving
- Validate empty name
- Validate empty path
- Validate path doesn't exist
- Enable/disable save button based on validity

---

## Test File 2: ConfigurationEditorPanelTest

**Test 2.1-2.12:** (12 tests)
- Panel created
- Name field editable
- Path field editable
- Save button visible
- Cancel button visible
- Error label visible
- Display current values
- Update name field
- Update path field
- Field validation shows error
- Save button enabled only if valid
- Clear error message on valid input

---

## Test File 3: PathValidationTest

**Test 3.1-3.8:** (8 tests)
- Validate existing path
- Reject non-existent path
- Reject empty path
- Reject path with special chars (on Windows)
- Accept UNC paths (\\network\share)
- Accept relative paths
- Accept absolute paths
- Show helpful error message

---

## Test File 4: PersistenceTest

**Test 4.1-4.6:** (6 tests)
- Save configuration to file
- Load configuration from file
- Configuration survives app restart
- Old configuration backed up before overwrite
- Concurrent edits handled safely
- File permissions checked

---

## Test File 5: IntegrationConfigurationEditingTest

**Test 5.1-5.6:** (6 tests)
- Edit and save configuration
- Updated config used in next analysis
- Switch between projects preserves their configs
- Invalid save rejected, UI stays
- Multiple configurations for different projects
- Config editor persists changes

---

## Acceptance Criteria (Phase 4 Complete)

- [ ] All test files created (5 files, 40 tests)
- [ ] All tests pass
- [ ] Can edit project configuration
- [ ] Path validation works
- [ ] Changes persist
- [ ] Invalid changes rejected
- [ ] Phase 0-3 tests still pass

---

**Status:** Ready for test creation

