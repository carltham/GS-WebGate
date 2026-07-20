package com.noprobit.analyzers.ui;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;

public class UITheme {
    // Color Palette
    public static final Color PRIMARY = new Color(33, 150, 243);
    public static final Color PRIMARY_DARK = new Color(25, 118, 210);
    public static final Color ACCENT = new Color(76, 175, 80);
    public static final Color ERROR = new Color(244, 67, 54);
    public static final Color BACKGROUND = new Color(245, 245, 245);
    public static final Color SURFACE = new Color(255, 255, 255);
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    public static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    public static final Color BORDER = new Color(224, 224, 224);

    // Fonts
    public static final Font FONT_REGULAR = new FontUIResource("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new FontUIResource("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_TITLE = new FontUIResource("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_HEADING = new FontUIResource("Segoe UI", Font.BOLD, 16);

    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Global settings
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("TextArea.font", FONT_REGULAR);
        UIManager.put("TabbedPane.font", FONT_BOLD);

        // Button styling
        UIManager.put("Button.background", PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.focus", PRIMARY_DARK);
        UIManager.put("Button.border", BORDER);

        // Component styling
        UIManager.put("TabbedPane.contentAreaColor", BACKGROUND);
        UIManager.put("TabbedPane.background", SURFACE);
        UIManager.put("TabbedPane.selectedBackground", SURFACE);
        UIManager.put("TabbedPane.focus", PRIMARY);

        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.alternateRowColor", new Color(250, 250, 250));
        UIManager.put("Table.gridColor", BORDER);

        UIManager.put("ScrollPane.background", BACKGROUND);
        UIManager.put("Viewport.background", BACKGROUND);

        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
    }
}
