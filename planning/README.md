# Swing UI Planning Folder

**Date:** 2026-07-18  
**Status:** Complete planning documentation  
**Ready for:** Phase 0 test implementation

---

## Folder Structure

```
planning/
├── README.md (you are here)
├── SWING_UI_PLAN.md (6-phase strategy, TDD rules, timeline)
├── SWING_UI_ARCHITECTURE.md (6-tier design, flows, DTOs)
├── CODE_MIGRATION_PLAN.md (integration with existing code)
├── SWING_UI_ALL_PHASES_INDEX.md (master index and summary)
├── SWING_UI_ALL_TASKS.md (complete task breakdown)
│
├── phases/
│   ├── phase-0/
│   │   ├── RED_SPECIFICATION.md (20 test specifications)
│   │   └── NOTES.md (track deviations)
│   ├── phase-1/
│   │   ├── RED_SPECIFICATION.md (26 test specs)
│   │   └── NOTES.md
│   ├── phase-2/
│   │   ├── RED_SPECIFICATION.md (40 test specs)
│   │   └── NOTES.md
│   ├── phase-3/
│   │   ├── RED_SPECIFICATION.md (52 test specs)
│   │   └── NOTES.md
│   ├── phase-4/
│   │   ├── RED_SPECIFICATION.md (40 test specs)
│   │   └── NOTES.md
│   └── phase-5/
│       ├── RED_SPECIFICATION.md (51 test specs)
│       └── NOTES.md
│
└── tasks/ ← **START HERE FOR IMPLEMENTATION**
    ├── INDEX.md (task file guide and reference)
    ├── 5.5-commit.md (Phase 5 final commit)
    │
    ├── phase-0/
    │   ├── 0.1-planning-setup.md
    │   ├── 0.2-write-tests-red.md
    │   ├── 0.3-implement-code-green.md
    │   ├── 0.4-refactor-quality.md
    │   └── 0.5-commit.md
    ├── phase-1/
    │   ├── 1.1-planning-setup.md
    │   ├── 1.2-write-tests-red.md
    │   ├── 1.3-implement-code-green.md
    │   ├── 1.4-refactor-quality.md
    │   └── 1.5-commit.md
    ├── phase-2/
    │   ├── 2.1-planning-setup.md
    │   ├── 2.2-write-tests-red.md
    │   ├── 2.3-implement-code-green.md
    │   ├── 2.4-refactor-quality.md
    │   └── 2.5-commit.md
    ├── phase-3/
    │   ├── 3.1-planning-setup.md
    │   ├── 3.2-write-tests-red.md
    │   ├── 3.3-implement-code-green.md
    │   ├── 3.4-refactor-quality.md
    │   └── 3.5-commit.md
    ├── phase-4/
    │   ├── 4.1-planning-setup.md
    │   ├── 4.2-write-tests-red.md
    │   ├── 4.3-implement-code-green.md
    │   ├── 4.4-refactor-quality.md
    │   └── 4.5-commit.md
    ├── phase-5/
    │   ├── 5.1-planning-setup.md
    │   ├── 5.2-write-tests-red.md
    │   ├── 5.3-implement-code-green.md
    │   ├── 5.4-refactor-quality.md
    │   └── 5.5-commit.md
    │
    ├── todo/ (move tasks here when starting)
    └── done/ (move tasks here when completed)
```

---

## Getting Started

### Step 1: Review Foundation Documents (2 hours)
1. **SWING_UI_PLAN.md** - Overall strategy and timeline
2. **SWING_UI_ARCHITECTURE.md** - 6-tier design and flows
3. **CODE_MIGRATION_PLAN.md** - Integration approach

### Step 2: Understand Phase Structure (1 hour)
1. **SWING_UI_ALL_PHASES_INDEX.md** - Complete overview
2. **tasks/INDEX.md** - Task file organization guide
3. Choose: Read full specs OR dive into tasks

### Step 3: Start Implementation (Week 1+)

**Option A: Task-Driven (Recommended)**
```bash
# 1. Navigate to tasks folder
cd planning/tasks

# 2. Open first task (Phase 0)
open phase-0/0.1-planning-setup.md

# 3. Move to todo/ when starting
mv phase-0/0.1-planning-setup.md todo/

# 4. Complete task and move to done/
mv todo/0.1-planning-setup.md done/

# 5. Move to next task in phase
mv phase-0/0.2-write-tests-red.md todo/
```

**Option B: Specification-Driven**
1. Open `phases/phase-0/RED_SPECIFICATION.md`
2. Read full test requirements
3. Reference task file `tasks/phase-0/0.2-write-tests-red.md`
4. Write tests following specification

### Step 4: Verify Progress

Each phase completes when:
- All tests in phase pass ✅
- All previous phase tests still pass ✅
- Code compiles with zero warnings ✅
- Phase commit made ✅
- NOTES.md updated ✅

---

## Document Purposes

### Foundation Documents (Read First)

