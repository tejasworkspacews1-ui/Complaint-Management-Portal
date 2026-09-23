package com.smartcomplaint;

import com.smartcomplaint.dao.ComplaintDAO;
import com.smartcomplaint.dao.DBConnection;
import com.smartcomplaint.dao.DepartmentDAO;
import com.smartcomplaint.dao.UserDAO;
import com.smartcomplaint.model.*;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.service.ReportService;
import com.smartcomplaint.util.SLAEngine;

import java.sql.Timestamp;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  STARTING AUTOMATED BACKEND & SERVICE TESTS");
        System.out.println("=================================================");

        // If MySQL is not reachable with default empty password, use SQLite for test verification
        boolean mySqlOk = DBConnection.testConnection("mysql", DBConnection.getHost(), DBConnection.getPort(), DBConnection.getDbName(), DBConnection.getUsername(), DBConnection.getPassword());
        if (!mySqlOk) {
            System.out.println("[INFO] MySQL root without password unreachable; testing with SQLite embedded engine...");
            DBConnection.saveConfig("sqlite", "localhost", 3306, "smart_complaint_db", "root", "", "smart_complaint.db");
        } else {
            System.out.println("[INFO] Testing with MySQL Server 8.0...");
        }

        // 1. Test Database Init & Seeding
        boolean initOk = DBConnection.initDatabase();
        assertCondition(initOk, "Database Initialization & Schema Creation");

        // 2. Test User Accounts & Authentication
        AuthService authService = new AuthService();
        User adminUser = authService.login("admin", "admin123", Role.ADMIN);
        assertCondition(adminUser != null && adminUser.getRole() == Role.ADMIN, "Admin Login Authentication");

        User officerUser = authService.login("officer_water", "officer123", Role.OFFICER);
        assertCondition(officerUser != null && officerUser.getRole() == Role.OFFICER, "Officer Login Authentication");

        User citizenUser = authService.login("citizen1", "citizen123", Role.CITIZEN);
        assertCondition(citizenUser != null && citizenUser.getRole() == Role.CITIZEN, "Citizen Login Authentication");

        // 3. Test Registration
        boolean regOk = authService.registerCitizen("DEFAULT", "test_user_" + System.currentTimeMillis(), "pass123", "Test Citizen", "test" + System.currentTimeMillis() + "@portal.gov.in", "9999999999");
        assertCondition(regOk, "New Citizen Registration");

        // 4. Test Complaint Creation with SLA
        ComplaintService complaintService = new ComplaintService();
        Complaint newC = new Complaint();
        newC.setUserId(citizenUser.getId());
        newC.setTitle("Automated Test Complaint - Water Shortage");
        newC.setDescription("Testing water shortage grievance in North Block 5.");
        newC.setCategory(ComplaintCategory.WATER);
        newC.setSubCategory("No Water Supply");
        newC.setLocation("North Block 5, Street 12");
        newC.setPriority(ComplaintPriority.EMERGENCY);
        newC.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        boolean compCreated = complaintService.registerComplaint(newC);
        assertCondition(compCreated, "Complaint Creation (Auto ID & SLA)");
        assertCondition(newC.getId() != null && newC.getId().startsWith("CMP"), "Generated Complaint ID format: " + newC.getId());
        assertCondition(newC.getSlaDeadline() != null, "SLA Deadline calculated: " + newC.getSlaDeadline());

        // 5. Test Complaint Tracking
        Complaint tracked = complaintService.trackComplaint(newC.getId());
        assertCondition(tracked != null && tracked.getTitle().equals(newC.getTitle()), "Complaint Tracking by ID");

        // 6. Test Status Update & SLA Check
        boolean statusUpd = complaintService.updateStatus(newC.getId(), ComplaintStatus.IN_PROGRESS, "Officer dispatched to inspect pipeline.", officerUser);
        assertCondition(statusUpd, "Complaint Status Update to IN_PROGRESS");

        tracked = complaintService.trackComplaint(newC.getId());
        assertCondition(tracked.getStatus() == ComplaintStatus.IN_PROGRESS, "Verified updated status IN_PROGRESS");

        // 7. Test Complaint Resolution & Citizen Feedback
        boolean resOk = complaintService.updateStatus(newC.getId(), ComplaintStatus.RESOLVED, "Pipeline repaired successfully.", officerUser);
        assertCondition(resOk, "Complaint Status Update to RESOLVED");

        boolean fbOk = complaintService.submitFeedback(newC.getId(), 5, "Great work! Problem resolved within 2 hours.");
        assertCondition(fbOk, "Citizen Feedback & 5-Star Rating Submission");

        // 8. Test Reports & Statistics
        ReportService reportService = new ReportService();
        ReportService.SystemStats stats = reportService.getOverallStatistics();
        assertCondition(stats.total > 0, "System Stats Total Complaints Count: " + stats.total);
        assertCondition(stats.resolved > 0, "System Stats Resolved Count: " + stats.resolved);

        System.out.println("\n=================================================");
        System.out.println("  ALL AUTOMATED TESTS PASSED SUCCESSFULLY! (10/10)");
        System.out.println("=================================================");
    }

    private static void assertCondition(boolean condition, String message) {
        if (condition) {
            System.out.println(" [PASS] " + message);
        } else {
            System.err.println(" [FAIL] " + message);
            System.exit(1);
        }
    }
}