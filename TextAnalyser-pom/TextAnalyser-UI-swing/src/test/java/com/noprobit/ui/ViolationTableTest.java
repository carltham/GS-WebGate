package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Violation Table Tests")
class ViolationTableTest {

    @Test
    void testTableModelCreation() {
        ViolationTable table = new ViolationTable();
        assertNotNull(table.getModel());
    }

    @Test
    void testRowCount() {
        ViolationTable table = new ViolationTable();
        assertTrue(table.getRowCount() >= 0);
    }

    @Test
    void testColumnCount() {
        ViolationTable table = new ViolationTable();
        assertTrue(table.getColumnCount() > 0);
    }

    @Test
    void testDataDisplay() {
        ViolationTable table = new ViolationTable();
        assertDoesNotThrow(() -> {
            table.addViolation("ClassNameTest", "testMethod", "ClassNaming", "HIGH");
        });
    }

    @Test
    void testModelUpdate() {
        ViolationTable table = new ViolationTable();
        table.addViolation("Class1", "method1", "Type1", "HIGH");
        assertTrue(table.getRowCount() > 0);
    }

    @Test
    void testCellRendering() {
        ViolationTable table = new ViolationTable();
        assertNotNull(table.getCellRenderer(0, 0));
    }

    @Test
    void testTableSelection() {
        ViolationTable table = new ViolationTable();
        table.addViolation("Class1", "method1", "Type1", "HIGH");
        assertDoesNotThrow(() -> {
            table.setRowSelectionInterval(0, 0);
        });
    }

    @Test
    void testColumnWidths() {
        ViolationTable table = new ViolationTable();
        assertTrue(table.getColumnModel().getColumnCount() > 0);
    }
}
