package com.smartcomplaint.dao;

import com.smartcomplaint.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT d.*, u.full_name as head_name, " +
                "(SELECT COUNT(*) FROM complaints c WHERE c.assigned_department_id = d.id AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')) as active_count " +
                "FROM departments d " +
                "LEFT JOIN users u ON d.head_officer_id = u.id " +
                "WHERE " + com.smartcomplaint.service.AuthService.getTenantWhereClause("d.") + " " +
                "ORDER BY d.name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Department d = new Department();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setCode(rs.getString("code"));
                d.setDescription(rs.getString("description"));
                int headId = rs.getInt("head_officer_id");
                if (!rs.wasNull()) d.setHeadOfficerId(headId);
                d.setHeadOfficerName(rs.getString("head_name"));
                d.setActiveComplaintsCount(rs.getInt("active_count"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Department getDepartmentById(int id) {
        String sql = "SELECT * FROM departments WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Department d = new Department();
                    d.setId(rs.getInt("id"));
                    d.setName(rs.getString("name"));
                    d.setCode(rs.getString("code"));
                    d.setDescription(rs.getString("description"));
                    return d;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createDepartment(Department dept) {
        String sql = "INSERT INTO departments (name, code, description) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getCode());
            ps.setString(3, dept.getDescription());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) dept.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
