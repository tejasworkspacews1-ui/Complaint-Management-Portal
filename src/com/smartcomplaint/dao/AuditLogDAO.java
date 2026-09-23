package com.smartcomplaint.dao;

import com.smartcomplaint.model.ComplaintLog;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public boolean addLog(ComplaintLog log) {
        String sql = "INSERT INTO complaint_logs (complaint_id, from_status, to_status, action_remarks, action_by_user_id, action_by_name, action_by_role, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getComplaintId());
            ps.setString(2, log.getFromStatus() != null ? log.getFromStatus().name() : null);
            ps.setString(3, log.getToStatus().name());
            ps.setString(4, log.getActionRemarks());
            if (log.getActionByUserId() != null) ps.setInt(5, log.getActionByUserId()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, log.getActionByName());
            ps.setString(7, log.getActionByRole() != null ? log.getActionByRole().name() : "SYSTEM");
            ps.setTimestamp(8, log.getCreatedAt() != null ? log.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ComplaintLog> getLogsForComplaint(String complaintId) {
        List<ComplaintLog> list = new ArrayList<>();
        String sql = "SELECT * FROM complaint_logs WHERE complaint_id = ? ORDER BY created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ComplaintLog l = new ComplaintLog();
                    l.setId(rs.getInt("id"));
                    l.setComplaintId(rs.getString("complaint_id"));
                    String from = rs.getString("from_status");
                    if (from != null) l.setFromStatus(ComplaintStatus.fromString(from));
                    l.setToStatus(ComplaintStatus.fromString(rs.getString("to_status")));
                    l.setActionRemarks(rs.getString("action_remarks"));
                    int uid = rs.getInt("action_by_user_id");
                    if (!rs.wasNull()) l.setActionByUserId(uid);
                    l.setActionByName(rs.getString("action_by_name"));
                    String r = rs.getString("action_by_role");
                    if (r != null) l.setActionByRole(Role.fromString(r));
                    l.setCreatedAt(ComplaintDAO.getSafeTimestamp(rs, "created_at"));
                    list.add(l);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
