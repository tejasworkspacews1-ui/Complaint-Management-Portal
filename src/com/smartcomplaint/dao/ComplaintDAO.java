package com.smartcomplaint.dao;

import com.smartcomplaint.model.*;
import com.smartcomplaint.util.IDGenerator;
import com.smartcomplaint.util.SLAEngine;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {

    public synchronized String getNextComplaintId() {
        int maxSeq = 1;
        String sql = "SELECT id FROM complaints ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.length() >= 8) {
                    try {
                        String numPart = id.substring(7); // CMP2026xxxx -> xxxx
                        int val = Integer.parseInt(numPart);
                        if (val >= maxSeq) {
                            maxSeq = val + 1;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return IDGenerator.generateComplaintId(maxSeq);
    }

    public boolean createComplaint(Complaint c) {
        if (c.getId() == null || c.getId().trim().isEmpty()) {
            c.setId(getNextComplaintId());
        }
        if (c.getCreatedAt() == null) {
            c.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        if (c.getSlaDeadline() == null) {
            c.setSlaDeadline(SLAEngine.calculateDeadline(c.getCreatedAt(), c.getPriority()));
        }
        c.setSlaStatus(SLAEngine.evaluateSLAStatus(c));

        String sql = "INSERT INTO complaints (id, tenant_id, user_id, title, description, category, sub_category, location, " +
                "priority, status, assigned_department_id, assigned_officer_id, attachment_path, is_anonymous, " +
                "created_at, sla_deadline, resolved_at, sla_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, com.smartcomplaint.service.AuthService.getCurrentUser() != null ? com.smartcomplaint.service.AuthService.getCurrentUser().getTenantId() : "DEFAULT");
            if (c.getUserId() != null) ps.setInt(3, c.getUserId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, c.getTitle());
            ps.setString(5, c.getDescription());
            ps.setString(6, c.getCategory() != null ? c.getCategory().name() : "OTHER");
            ps.setString(7, c.getSubCategory());
            ps.setString(8, c.getLocation());
            ps.setString(9, c.getPriority() != null ? c.getPriority().name() : "MEDIUM");
            ps.setString(10, c.getStatus() != null ? c.getStatus().name() : "SUBMITTED");
            if (c.getAssignedDepartmentId() != null) ps.setInt(11, c.getAssignedDepartmentId()); else ps.setNull(11, Types.INTEGER);
            if (c.getAssignedOfficerId() != null) ps.setInt(12, c.getAssignedOfficerId()); else ps.setNull(12, Types.INTEGER);
            ps.setString(13, c.getAttachmentPath());
            ps.setBoolean(14, c.isAnonymous());
            ps.setTimestamp(15, c.getCreatedAt());
            ps.setTimestamp(16, c.getSlaDeadline());
            ps.setTimestamp(17, c.getResolvedAt());
            ps.setString(18, c.getSlaStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                // Log creation
                ComplaintLog log = new ComplaintLog();
                log.setComplaintId(c.getId());
                log.setToStatus(c.getStatus());
                log.setActionRemarks("Complaint registered. SLA deadline: " + c.getPriority().getSlaLabel());
                log.setActionByUserId(c.getUserId());
                log.setActionByName(c.isAnonymous() ? "Anonymous Citizen" : (c.getCitizenName() != null ? c.getCitizenName() : "Citizen"));
                log.setActionByRole(Role.CITIZEN);
                new AuditLogDAO().addLog(log);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Complaint getComplaintById(String id) {
        refreshSLAStatuses();
        String sql = baseQuery() + " WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("c.") + " AND c.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapComplaint(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Complaint> getComplaintsByUserId(int userId) {
        refreshSLAStatuses();
        List<Complaint> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("c.") + " AND c.user_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapComplaint(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Complaint> getComplaintsByOfficerId(int officerUserId) {
        refreshSLAStatuses();
        List<Complaint> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("c.") + " AND c.assigned_officer_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, officerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapComplaint(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Complaint> getComplaintsByDepartmentId(int deptId) {
        refreshSLAStatuses();
        List<Complaint> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("c.") + " AND c.assigned_department_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapComplaint(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Complaint> getAllComplaints() {
        refreshSLAStatuses();
        List<Complaint> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("c.") + " ORDER BY c.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapComplaint(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Complaint> getBreachedComplaints() {
        refreshSLAStatuses();
        List<Complaint> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("c.") + " AND c.sla_status = 'SLA_BREACHED' AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED') ORDER BY c.sla_deadline ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapComplaint(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(String complaintId, ComplaintStatus newStatus, String remarks, User actionUser) {
        Complaint old = getComplaintById(complaintId);
        if (old == null) return false;

        Timestamp resolvedAt = null;
        SLAStatus slaStatus = old.getSlaStatus();

        if (newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.CLOSED) {
            resolvedAt = new Timestamp(System.currentTimeMillis());
            if (old.getSlaDeadline() != null && resolvedAt.after(old.getSlaDeadline())) {
                slaStatus = SLAStatus.SLA_BREACHED;
            } else {
                slaStatus = SLAStatus.RESOLVED;
            }
        }

        String sql = "UPDATE complaints SET status = ?, resolved_at = ?, sla_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setTimestamp(2, resolvedAt);
            ps.setString(3, slaStatus.name());
            ps.setString(4, complaintId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                // Log action
                ComplaintLog log = new ComplaintLog();
                log.setComplaintId(complaintId);
                log.setFromStatus(old.getStatus());
                log.setToStatus(newStatus);
                log.setActionRemarks(remarks != null && !remarks.trim().isEmpty() ? remarks : "Status updated to " + newStatus.getDisplayName());
                if (actionUser != null) {
                    log.setActionByUserId(actionUser.getId());
                    log.setActionByName(actionUser.getFullName());
                    log.setActionByRole(actionUser.getRole());
                } else {
                    log.setActionByName("System");
                    log.setActionByRole(Role.ADMIN);
                }
                new AuditLogDAO().addLog(log);

                // Notify citizen if registered
                if (old.getUserId() != null) {
                    Notification n = new Notification();
                    n.setUserId(old.getUserId());
                    n.setComplaintId(complaintId);
                    n.setTitle("Status Update: " + complaintId);
                    n.setMessage("Your complaint status has changed to: " + newStatus.getDisplayName() + (remarks != null && !remarks.isEmpty() ? " (" + remarks + ")" : ""));
                    new NotificationDAO().createNotification(n);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean assignOfficerAndDepartment(String complaintId, int deptId, Integer officerUserId, String remarks, User actionUser) {
        Complaint old = getComplaintById(complaintId);
        if (old == null) return false;

        ComplaintStatus nextStatus = (old.getStatus() == ComplaintStatus.SUBMITTED || old.getStatus() == ComplaintStatus.UNDER_REVIEW)
                ? ComplaintStatus.ASSIGNED : old.getStatus();

        String sql = "UPDATE complaints SET assigned_department_id = ?, assigned_officer_id = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            if (officerUserId != null) ps.setInt(2, officerUserId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, nextStatus.name());
            ps.setString(4, complaintId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                // Audit log
                ComplaintLog log = new ComplaintLog();
                log.setComplaintId(complaintId);
                log.setFromStatus(old.getStatus());
                log.setToStatus(nextStatus);
                log.setActionRemarks("Assigned to department ID: " + deptId + (officerUserId != null ? ", Officer User ID: " + officerUserId : "") + (remarks != null ? " - " + remarks : ""));
                if (actionUser != null) {
                    log.setActionByUserId(actionUser.getId());
                    log.setActionByName(actionUser.getFullName());
                    log.setActionByRole(actionUser.getRole());
                }
                new AuditLogDAO().addLog(log);

                // Notify officer
                if (officerUserId != null) {
                    Notification offNote = new Notification();
                    offNote.setUserId(officerUserId);
                    offNote.setComplaintId(complaintId);
                    offNote.setTitle("New Assignment: " + complaintId);
                    offNote.setMessage("Complaint '" + old.getTitle() + "' has been assigned to you. Priority: " + old.getPriority().getDisplayName());
                    new NotificationDAO().createNotification(offNote);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean submitFeedback(String complaintId, int rating, String feedback) {
        String sql = "UPDATE complaints SET citizen_rating = ?, citizen_feedback = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, feedback);
            ps.setString(3, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Automatic SLA evaluator that checks active complaints and sets SLA_BREACHED or DUE_SOON.
     */
    public synchronized void refreshSLAStatuses() {
        String sql = "SELECT id, created_at, sla_deadline, priority, status, sla_status, assigned_officer_id FROM complaints c " +
                "WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("c.") + " AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            long now = System.currentTimeMillis();
            List<String[]> updates = new ArrayList<>();

            while (rs.next()) {
                String id = rs.getString("id");
                Timestamp deadline = getSafeTimestamp(rs, "sla_deadline");
                String currentSla = rs.getString("sla_status");
                int officerId = rs.getInt("assigned_officer_id");

                if (deadline != null) {
                    String newSla = currentSla;
                    if (now > deadline.getTime()) {
                        newSla = "SLA_BREACHED";
                    } else if ((deadline.getTime() - now) < (12 * 3600 * 1000L)) {
                        newSla = "DUE_SOON";
                    } else {
                        newSla = "WITHIN_SLA";
                    }

                    if (!newSla.equals(currentSla)) {
                        updates.add(new String[]{id, newSla, currentSla, String.valueOf(officerId)});
                    }
                }
            }

            for (String[] upd : updates) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE complaints SET sla_status = ? WHERE id = ?")) {
                    ps.setString(1, upd[1]);
                    ps.setString(2, upd[0]);
                    ps.executeUpdate();
                }

                if ("SLA_BREACHED".equals(upd[1]) && !"SLA_BREACHED".equals(upd[2])) {
                    // Log breach event
                    ComplaintLog breachLog = new ComplaintLog();
                    breachLog.setComplaintId(upd[0]);
                    breachLog.setToStatus(ComplaintStatus.IN_PROGRESS);
                    breachLog.setActionRemarks("ALERT: SLA Resolution Deadline has been BREACHED.");
                    breachLog.setActionByName("SLA Monitor Daemon");
                    breachLog.setActionByRole(Role.ADMIN);
                    new AuditLogDAO().addLog(breachLog);

                    // Notify assigned officer
                    try {
                        int offId = Integer.parseInt(upd[3]);
                        if (offId > 0) {
                            Notification n = new Notification();
                            n.setUserId(offId);
                            n.setComplaintId(upd[0]);
                            n.setTitle("SLA BREACH ALERT: " + upd[0]);
                            n.setMessage("URGENT: Complaint " + upd[0] + " has exceeded its SLA resolution deadline!");
                            new NotificationDAO().createNotification(n);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            // Silently handle if tables not ready yet
        }
    }

    private String baseQuery() {
        return "SELECT c.*, " +
                "u.full_name as citizen_name, u.email as citizen_email, u.phone as citizen_phone, " +
                "d.name as dept_name, " +
                "off_u.full_name as officer_name " +
                "FROM complaints c " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN departments d ON c.assigned_department_id = d.id " +
                "LEFT JOIN users off_u ON c.assigned_officer_id = off_u.id";
    }

    private Complaint mapComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getString("id"));
        try { c.setTenantId(rs.getString("tenant_id")); } catch (Exception ignored) {}
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) c.setUserId(uid);
        c.setCitizenName(rs.getString("citizen_name"));
        c.setCitizenEmail(rs.getString("citizen_email"));
        c.setCitizenPhone(rs.getString("citizen_phone"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(ComplaintCategory.fromString(rs.getString("category")));
        c.setSubCategory(rs.getString("sub_category"));
        c.setLocation(rs.getString("location"));
        c.setPriority(ComplaintPriority.fromString(rs.getString("priority")));
        c.setStatus(ComplaintStatus.fromString(rs.getString("status")));

        int deptId = rs.getInt("assigned_department_id");
        if (!rs.wasNull()) c.setAssignedDepartmentId(deptId);
        c.setAssignedDepartmentName(rs.getString("dept_name"));

        int offId = rs.getInt("assigned_officer_id");
        if (!rs.wasNull()) c.setAssignedOfficerId(offId);
        c.setAssignedOfficerName(rs.getString("officer_name"));

        c.setAttachmentPath(rs.getString("attachment_path"));
        c.setAnonymous(rs.getBoolean("is_anonymous"));
        c.setCreatedAt(getSafeTimestamp(rs, "created_at"));
        c.setSlaDeadline(getSafeTimestamp(rs, "sla_deadline"));
        c.setResolvedAt(getSafeTimestamp(rs, "resolved_at"));
        c.setSlaStatus(SLAStatus.fromString(rs.getString("sla_status")));

        int rating = rs.getInt("citizen_rating");
        if (!rs.wasNull()) c.setCitizenRating(rating);
        c.setCitizenFeedback(rs.getString("citizen_feedback"));

        return c;
    }

    public static Timestamp getSafeTimestamp(ResultSet rs, String col) {
        try {
            Timestamp ts = rs.getTimestamp(col);
            if (ts != null) return ts;
        } catch (Exception ignored) {}
        try {
            String str = rs.getString(col);
            if (str != null && !str.trim().isEmpty()) {
                try {
                    long ms = Long.parseLong(str.trim());
                    return new Timestamp(ms);
                } catch (NumberFormatException nfe) {
                    try {
                        return Timestamp.valueOf(str.trim());
                    } catch (Exception ex) {
                        return new Timestamp(System.currentTimeMillis());
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
