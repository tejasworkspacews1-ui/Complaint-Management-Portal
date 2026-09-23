package com.smartcomplaint.model;

import java.awt.Color;

public enum ComplaintPriority {
    EMERGENCY("Emergency", 24, "24 Hours", new Color(220, 38, 38)),     // Red
    HIGH("High", 48, "2 Days (48h)", new Color(234, 88, 12)),           // Orange
    MEDIUM("Medium", 120, "5 Days (120h)", new Color(202, 138, 4)),     // Amber/Yellow
    LOW("Low", 168, "7 Days (168h)", new Color(16, 149, 193));          // Cyan/Blue

    private final String displayName;
    private final int slaHours;
    private final String slaLabel;
    private final Color color;

    ComplaintPriority(String displayName, int slaHours, String slaLabel, Color color) {
        this.displayName = displayName;
        this.slaHours = slaHours;
        this.slaLabel = slaLabel;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public int getSlaHours() { return slaHours; }
    public String getSlaLabel() { return slaLabel; }
    public Color getColor() { return color; }

    public static ComplaintPriority fromString(String text) {
        if (text == null) return MEDIUM;
        for (ComplaintPriority p : ComplaintPriority.values()) {
            if (p.name().equalsIgnoreCase(text) || p.displayName.equalsIgnoreCase(text)) {
                return p;
            }
        }
        return MEDIUM;
    }
}
