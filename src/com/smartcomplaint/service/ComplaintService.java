package com.smartcomplaint.service;

import com.smartcomplaint.dao.ComplaintDAO;
import com.smartcomplaint.dao.DepartmentDAO;
import com.smartcomplaint.model.*;

import java.util.List;

public class ComplaintService {
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    public boolean registerComplaint(Complaint c) {
        if (c.getTitle() == null || c.getTitle().trim().isEmpty()) return false;
        if (c.getDescription() == null || c.getDescription().trim().isEmpty()) return false;
        if (c.getLocation() == null || c.getLocation().trim().isEmpty()) return false;

        // Auto-assign default department based on category if not assigned
        if (c.getAssignedDepartmentId() == null && c.getCategory() != null) {
            List<Department> depts = departmentDAO.getAllDepartments();
            for (Department d : depts) {
                if (d.getName().toLowerCase().contains(c.getCategory().name().toLowerCase()) ||
                    c.getCategory().getDefaultDepartment().toLowerCase().contains(d.getName().toLowerCase())) {
                    c.setAssignedDepartmentId(d.getId());
                    break;
                }
            }
            if (c.getAssignedDepartmentId() == null && !depts.isEmpty()) {
                c.setAssignedDepartmentId(depts.get(0).getId());
            }
        }

        return complaintDAO.createComplaint(c);
    }

    public Complaint trackComplaint(String complaintId) {
        if (complaintId == null || complaintId.trim().isEmpty()) return null;
        return complaintDAO.getComplaintById(complaintId.trim());
    }

    public List<Complaint> getUserComplaints(int userId) {
        return complaintDAO.getComplaintsByUserId(userId);
    }

    public List<Complaint> getOfficerComplaints(int officerUserId) {
        return complaintDAO.getComplaintsByOfficerId(officerUserId);
    }

    public List<Complaint> getDepartmentComplaints(int deptId) {
        return complaintDAO.getComplaintsByDepartmentId(deptId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintDAO.getAllComplaints();
    }

    public List<Complaint> getBreachedComplaints() {
        return complaintDAO.getBreachedComplaints();
    }

    public boolean updateStatus(String complaintId, ComplaintStatus newStatus, String remarks, User actionUser) {
        return complaintDAO.updateStatus(complaintId, newStatus, remarks, actionUser);
    }

    public boolean assignOfficerAndDepartment(String complaintId, int deptId, Integer officerUserId, String remarks, User actionUser) {
        return complaintDAO.assignOfficerAndDepartment(complaintId, deptId, officerUserId, remarks, actionUser);
    }

    public boolean submitFeedback(String complaintId, int rating, String feedback) {
        return complaintDAO.submitFeedback(complaintId, rating, feedback);
    }

    public void triggerSLACheck() {
        complaintDAO.refreshSLAStatuses();
    }
}
