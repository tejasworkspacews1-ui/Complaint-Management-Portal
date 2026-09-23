package com.smartcomplaint.model;

import java.sql.Timestamp;

public class ComplaintLog {
    private int id;
    private String complaintId;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private String actionRemarks;
    private Integer actionByUserId;
    private String actionByName;
    private Role actionByRole;
    private Timestamp createdAt;

    public ComplaintLog() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public ComplaintStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ComplaintStatus fromStatus) { this.fromStatus = fromStatus; }

    public ComplaintStatus getToStatus() { return toStatus; }
    public void setToStatus(ComplaintStatus toStatus) { this.toStatus = toStatus; }

    public String getActionRemarks() { return actionRemarks; }
    public void setActionRemarks(String actionRemarks) { this.actionRemarks = actionRemarks; }

    public Integer getActionByUserId() { return actionByUserId; }
    public void setActionByUserId(Integer actionByUserId) { this.actionByUserId = actionByUserId; }

    public String getActionByName() { return actionByName; }
    public void setActionByName(String actionByName) { this.actionByName = actionByName; }

    public Role getActionByRole() { return actionByRole; }
    public void setActionByRole(Role actionByRole) { this.actionByRole = actionByRole; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
