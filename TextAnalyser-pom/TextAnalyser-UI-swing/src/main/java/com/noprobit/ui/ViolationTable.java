package com.noprobit.analyzers.ui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class ViolationTable extends JTable {
    private DefaultTableModel tableModel;
    private List<ReportController.ViolationData> data = new ArrayList<>();

    public ViolationTable() {
        String[] columns = {"Class", "Method", "Violation", "Severity"};
        tableModel = new DefaultTableModel(columns, 0);
        setModel(tableModel);
    }

    public void addViolation(String className, String method, String type, String severity) {
        ReportController.ViolationData violation = new ReportController.ViolationData(className, method, type, severity);
        data.add(violation);
        tableModel.addRow(new Object[]{className, method, type, severity});
    }

    public void clearViolations() {
        data.clear();
        tableModel.setRowCount(0);
    }

    public void setViolations(List<ReportController.ViolationData> violations) {
        clearViolations();
        for (ReportController.ViolationData v : violations) {
            addViolation(v.className, v.method, v.type, v.severity);
        }
    }

    public List<ReportController.ViolationData> getViolations() {
        return new ArrayList<>(data);
    }

    public void updateModel(String[] columns) {
        tableModel = new DefaultTableModel(columns, 0);
        setModel(tableModel);
    }
}
