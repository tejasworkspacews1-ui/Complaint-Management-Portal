package com.smartcomplaint.service;

import com.smartcomplaint.dao.ComplaintDAO;
import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintCategory;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.SLAStatus;
import com.smartcomplaint.util.CSVExporter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    public static class SystemStats {
        public int total;
        public int pending;
        public int inProgress;
        public int resolved;
        public int breached;
        public int dueSoon;
        public double resolutionRate;
        public double slaComplianceRate;
    }

    public SystemStats getOverallStatistics() {
        List<Complaint> all = complaintDAO.getAllComplaints();
        SystemStats s = new SystemStats();
        s.total = all.size();

        int resolvedCount = 0;
        int breachedCount = 0;

        for (Complaint c : all) {
            if (c.getStatus() == ComplaintStatus.SUBMITTED || c.getStatus() == ComplaintStatus.UNDER_REVIEW || c.getStatus() == ComplaintStatus.ASSIGNED) {
                s.pending++;
            } else if (c.getStatus() == ComplaintStatus.IN_PROGRESS) {
                s.inProgress++;
            } else if (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED) {
                s.resolved++;
                resolvedCount++;
            }

            if (c.getSlaStatus() == SLAStatus.SLA_BREACHED) {
                s.breached++;
                breachedCount++;
            } else if (c.getSlaStatus() == SLAStatus.DUE_SOON) {
                s.dueSoon++;
            }
        }

        s.resolutionRate = s.total > 0 ? ((double) s.resolved / s.total) * 100.0 : 0.0;
        int activeOrResolved = s.total;
        s.slaComplianceRate = activeOrResolved > 0 ? (((double) (activeOrResolved - breachedCount)) / activeOrResolved) * 100.0 : 100.0;

        return s;
    }

    public Map<ComplaintCategory, Integer> getCategoryDistribution() {
        List<Complaint> all = complaintDAO.getAllComplaints();
        Map<ComplaintCategory, Integer> map = new HashMap<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            map.put(cat, 0);
        }
        for (Complaint c : all) {
            ComplaintCategory cat = c.getCategory() != null ? c.getCategory() : ComplaintCategory.OTHER;
            map.put(cat, map.getOrDefault(cat, 0) + 1);
        }
        return map;
    }

    public boolean exportAllToCSV(File file) {
        List<Complaint> all = complaintDAO.getAllComplaints();
        return CSVExporter.exportComplaintsToCSV(all, file);
    }
}
