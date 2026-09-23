package com.smartcomplaint.model;

public class Officer {
    private int id;
    private int userId;
    private int departmentId;
    private String departmentName;
    private String designation;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phone;
    private int activeComplaints;
    private int resolvedComplaints;

    public Officer() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getActiveComplaints() { return activeComplaints; }
    public void setActiveComplaints(int activeComplaints) { this.activeComplaints = activeComplaints; }

    public int getResolvedComplaints() { return resolvedComplaints; }
    public void setResolvedComplaints(int resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }

    @Override
    public String toString() {
        return fullName + " (" + designation + " - " + (departmentName != null ? departmentName : "") + ")";
    }
}
