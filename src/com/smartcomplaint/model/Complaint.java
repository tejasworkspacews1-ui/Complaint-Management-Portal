package com.smartcomplaint.model;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

public class Complaint {
    private String id; // e.g. CMP20260001
    private String tenantId;
    private Integer userId;
    private String citizenName;
    private String citizenEmail;
    private String citizenPhone;
    private String title;
    private String description;
    private ComplaintCategory category;
    private String subCategory;
    private String location;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private Integer assignedDepartmentId;
    private String assignedDepartmentName;
    private Integer assignedOfficerId;
    private String assignedOfficerName;
    private String attachmentPath;
    private boolean isAnonymous;
    private Timestamp createdAt;
    private Timestamp slaDeadline;
    private Timestamp resolvedAt;
    private SLAStatus slaStatus;
    private Integer citizenRating;
    private String citizenFeedback;

    public Complaint() {
        this.status = ComplaintStatus.SUBMITTED;
        this.priority = ComplaintPriority.MEDIUM;
        this.slaStatus = SLAStatus.WITHIN_SLA;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getCitizenEmail() { return citizenEmail; }
    public void setCitizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; }

    public String getCitizenPhone() { return citizenPhone; }
    public void setCitizenPhone(String citizenPhone) { this.citizenPhone = citizenPhone; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public Integer getAssignedDepartmentId() { return assignedDepartmentId; }
    public void setAssignedDepartmentId(Integer assignedDepartmentId) { this.assignedDepartmentId = assignedDepartmentId; }

    public String getAssignedDepartmentName() { return assignedDepartmentName; }
    public void setAssignedDepartmentName(String assignedDepartmentName) { this.assignedDepartmentName = assignedDepartmentName; }

    public Integer getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(Integer assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }

    public String getAssignedOfficerName() { return assignedOfficerName; }
    public void setAssignedOfficerName(String assignedOfficerName) { this.assignedOfficerName = assignedOfficerName; }

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(Timestamp slaDeadline) { this.slaDeadline = slaDeadline; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public SLAStatus getSlaStatus() { return slaStatus; }
    public void setSlaStatus(SLAStatus slaStatus) { this.slaStatus = slaStatus; }

    public Integer getCitizenRating() { return citizenRating; }
    public void setCitizenRating(Integer citizenRating) { this.citizenRating = citizenRating; }

    public String getCitizenFeedback() { return citizenFeedback; }
    public void setCitizenFeedback(String citizenFeedback) { this.citizenFeedback = citizenFeedback; }

    /**
     * Calculates remaining time or overdue duration against SLA deadline
     */
    public String getRemainingTimeFormatted() {
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) {
            return "Resolved (" + (slaStatus == SLAStatus.SLA_BREACHED ? "After SLA" : "On Time") + ")";
        }
        if (slaDeadline == null) return "N/A";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = slaDeadline.toLocalDateTime();

        Duration duration = Duration.between(now, deadline);
        long totalHours = duration.toHours();
        long minutes = Math.abs(duration.toMinutes() % 60);

        if (duration.isNegative()) {
            long overdueHours = Math.abs(totalHours);
            long days = overdueHours / 24;
            long remHours = overdueHours % 24;
            if (days > 0) {
                return "Breached by " + days + "d " + remHours + "h";
            }
            return "Breached by " + overdueHours + "h " + minutes + "m";
        } else {
            long days = totalHours / 24;
            long remHours = totalHours % 24;
            if (days > 0) {
                return days + "d " + remHours + "h remaining";
            }
            return remHours + "h " + minutes + "m remaining";
        }
    }

    public boolean isBreached() {
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) {
            return slaStatus == SLAStatus.SLA_BREACHED;
        }
        if (slaDeadline == null) return false;
        return System.currentTimeMillis() > slaDeadline.getTime();
    }
}