**SWING_UI_PLAN.md**
- 6-phase overview
- TDD discipline rules
- Timeline and dependencies
- Success criteria per phase

**SWING_UI_ARCHITECTURE.md**
- Layer separation (6-tier)
- Communication flows
- Event system
- Data structures (DTOs)

**CODE_MIGRATION_PLAN.md**
- How existing code gets wrapped
- What stays unchanged
- What gets new service layers
- Integration points

**SWING_UI_ALL_PHASES_INDEX.md**
- Summary of all 6 phases
- Test statistics (229 tests)
- Dependency chain
- File structure needed

**SWING_UI_ALL_TASKS.md**
- Complete task breakdown
- Estimated hours per phase
- Cumulative task checklist
- Daily tracking template

### Phase Documents (Per Phase)

**phases/phase-N/RED_SPECIFICATION.md**
- What tests to write
- Test descriptions
- Expected behavior
- Edge cases
- File structure

**phases/phase-N/TASKS.md**
- Specific tasks for this phase
- Organized into 5 sections:
  1. Planning & Setup
  2. Write Tests (RED)
  3. Implement Code (GREEN)
  4. Refactor & Quality
  5. Commit

**phases/phase-N/NOTES.md**
- Track your actual execution
- Document deviations from spec
- Record lessons learned
- Track time spent

---

## Quick Reference

### Phase Overview
- Phase 0: Application Launcher (20 hours)
- Phase 1: Project Selection (24 hours)
- Phase 2: Analysis Execution (29 hours)
- Phase 3: Report Display (26 hours)
- Phase 4: Configuration Editor (21 hours)
- Phase 5: Dashboard (23 hours)
- Integration & Final: 17 hours
- **Total: 160 hours**

### Test Statistics
- Phase 0: 20 tests
- Phase 1: 26 tests
- Phase 2: 40 tests
- Phase 3: 52 tests
- Phase 4: 40 tests
- Phase 5: 51 tests
- **Total: 229 tests**

### Key Dates
- Week 1: Phase 0
- Week 2: Phase 1
- Week 3: Phase 2
- Week 4: Phase 3
- Week 5: Phase 4
- Week 6: Phase 5
- Week 7-8: Integration & final

---

## Phase Workflow Template

### For each phase:

**Day 1-2: Tests (RED)**
```
1. Read RED_SPECIFICATION.md
2. Create test files (5-7 per phase)
3. Write all test methods
4. Run mvn test - all should FAIL
```

**Day 3-4: Implementation (GREEN)**
```
1. Create minimal implementation
2. No extra features
3. Just enough to pass tests
4. Run mvn test - all should PASS
```

**Day 5: Refactor & Commit (REFACTOR)**
```
1. Improve code quality
2. Better names, extract duplicates
3. Add logging, optimize
4. Keep tests passing
5. Run mvn clean compile - zero warnings
6. Commit with phase message
7. Update NOTES.md
```

---

## Task Workflow

### For Each Individual Task

1. **Open task file**
   ```bash
   cat tasks/phase-N/N.X-task-name.md
   ```

2. **Read checklist** - Understand what needs to be done

3. **Start work** - Move file to todo/
   ```bash
   mv tasks/phase-N/N.X-task-name.md tasks/todo/
   ```

4. **Complete checklist** - Mark items done

5. **Finish task** - Move to done/
   ```bash
   mv tasks/todo/N.X-task-name.md tasks/done/
   ```

6. **Progress to next** - Open next task file

### Phase Completion Checklist

Before moving to next phase:
- [ ] All tasks in current phase completed
- [ ] All tests pass (current + all previous)
- [ ] Code compiles with zero warnings
- [ ] Phase commit made
- [ ] NOTES.md in phases/phase-N/ updated
- [ ] Verified no regression in previous phases

### After All Phases Complete

**MVP Ready When:**
- [ ] 246 tests all passing (29+34+40+52+40+51)
- [ ] Zero compilation warnings
- [ ] Full Swing UI functional
- [ ] All 6 phases complete
- [ ] Ready for integration testing

---

## Notes on Deviations

Each phase folder has a NOTES.md file. Use it to track:
- What differed from the spec
- Why you made changes
- Time actually spent vs estimated
- Blockers encountered
- Lessons learned

This helps future implementations and maintains accuracy.

---

## File Locations

All planning documents are now in:
`/mnt/DATA/Projects/0.present-projects/Active/TextAnalyser/planning/`

Implementation code will go in:
`src/main/java/com/noprobit/tools/ui/`
`src/test/java/com/noprobit/tools/ui/`

---

## Quick Links

- [Overall Plan](SWING_UI_PLAN.md)
- [Architecture Design](SWING_UI_ARCHITECTURE.md)
- [Code Migration](CODE_MIGRATION_PLAN.md)
- [All Phases Index](SWING_UI_ALL_PHASES_INDEX.md)
- [All Tasks](SWING_UI_ALL_TASKS.md)

---

**Ready to start Phase 0?**

Open `phases/phase-0/` to begin.

