package com.smartcomplaint.util;

import com.smartcomplaint.model.Complaint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

public class CSVExporter {

    public static boolean exportComplaintsToCSV(List<Complaint> complaints, File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header
            writer.println("Complaint ID,Title,Category,Sub-Category,Priority,Status,SLA Status,Location,Citizen,Officer,Department,Submitted At,SLA Deadline,Resolved At");

            for (Complaint c : complaints) {
                StringBuilder sb = new StringBuilder();
                sb.append(escape(c.getId())).append(",");
                sb.append(escape(c.getTitle())).append(",");
                sb.append(escape(c.getCategory() != null ? c.getCategory().getDisplayName() : "")).append(",");
                sb.append(escape(c.getSubCategory())).append(",");
                sb.append(escape(c.getPriority() != null ? c.getPriority().getDisplayName() : "")).append(",");
                sb.append(escape(c.getStatus() != null ? c.getStatus().getDisplayName() : "")).append(",");
                sb.append(escape(c.getSlaStatus() != null ? c.getSlaStatus().getDisplayName() : "")).append(",");
                sb.append(escape(c.getLocation())).append(",");
                sb.append(escape(c.isAnonymous() ? "Anonymous" : (c.getCitizenName() != null ? c.getCitizenName() : "N/A"))).append(",");
                sb.append(escape(c.getAssignedOfficerName() != null ? c.getAssignedOfficerName() : "Unassigned")).append(",");
                sb.append(escape(c.getAssignedDepartmentName() != null ? c.getAssignedDepartmentName() : "N/A")).append(",");
                sb.append(c.getCreatedAt() != null ? sdf.format(c.getCreatedAt()) : "").append(",");
                sb.append(c.getSlaDeadline() != null ? sdf.format(c.getSlaDeadline()) : "").append(",");
                sb.append(c.getResolvedAt() != null ? sdf.format(c.getResolvedAt()) : "");
                writer.println(sb.toString());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String escape(String data) {
        if (data == null) return "\"\"";
        String escaped = data.replaceAll("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
