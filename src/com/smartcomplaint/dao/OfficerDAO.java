package com.smartcomplaint.dao;

import com.smartcomplaint.model.Officer;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OfficerDAO {

    public List<Officer> getAllOfficers() {
        List<Officer> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name, u.email, u.phone, d.name as dept_name, " +
                "(SELECT COUNT(*) FROM complaints c WHERE c.assigned_officer_id = o.user_id AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')) as active_cnt, " +
                "(SELECT COUNT(*) FROM complaints c WHERE c.assigned_officer_id = o.user_id AND c.status IN ('RESOLVED', 'CLOSED')) as resolved_cnt " +
                "FROM officers o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN departments d ON o.department_id = d.id " +
                "WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("u.") + " " +
                "ORDER BY u.full_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapOfficer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Officer> getOfficersByDepartment(int departmentId) {
        List<Officer> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name, u.email, u.phone, d.name as dept_name, " +
                "(SELECT COUNT(*) FROM complaints c WHERE c.assigned_officer_id = o.user_id AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')) as active_cnt, " +
                "(SELECT COUNT(*) FROM complaints c WHERE c.assigned_officer_id = o.user_id AND c.status IN ('RESOLVED', 'CLOSED')) as resolved_cnt " +
                "FROM officers o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN departments d ON o.department_id = d.id " +
                "WHERE o.department_id = ? " +
                "ORDER BY u.full_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOfficer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Officer getOfficerByUserId(int userId) {
        String sql = "SELECT o.*, u.full_name, u.email, u.phone, d.name as dept_name, 0 as active_cnt, 0 as resolved_cnt " +
                "FROM officers o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN departments d ON o.department_id = d.id " +
                "WHERE o.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOfficer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createOfficer(User user, String plainPassword, int departmentId, String designation, String employeeCode) {
        UserDAO userDAO = new UserDAO();
        user.setRole(Role.OFFICER);
        user.setDepartmentId(departmentId);
        boolean userCreated = userDAO.createUser(user, plainPassword);
        if (!userCreated) return false;

        String sql = "INSERT INTO officers (user_id, department_id, designation, employee_code) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            ps.setInt(2, departmentId);
            ps.setString(3, designation);
            ps.setString(4, employeeCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Officer mapOfficer(ResultSet rs) throws SQLException {
        Officer o = new Officer();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setDepartmentId(rs.getInt("department_id"));
        o.setDepartmentName(rs.getString("dept_name"));
        o.setDesignation(rs.getString("designation"));
        o.setEmployeeCode(rs.getString("employee_code"));
        o.setFullName(rs.getString("full_name"));
        o.setEmail(rs.getString("email"));
        o.setPhone(rs.getString("phone"));
        o.setActiveComplaints(rs.getInt("active_cnt"));
        o.setResolvedComplaints(rs.getInt("resolved_cnt"));
        return o;
    }
}
