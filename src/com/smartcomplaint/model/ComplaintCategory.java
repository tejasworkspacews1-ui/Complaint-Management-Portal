package com.smartcomplaint.model;

import java.util.Arrays;
import java.util.List;

public enum ComplaintCategory {
    WATER("Water Supply & Drainage", "Water Department", Arrays.asList(
            "No Water Supply", "Contaminated/Dirty Water", "Pipe Leakage/Burst", "Low Pressure", "Water Meter Issue", "Drainage Overflow"
    )),
    ELECTRICITY("Electricity & Power", "Electricity Department", Arrays.asList(
            "Frequent Power Outage", "Flickering/Low Voltage", "Faulty Electric Pole/Wire", "Transformer Issue", "Electric Meter Fault", "Street Light Not Working"
    )),
    ROADS("Roads & Traffic", "Roads & Highway Authority", Arrays.asList(
            "Potholes & Broken Road", "Traffic Signal Failure", "Speed Breaker Needed", "Waterlogging on Road", "Encroachment on Road", "Missing Signboards"
    )),
    GARBAGE("Garbage & Sanitation", "Sanitation Department", Arrays.asList(
            "Garbage Not Collected", "Overflowing Community Dumpster", "Illegal Waste Dumping", "Dead Animal Removal", "Public Toilet Cleaning Needed"
    )),
    PUBLIC_SAFETY("Public Safety & Security", "Public Safety Department", Arrays.asList(
            "Unsafe/Dark Alley", "Anti-Social Elements", "Open Manhole/Hazard", "Stray Dog Nuisance", "Fire Hazard"
    )),
    TRANSPORT("Public Transport", "Transport Department", Arrays.asList(
            "Bus Overcrowding/Delays", "Rash Driving by Public Transport", "Bus Stop Shelter Damaged", "Ticket Overcharging"
    )),
    INTERNET("Internet & Telecom Infrastructure", "Telecom Department", Arrays.asList(
            "Fiber Optic Cable Cut", "Cell Tower Interference", "Public WiFi Outage"
    )),
    INFRASTRUCTURE("Public Infrastructure", "Infrastructure & Public Works", Arrays.asList(
            "Damaged Bridge/Flyover", "Cracked Footpath", "Park Maintenance", "Community Hall Damage"
    )),
    OTHER("Other Complaints", "General Grievances", Arrays.asList(
            "Noise Pollution", "Illegal Construction", "Administrative Delay", "General Grievance"
    ));

    private final String displayName;
    private final String defaultDepartment;
    private final List<String> subCategories;

    ComplaintCategory(String displayName, String defaultDepartment, List<String> subCategories) {
        this.displayName = displayName;
        this.defaultDepartment = defaultDepartment;
        this.subCategories = subCategories;
    }

    public String getDisplayName() { return displayName; }
    public String getDefaultDepartment() { return defaultDepartment; }
    public List<String> getSubCategories() { return subCategories; }

    public static ComplaintCategory fromString(String text) {
        if (text == null) return OTHER;
        for (ComplaintCategory c : ComplaintCategory.values()) {
            if (c.name().equalsIgnoreCase(text) || c.displayName.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return OTHER;
    }
}
