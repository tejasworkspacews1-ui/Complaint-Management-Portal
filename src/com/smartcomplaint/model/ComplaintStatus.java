package com.smartcomplaint.model;

import java.awt.Color;

public enum ComplaintStatus {
    SUBMITTED("Submitted", 0, new Color(59, 130, 246)),          // Blue
    UNDER_REVIEW("Under Review", 1, new Color(139, 92, 246)),     // Purple
    ASSIGNED("Assigned", 2, new Color(14, 165, 233)),            // Sky
    IN_PROGRESS("In Progress", 3, new Color(245, 158, 11)),       // Amber
    RESOLVED("Resolved", 4, new Color(16, 185, 129)),            // Green
    CLOSED("Closed", 5, new Color(100, 116, 139)),               // Slate
    REJECTED("Rejected", -1, new Color(239, 68, 68));            // Red

    private final String displayName;
    private final int stepIndex; // 0 to 5 for visual tracker (-1 for rejected)
    private final Color color;

    ComplaintStatus(String displayName, int stepIndex, Color color) {
        this.displayName = displayName;
        this.stepIndex = stepIndex;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public int getStepIndex() { return stepIndex; }
    public Color getColor() { return color; }

    public static ComplaintStatus fromString(String text) {
        if (text == null) return SUBMITTED;
        for (ComplaintStatus s : ComplaintStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return SUBMITTED;
    }
}
