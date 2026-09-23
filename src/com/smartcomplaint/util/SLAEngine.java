package com.smartcomplaint.util;

import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintPriority;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.SLAStatus;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

public class SLAEngine {

    /**
     * Computes the SLA deadline from creation time and priority.
     */
    public static Timestamp calculateDeadline(Timestamp createdAt, ComplaintPriority priority) {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
        if (priority == null) {
            priority = ComplaintPriority.MEDIUM;
        }
        LocalDateTime createdLdt = createdAt.toLocalDateTime();
        LocalDateTime deadlineLdt = createdLdt.plusHours(priority.getSlaHours());
        return Timestamp.valueOf(deadlineLdt);
    }

    /**
     * Evaluates current SLA status for a complaint.
     */
    public static SLAStatus evaluateSLAStatus(Complaint complaint) {
        if (complaint == null) return SLAStatus.WITHIN_SLA;

        if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.CLOSED) {
            if (complaint.getResolvedAt() != null && complaint.getSlaDeadline() != null) {
                if (complaint.getResolvedAt().after(complaint.getSlaDeadline())) {
                    return SLAStatus.SLA_BREACHED;
                }
            }
            return SLAStatus.RESOLVED;
        }

        if (complaint.getSlaDeadline() == null) {
            return SLAStatus.WITHIN_SLA;
        }

        long now = System.currentTimeMillis();
        long deadline = complaint.getSlaDeadline().getTime();

        if (now > deadline) {
            return SLAStatus.SLA_BREACHED;
        }

        // Check if due soon (< 12 hours remaining)
        long diffHours = (deadline - now) / (1000 * 60 * 60);
        if (diffHours < 12) {
            return SLAStatus.DUE_SOON;
        }

        return SLAStatus.WITHIN_SLA;
    }

    /**
     * Formats remaining SLA time or breach time as a human-friendly string.
     */
    public static String formatRemainingTime(Complaint complaint) {
        if (complaint == null || complaint.getSlaDeadline() == null) return "N/A";

        if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.CLOSED) {
            if (complaint.getSlaStatus() == SLAStatus.SLA_BREACHED) {
                return "Resolved (Past SLA Deadline)";
            }
            return "Resolved within SLA";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = complaint.getSlaDeadline().toLocalDateTime();
        Duration duration = Duration.between(now, deadline);

        if (duration.isNegative()) {
            long overdueMinutes = Math.abs(duration.toMinutes());
            long hours = overdueMinutes / 60;
            long mins = overdueMinutes % 60;
            long days = hours / 24;
            long remHours = hours % 24;

            if (days > 0) {
                return "Breached by " + days + "d " + remHours + "h";
            }
            return "Breached by " + hours + "h " + mins + "m";
        } else {
            long remainingMinutes = duration.toMinutes();
            long hours = remainingMinutes / 60;
            long mins = remainingMinutes % 60;
            long days = hours / 24;
            long remHours = hours % 24;

            if (days > 0) {
                return days + "d " + remHours + "h remaining";
            }
            if (hours > 0) {
                return hours + "h " + mins + "m remaining";
            }
            return mins + " minutes remaining";
        }
    }

    /**
     * Returns progress percentage (0 to 100) of SLA time consumed.
     */
    public static int calculateSLAProgressPercent(Complaint complaint) {
        if (complaint == null || complaint.getCreatedAt() == null || complaint.getSlaDeadline() == null) {
            return 0;
        }
        long start = complaint.getCreatedAt().getTime();
        long end = complaint.getSlaDeadline().getTime();
        long current = (complaint.getStatus() == ComplaintStatus.RESOLVED && complaint.getResolvedAt() != null)
                ? complaint.getResolvedAt().getTime()
                : System.currentTimeMillis();

        if (end <= start) return 100;
        if (current <= start) return 0;
        if (current >= end) return 100;

        long elapsed = current - start;
        long total = end - start;
        return (int) Math.min(100, Math.max(0, (elapsed * 100) / total));
    }
}
