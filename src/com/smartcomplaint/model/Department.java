package com.smartcomplaint.model;

public class Department {
    private int id;
    private String name;
    private String code;
    private String description;
    private Integer headOfficerId;
    private String headOfficerName;
    private int activeComplaintsCount;

    public Department() {}

    public Department(int id, String name, String code, String description) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getHeadOfficerId() { return headOfficerId; }
    public void setHeadOfficerId(Integer headOfficerId) { this.headOfficerId = headOfficerId; }

    public String getHeadOfficerName() { return headOfficerName; }
    public void setHeadOfficerName(String headOfficerName) { this.headOfficerName = headOfficerName; }

    public int getActiveComplaintsCount() { return activeComplaintsCount; }
    public void setActiveComplaintsCount(int activeComplaintsCount) { this.activeComplaintsCount = activeComplaintsCount; }

    @Override
    public String toString() {
        return name;
    }
}
