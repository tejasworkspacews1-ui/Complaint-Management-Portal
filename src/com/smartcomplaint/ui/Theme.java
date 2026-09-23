package com.smartcomplaint.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Theme {
    // Colors
    public static final Color PRIMARY = new Color(37, 99, 235);         // Blue 600
    public static final Color PRIMARY_DARK = new Color(30, 64, 175);    // Blue 800
    public static final Color PRIMARY_LIGHT = new Color(219, 234, 254); // Blue 100
    
    public static final Color ACCENT = new Color(14, 165, 233);         // Sky 500
    public static final Color SUCCESS = new Color(16, 185, 129);        // Emerald 500
    public static final Color SUCCESS_LIGHT = new Color(209, 250, 229);  // Emerald 100
    
    public static final Color WARNING = new Color(245, 158, 11);        // Amber 500
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);  // Amber 100
    
    public static final Color DANGER = new Color(239, 68, 68);          // Red 500
    public static final Color DANGER_LIGHT = new Color(254, 226, 226);   // Red 100
    public static final Color DANGER_DARK = new Color(185, 28, 28);     // Red 700

    public static final Color BG_MAIN = new Color(248, 250, 252);       // Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240);  // Slate 200
    public static final Color BORDER_HOVER = new Color(148, 163, 184);  // Slate 400

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);     // Slate 900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);// Slate 500
    public static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    public static final Color SIDEBAR_BG = new Color(15, 23, 42);       // Slate 900
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);    // Slate 800
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);  // Blue 600

    // Fonts
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_STAT_VAL = new Font("Segoe UI", Font.BOLD, 26);

    /**
     * Initializes FlatLaf Look and Feel if available, or Nimbus / System as fallback.
     */
    public static void setupLookAndFeel() {
        try {
            // Attempt FlatLaf Light
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("TableHeader.font", FONT_HEADER);
            UIManager.put("Table.font", FONT_BODY);
        } catch (Exception e) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // System L&F fallback
            }
        }
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        );
    }
}
