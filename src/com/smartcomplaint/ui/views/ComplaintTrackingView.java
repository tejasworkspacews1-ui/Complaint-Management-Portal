package com.smartcomplaint.ui.views;

import com.smartcomplaint.dao.AuditLogDAO;
import com.smartcomplaint.model.*;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.*;
import com.smartcomplaint.util.SLAEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ComplaintTrackingView extends JPanel {
    private final ComplaintService complaintService = new ComplaintService();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private ModernTextField searchField;
    private JPanel detailsContainer;
    private VisualStatusTracker statusTracker;
    private JLabel idLabel;
    private JLabel titleLabel;
    private JLabel categoryLabel;
    private JLabel dateLabel;
    private JLabel priorityLabel;
    private JLabel deptLabel;
    private JLabel officerLabel;
    private JLabel statusBadgeContainer;
    private JLabel deadlineLabel;
    private JLabel remainingLabel;
    private JLabel resDateLabel;
    private JProgressBar slaProgressBar;

    private ModernTable historyTable;
    private DefaultTableModel historyTableModel;

    private CardPanel feedbackCard;
    private JComboBox<String> ratingCombo;
    private JTextArea feedbackArea;
    private Complaint currentComplaint;

    public ComplaintTrackingView() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        // Header & Search Bar
        JPanel top = new JPanel(new BorderLayout(15, 10));
        top.setOpaque(false);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        JLabel title = new JLabel("🔍 Track & Monitor Complaint");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Enter your Complaint ID (e.g. CMP20260001) to view real-time SLA status and progress.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        headerText.add(title);
        headerText.add(sub);
        top.add(headerText, BorderLayout.NORTH);

        // Search Box Card
        CardPanel searchCard = new CardPanel(new BorderLayout(10, 0), 12);
        searchCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        searchField = new ModernTextField("Enter Complaint ID (e.g. CMP20260001)...");
        searchField.setFont(Theme.FONT_SUBTITLE);
        ModernButton searchBtn = new ModernButton("Track Grievance");
        searchBtn.addActionListener(e -> trackComplaint(searchField.getText().trim()));
        searchField.addActionListener(e -> trackComplaint(searchField.getText().trim()));

        searchCard.add(searchField, BorderLayout.CENTER);
        searchCard.add(searchBtn, BorderLayout.EAST);
        top.add(searchCard, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        // Main Details Container (Scrollable)
        detailsContainer = new JPanel();
        detailsContainer.setLayout(new BoxLayout(detailsContainer, BoxLayout.Y_AXIS));
        detailsContainer.setOpaque(false);

        // 1. Visual Tracker Card
        CardPanel trackerCard = new CardPanel(new BorderLayout(10, 10), 14);
        trackerCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel trackerTitle = new JLabel("Live Progress Pipeline");
        trackerTitle.setFont(Theme.FONT_SUBTITLE);
        trackerCard.add(trackerTitle, BorderLayout.NORTH);

        statusTracker = new VisualStatusTracker();
        trackerCard.add(statusTracker, BorderLayout.CENTER);
        detailsContainer.add(trackerCard);
        detailsContainer.add(Box.createVerticalStrut(14));

        // 2. Info Summary Grid Card
        CardPanel infoCard = new CardPanel(new BorderLayout(12, 14), 14);
        infoCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel infoHeader = new JPanel(new BorderLayout());
        infoHeader.setOpaque(false);
        titleLabel = new JLabel("Complaint Overview");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        statusBadgeContainer = new JLabel();
        infoHeader.add(titleLabel, BorderLayout.WEST);
        infoHeader.add(statusBadgeContainer, BorderLayout.EAST);
        infoCard.add(infoHeader, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(5, 2, 12, 10));
        grid.setOpaque(false);

        idLabel = new JLabel("-");
        categoryLabel = new JLabel("-");
        dateLabel = new JLabel("-");
        priorityLabel = new JLabel("-");
        deptLabel = new JLabel("-");
        officerLabel = new JLabel("-");
        deadlineLabel = new JLabel("-");
        remainingLabel = new JLabel("-");
        resDateLabel = new JLabel("-");

        addInfoRow(grid, "Complaint ID:", idLabel);
        addInfoRow(grid, "Category & Sub-Category:", categoryLabel);
        addInfoRow(grid, "Date Submitted:", dateLabel);
        addInfoRow(grid, "Priority & SLA Target:", priorityLabel);
        addInfoRow(grid, "Assigned Department:", deptLabel);
        addInfoRow(grid, "Assigned Officer:", officerLabel);
        addInfoRow(grid, "SLA Deadline:", deadlineLabel);
        addInfoRow(grid, "Time Remaining / SLA:", remainingLabel);
        addInfoRow(grid, "Resolution Date:", resDateLabel);

        // SLA Progress Bar
        JPanel progressPanel = new JPanel(new BorderLayout(8, 4));
        progressPanel.setOpaque(false);
        slaProgressBar = new JProgressBar(0, 100);
        slaProgressBar.setStringPainted(true);
        slaProgressBar.setPreferredSize(new Dimension(200, 20));
        progressPanel.add(new JLabel("SLA Elapsed:"), BorderLayout.WEST);
        progressPanel.add(slaProgressBar, BorderLayout.CENTER);
        grid.add(progressPanel);

        infoCard.add(grid, BorderLayout.CENTER);
        detailsContainer.add(infoCard);
        detailsContainer.add(Box.createVerticalStrut(14));

        // 3. History & Audit Trail Card
        CardPanel historyCard = new CardPanel(new BorderLayout(10, 10), 14);
        historyCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel histTitle = new JLabel("📜 Activity History & Resolution Log");
        histTitle.setFont(Theme.FONT_SUBTITLE);
        historyCard.add(histTitle, BorderLayout.NORTH);

        historyTableModel = new DefaultTableModel(new String[]{"Date & Time", "Status Transition", "Action By", "Remarks / Work Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new ModernTable(historyTableModel);
        JScrollPane histScroll = new JScrollPane(historyTable);
        histScroll.setPreferredSize(new Dimension(600, 160));
        historyCard.add(histScroll, BorderLayout.CENTER);

        detailsContainer.add(historyCard);
        detailsContainer.add(Box.createVerticalStrut(14));

        // 4. Citizen Feedback Card (Conditional)
        feedbackCard = new CardPanel(new BorderLayout(10, 10), 14);
        feedbackCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel fbTitle = new JLabel("⭐ Rate Resolution Quality");
        fbTitle.setFont(Theme.FONT_SUBTITLE);
        feedbackCard.add(fbTitle, BorderLayout.NORTH);

        JPanel fbBody = new JPanel(new BorderLayout(10, 8));
        fbBody.setOpaque(false);

        JPanel rateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rateRow.setOpaque(false);
        rateRow.add(new JLabel("Your Rating:"));
        ratingCombo = new JComboBox<>(new String[]{"⭐⭐⭐⭐⭐ (5 - Excellent)", "⭐⭐⭐⭐ (4 - Good)", "⭐⭐⭐ (3 - Average)", "⭐⭐ (2 - Poor)", "⭐ (1 - Very Bad)"});
        rateRow.add(ratingCombo);
        fbBody.add(rateRow, BorderLayout.NORTH);

        feedbackArea = new JTextArea(2, 30);
        feedbackArea.setFont(Theme.FONT_BODY);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JScrollPane fbScroll = new JScrollPane(feedbackArea);
        fbBody.add(fbScroll, BorderLayout.CENTER);

        ModernButton submitFbBtn = ModernButton.success("Submit Feedback");
        submitFbBtn.addActionListener(e -> submitCitizenFeedback());
        JPanel fbBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fbBtnRow.setOpaque(false);
        fbBtnRow.add(submitFbBtn);
        fbBody.add(fbBtnRow, BorderLayout.SOUTH);

        feedbackCard.add(fbBody, BorderLayout.CENTER);
        detailsContainer.add(feedbackCard);

        JScrollPane mainScroll = new JScrollPane(detailsContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        // Hide feedback card initially
        feedbackCard.setVisible(false);
    }

    private void addInfoRow(JPanel grid, String title, JLabel val) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_HEADER);
        t.setForeground(Theme.TEXT_SECONDARY);
        val.setFont(Theme.FONT_BODY_BOLD);
        val.setForeground(Theme.TEXT_PRIMARY);
        p.add(t, BorderLayout.WEST);
        p.add(val, BorderLayout.CENTER);
        grid.add(p);
    }

    public void trackComplaint(String complaintId) {
        if (complaintId == null || complaintId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Complaint ID.", "Track Complaint", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Complaint c = complaintService.trackComplaint(complaintId.trim());
        if (c == null) {
            JOptionPane.showMessageDialog(this, "No complaint found with ID: " + complaintId, "Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        displayComplaint(c);
    }

    public void displayComplaint(Complaint c) {
        this.currentComplaint = c;
        searchField.setText(c.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        idLabel.setText(c.getId());
        titleLabel.setText(c.getTitle());
        categoryLabel.setText(c.getCategory().getDisplayName() + (c.getSubCategory() != null ? " → " + c.getSubCategory() : ""));
        dateLabel.setText(c.getCreatedAt() != null ? sdf.format(c.getCreatedAt()) : "N/A");
        priorityLabel.setText(c.getPriority().getDisplayName() + " (SLA: " + c.getPriority().getSlaLabel() + ")");
        deptLabel.setText(c.getAssignedDepartmentName() != null ? c.getAssignedDepartmentName() : "General Dispatch");
        officerLabel.setText(c.getAssignedOfficerName() != null ? c.getAssignedOfficerName() : "Pending Assignment");
        deadlineLabel.setText(c.getSlaDeadline() != null ? sdf.format(c.getSlaDeadline()) : "N/A");
        remainingLabel.setText(c.getRemainingTimeFormatted());

        if (c.isBreached()) {
            remainingLabel.setForeground(Theme.DANGER);
        } else if (c.getSlaStatus() == SLAStatus.DUE_SOON) {
            remainingLabel.setForeground(Theme.WARNING);
        } else {
            remainingLabel.setForeground(Theme.SUCCESS);
        }

        resDateLabel.setText(c.getResolvedAt() != null ? sdf.format(c.getResolvedAt()) : "Pending Resolution");

        // Status badge & pipeline
        statusTracker.setStatus(c.getStatus());

        // SLA progress
        int progress = SLAEngine.calculateSLAProgressPercent(c);
        slaProgressBar.setValue(progress);
        slaProgressBar.setString(progress + "% SLA Time Elapsed");
        if (progress >= 100 && c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED) {
            slaProgressBar.setForeground(Theme.DANGER);
        } else if (progress >= 75) {
            slaProgressBar.setForeground(Theme.WARNING);
        } else {
            slaProgressBar.setForeground(Theme.PRIMARY);
        }

        // History logs
        historyTableModel.setRowCount(0);
        List<ComplaintLog> logs = auditLogDAO.getLogsForComplaint(c.getId());
        for (ComplaintLog l : logs) {
            String transition = (l.getFromStatus() != null ? l.getFromStatus().getDisplayName() + " → " : "") + l.getToStatus().getDisplayName();
            historyTableModel.addRow(new Object[]{
                    sdf.format(l.getCreatedAt()),
                    transition,
                    l.getActionByName() + " (" + (l.getActionByRole() != null ? l.getActionByRole().name() : "") + ")",
                    l.getActionRemarks()
            });
        }

        // Show feedback card if resolved
        boolean isResolved = (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED);
        feedbackCard.setVisible(isResolved);
        if (c.getCitizenRating() != null && c.getCitizenRating() > 0) {
            feedbackArea.setText(c.getCitizenFeedback() != null ? c.getCitizenFeedback() : "");
            ratingCombo.setSelectedIndex(5 - c.getCitizenRating());
        }

        detailsContainer.revalidate();
        detailsContainer.repaint();
    }

    private void submitCitizenFeedback() {
        if (currentComplaint == null) return;
        int selectedIndex = ratingCombo.getSelectedIndex();
        int rating = 5 - selectedIndex;
        String feedback = feedbackArea.getText().trim();

        boolean ok = complaintService.submitFeedback(currentComplaint.getId(), rating, feedback);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been recorded.", "Feedback Submitted", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to submit feedback.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
