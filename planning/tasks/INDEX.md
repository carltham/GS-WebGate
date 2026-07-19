# Task Files Index

**Location:** `/planning/tasks/`

**Structure:** Individual task files organized by phase, with workflow tracking folders.

---

## Quick Start

1. **Phase 0:** Open `phase-0/0.1-planning-setup.md`
2. **Track Progress:** Move files to `todo/` then `done/` as you work
3. **Reference:** Check `../phases/phase-N/RED_SPECIFICATION.md` for test details

---

## Workflow Folders

### `/todo/`
Tasks ready to work on but not yet started.

**How to use:**
```bash
# Move task here when starting
mv phase-0/0.1-planning-setup.md todo/

# Move to done when complete
mv todo/0.1-planning-setup.md done/
```

### `/done/`
Completed tasks for reference and historical tracking.

---

## Phase Task Files

### Phase 0: Application Launcher (20 hours)

| Task | File | Duration | Status |
|------|------|----------|--------|
| 0.1 | `done/0.1-planning-setup.md` | 2h | ✅ |
| 0.2 | `done/0.2-write-tests-red.md` | 8h | ✅ |
| 0.3 | `done/0.3-implement-code-green.md` | 6h | ✅ |
| 0.4 | `done/0.4-refactor-quality.md` | 3h | ✅ |
| 0.5 | `done/0.5-commit.md` | 1h | ✅ |

### Phase 1: Project Selection (24 hours)

| Task | File | Duration | Status |
|------|------|----------|--------|
| 1.1 | `done/1.1-planning-setup.md` | 2h | ✅ |
| 1.2 | `done/1.2-write-tests-red.md` | 10h | ✅ |
| 1.3 | `done/1.3-implement-code-green.md` | 8h | ✅ |
| 1.4 | `done/1.4-refactor-quality.md` | 3h | ✅ |
| 1.5 | `done/1.5-commit.md` | 1h | ✅ |

### Phase 2: Analysis Execution (29 hours)

| Task | File | Duration | Status |
|------|------|----------|--------|
| 2.1 | `done/2.1-planning-setup.md` | 2h | ✅ |
| 2.2 | `done/2.2-write-tests-red.md` | 12h | ✅ |
| 2.3 | `done/2.3-implement-code-green.md` | 10h | ✅ |
| 2.4 | `done/2.4-refactor-quality.md` | 4h | ✅ |
| 2.5 | `done/2.5-commit.md` | 1h | ✅ |

### Phase 3: Report Display (26 hours)

| Task | File | Duration | Status |
|------|------|----------|--------|
| 3.1 | `done/3.1-planning-setup.md` | 2h | ✅ |
| 3.2 | `done/3.2-write-tests-red.md` | 10h | ✅ |
| 3.3 | `done/3.3-implement-code-green.md` | 10h | ✅ |
| 3.4 | `done/3.4-refactor-quality.md` | 3h | ✅ |
| 3.5 | `done/3.5-commit.md` | 1h | ✅ |

### Phase 4: Configuration Editor (21 hours)

| Task | File | Duration | Status |
|------|------|----------|--------|
| 4.1 | `done/4.1-planning-setup.md` | 2h | ✅ |
| 4.2 | `done/4.2-write-tests-red.md` | 8h | ✅ |
| 4.3 | `done/4.3-implement-code-green.md` | 8h | ✅ |
| 4.4 | `done/4.4-refactor-quality.md` | 2h | ✅ |
| 4.5 | `done/4.5-commit.md` | 1h | ✅ |

### Phase 5: Results Dashboard (23 hours)

| Task | File | Duration | Status |
|------|------|----------|--------|
| 5.1 | `done/5.1-planning-setup.md` | 2h | ✅ |
| 5.2 | `done/5.2-write-tests-red.md` | 9h | ✅ |
| 5.3 | `done/5.3-implement-code-green.md` | 9h | ✅ |
| 5.4 | `todo/5.4-refactor-quality.md` | 2h | 🟨 |
| 5.5 | `phase-5/5.5-commit.md` | 1h | ⬜ |

---

## Summary

**Total Tasks:** 30 individual task files
**Completed:** 28/30 (93%)
**In Progress:** 1/30 (Phase 5.4)
**Not Started:** 1/30 (Phase 5.5)

**By Phase:**
- Phase 0: ✅ 5/5 tasks complete (20 hours)
- Phase 1: ✅ 5/5 tasks complete (24 hours)
- Phase 2: ✅ 5/5 tasks complete (29 hours)
- Phase 3: ✅ 5/5 tasks complete (26 hours)
- Phase 4: ✅ 5/5 tasks complete (21 hours)
- Phase 5: 🟨 3/5 complete, 1 in progress, 1 pending (23 hours)

---

## Task Template Structure

Each task file follows this structure:

```
# [Phase N]: Task [N.M] - [Task Name]

**Phase:** [number] - [Phase Name]
**Task:** [N.M] - [Task Name]
**Estimated Hours:** [hours]
**Status:** [⬜ Not Started | 🟨 In Progress | ✅ Complete]

## Overview

[Brief description]

## Checklist

- [ ] Item 1
- [ ] Item 2
...

## Details

[Implementation details and guidance]

## Acceptance Criteria

- [ ] Criteria 1
- [ ] Criteria 2
...

## Next Step

→ Move to **[next task file]**
```

---

## How to Use This Index

### Starting Phase 0

```bash
cd /mnt/DATA/Projects/0.present-projects/Active/TextAnalyser/planning/tasks

# Open first task
cat phase-0/0.1-planning-setup.md

# Move to todo folder when starting
mv phase-0/0.1-planning-setup.md todo/

# After completing
mv todo/0.1-planning-setup.md done/
```

### Tracking Progress

**Status Indicators:**
- ⬜ Not Started
- 🟨 In Progress
- ✅ Complete
- ❌ Blocked

Update status in each task file as you work:
```markdown
**Status:** 🟨 In Progress
```

### Referencing Test Specifications

Each task file directs you to the corresponding specification:
- Phase 0 tests → `../phases/phase-0/RED_SPECIFICATION.md`
- Phase 1 tests → `../phases/phase-1/RED_SPECIFICATION.md`
- etc.

---

## Related Documents

- **Planning:** `/planning/README.md`
- **Architecture:** `/planning/SWING_UI_ARCHITECTURE.md`
- **Plan:** `/planning/SWING_UI_PLAN.md`
- **Phases Index:** `/planning/SWING_UI_ALL_PHASES_INDEX.md`
- **All Tasks:** `/planning/SWING_UI_ALL_TASKS.md`

---

## Notes

Each phase folder also contains:
- `NOTES.md` - Template for tracking actual execution vs estimates
- `RED_SPECIFICATION.md` (at phase level) - Detailed test requirements

---

**Ready to begin Phase 0?**

```bash
open phase-0/0.1-planning-setup.md
```

Then move task to `todo/` when you start working on it.
