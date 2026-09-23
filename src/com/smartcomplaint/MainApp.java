package com.smartcomplaint;

import com.smartcomplaint.ui.MainFrame;
import com.smartcomplaint.ui.Theme;

import javax.swing.*;

/**
 * Main Entry point for Smart Complaint Registration & Management Portal.
 */
public class MainApp {
    public static void main(String[] args) {
        // Initialize Look and Feel
        Theme.setupLookAndFeel();

        // Launch UI in Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
