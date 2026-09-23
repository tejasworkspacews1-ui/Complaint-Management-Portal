package com.smartcomplaint.model;

/**
 * User roles in the Complaint Portal.
 */
public enum Role {
    CITIZEN("Citizen / User"),
    OFFICER("Complaint Officer"),
    MODERATOR("Space Moderator"),
    ADMIN("Super Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(text) || role.displayName.equalsIgnoreCase(text)) {
                return role;
            }
        }
        return CITIZEN;
    }
}
