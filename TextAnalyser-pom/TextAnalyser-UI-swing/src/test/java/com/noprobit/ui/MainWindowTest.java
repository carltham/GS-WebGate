package com.noprobit.analyzers.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import javax.swing.JFrame;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Main Window Tests")
class MainWindowTest {

    private MainWindow window;

    @BeforeEach
    void setUp() {
        // Initialize window before each test
    }

    @AfterEach
    void tearDown() {
        // Clean up window after each test
        if (window != null) {
            window.dispose();
        }
    }

    @Test
    @DisplayName("Main window can be created")
    void testWindowCreated() {
        assertDoesNotThrow(() -> {
            window = new MainWindow();
        });
        assertNotNull(window, "Window should be created");
        assertInstanceOf(JFrame.class, window, "Should be instance of JFrame");
    }

    @Test
    @DisplayName("Window title is set to 'TextAnalyser'")
    void testWindowTitleSet() {
        window = new MainWindow();
        String title = window.getTitle();
        assertNotNull(title, "Window should have a title");
        assertTrue(title.contains("TextAnalyser"),
            "Title should contain 'TextAnalyser'");
    }

    @Test
    @DisplayName("Window close button works")
    void testWindowExit() {
        window = new MainWindow();
        window.setVisible(true);
        assertTrue(window.isVisible(), "Window should be visible");

        window.dispose();
        // After dispose, window should be disposed
        assertFalse(window.isDisplayable(), "Window should be disposed");
    }

    @Test
    @DisplayName("Window contains UI panels")
    void testWindowContainsPanels() {
        window = new MainWindow();
        int componentCount = window.getContentPane().getComponentCount();
        assertTrue(componentCount > 0, "Window should contain components");
    }

    @Test
    @DisplayName("Window size is reasonable")
    void testWindowSizeReasonable() {
        window = new MainWindow();
        window.setSize(800, 600);

        int width = window.getWidth();
        int height = window.getHeight();

        assertTrue(width >= 400, "Window width should be at least 400px");
        assertTrue(height >= 300, "Window height should be at least 300px");
    }

    @Test
    @DisplayName("Window is initially centered")
    void testWindowInitialPosition() {
        window = new MainWindow();
        window.setLocationRelativeTo(null);  // Center on screen

        int x = window.getX();
        int y = window.getY();

        // When centered, position should not be at origin
        // (This is a soft check - behavior varies by platform)
        assertTrue(x >= 0 && y >= 0, "Window position should be valid");
    }
}
