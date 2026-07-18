# Phase 3: Report Display - RED Tests Specification

**Date:** 2026-07-18  
**Phase:** 3 - Report Viewing  
**Stage:** RED - Test Specification  
**Depends on:** Phase 2 complete

---

## Overview

Phase 3 displays analysis results:
- Load reports after analysis completes
- Display violations in table
- Filter by violation type
- Sort by column
- Export to CSV
- Export to Markdown

---

## Test File 1: ReportControllerTest

**Test 1.1-1.8:** (8 tests)
- Load report after analysis
- Display violations in table
- Filter by type
- Filter by severity
- Sort by class name
- Sort by severity
- Export to CSV
- Export to Markdown

---

## Test File 2: ReportPanelTest

**Test 2.1-2.10:** (10 tests)
- Panel created
- Violations table displayed
- Column headers visible
- Rows show data
- Empty table handled
- Table scrollable
- Selected row highlighted
- Double-click on row
- Copy cell value
- Resize columns

---

## Test File 3: ViolationTableTest

**Test 3.1-3.8:** (8 tests)
- Table model created
- Row count correct
- Column count correct
- Get cell value
- Update row
- Add row
- Remove row
- Sort column

---

## Test File 4: FilteringTest

**Test 4.1-4.7:** (7 tests)
- Filter by violation type
- Filter by severity (ERROR/WARNING/INFO)
- Filter by class name
- Multiple filters combined
- Clear filters
- Filter count updated
- Empty result after filter

---

## Test File 5: SortingTest

**Test 5.1-5.6:** (6 tests)
- Sort ascending
- Sort descending
- Sort by different columns
- Sort with filters applied
- Sort preserves data integrity
- Sort performance

---

## Test File 6: ExportTest

**Test 6.1-6.8:** (8 tests)
- Export to CSV file
- CSV format valid
- CSV contains all columns
- CSV contains all rows
- Export to Markdown file
- Markdown format valid
- Markdown has table
- Markdown has summary

---

## Test File 7: IntegrationReportViewingTest

**Test 7.1-7.5:** (5 tests)
- Complete workflow: analyze → view report → filter → export
- Switch projects with reports open
- Reload latest report
- View historical reports
- Report updates after re-analysis

---

## Acceptance Criteria (Phase 3 Complete)

- [ ] All test files created (7 files, 52 tests)
- [ ] All tests pass
- [ ] Can view analysis results
- [ ] Can filter violations
- [ ] Can sort by column
- [ ] Can export to CSV
- [ ] Can export to Markdown
- [ ] Table performance acceptable
- [ ] Phase 0-2 tests still pass

---

**Status:** Ready for test creation

