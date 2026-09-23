package com.smartcomplaint.model;

import java.awt.Color;

public enum SLAStatus {
    WITHIN_SLA("Within SLA", new Color(16, 185, 129), new Color(209, 250, 229)),       // Green
    DUE_SOON("Due Soon (<12h)", new Color(217, 119, 6), new Color(254, 243, 199)),     // Amber
    SLA_BREACHED("SLA Breached", new Color(220, 38, 38), new Color(254, 226, 226)),    // Red
    RESOLVED("Resolved on Time", new Color(5, 150, 105), new Color(209, 250, 229));   // Emerald

    private final String displayName;
    private final Color fgColor;
    private final Color bgColor;

    SLAStatus(String displayName, Color fgColor, Color bgColor) {
        this.displayName = displayName;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
    }

    public String getDisplayName() { return displayName; }
    public Color getFgColor() { return fgColor; }
    public Color getBgColor() { return bgColor; }

    public static SLAStatus fromString(String text) {
        if (text == null) return WITHIN_SLA;
        for (SLAStatus s : SLAStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return WITHIN_SLA;
    }
}
