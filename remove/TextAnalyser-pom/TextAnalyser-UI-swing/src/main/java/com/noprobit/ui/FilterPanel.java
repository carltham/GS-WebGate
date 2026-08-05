package com.noprobit.analyzers.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.FlowLayout;

public class FilterPanel extends JPanel {
    private JComboBox<String> typeFilter;
    private JComboBox<String> severityFilter;
    private JTextField classFilter;

    public FilterPanel() {
        setLayout(new FlowLayout());

        add(new JLabel("Type:"));
        typeFilter = new JComboBox<>(new String[]{"All", "ClassNaming", "MethodNaming", "Import"});
        add(typeFilter);

        add(new JLabel("Severity:"));
        severityFilter = new JComboBox<>(new String[]{"All", "HIGH", "MEDIUM", "LOW"});
        add(severityFilter);

        add(new JLabel("Class:"));
        classFilter = new JTextField(10);
        add(classFilter);
    }

    public String getSelectedType() {
        return (String) typeFilter.getSelectedItem();
    }

    public String getSelectedSeverity() {
        return (String) severityFilter.getSelectedItem();
    }

    public String getClassFilter() {
        return classFilter.getText();
    }
}
