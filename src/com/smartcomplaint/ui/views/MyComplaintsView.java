package com.smartcomplaint.ui.views;

import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintPriority;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.SLAStatus;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.CardPanel;
import com.smartcomplaint.ui.components.ModernButton;
import com.smartcomplaint.ui.components.ModernTable;
import com.smartcomplaint.ui.components.ModernTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MyComplaintsView extends JPanel {
    private final ComplaintService complaintService = new ComplaintService();
    private final Consumer<String> onTrackRequested;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private ModernTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private List<Complaint> loadedComplaints = new ArrayList<>();

    public MyComplaintsView(Consumer<String> onTrackRequested) {
        this.onTrackRequested = onTrackRequested;
        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        JLabel title = new JLabel("📋 My Registered Complaints");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("View and monitor all grievances submitted by your account.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        headerText.add(title);
        headerText.add(sub);
        top.add(headerText, BorderLayout.NORTH);

        // Filter Bar Card
        CardPanel filterCard = new CardPanel(new BorderLayout(10, 0), 10);
        filterCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        searchField = new ModernTextField("Search by ID, Title or Location...");
        searchField.addActionListener(e -> applyFilters());

        JPanel rightFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightFilters.setOpaque(false);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Pending", "In Progress", "Resolved", "SLA Breached"});
        statusFilterCombo.addActionListener(e -> applyFilters());

        ModernButton refreshBtn = ModernButton.neutral("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        ModernButton trackBtn = new ModernButton("🔍 Track Selected");
        trackBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) table.getValueAt(row, 0);
                if (onTrackRequested != null) onTrackRequested.accept(id);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a complaint row to track.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        rightFilters.add(new JLabel("Filter:"));
        rightFilters.add(statusFilterCombo);
        rightFilters.add(refreshBtn);
        rightFilters.add(trackBtn);

        filterCard.add(searchField, BorderLayout.CENTER);
        filterCard.add(rightFilters, BorderLayout.EAST);
        top.add(filterCard, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        // Table Card
        CardPanel tableCard = new CardPanel(new BorderLayout(), 12);
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Title", "Category", "Priority", "Status", "SLA Status", "Remaining / Overdue", "Submitted On"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshData() {
        User u = AuthService.getCurrentUser();
        if (u == null) return;

        loadedComplaints = complaintService.getUserComplaints(u.getId());
        applyFilters();
    }

    private void applyFilters() {
        tableModel.setRowCount(0);
        String search = searchField.getText().trim().toLowerCase();
        String filter = (String) statusFilterCombo.getSelectedItem();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Complaint c : loadedComplaints) {
            boolean matchesSearch = search.isEmpty() ||
                    c.getId().toLowerCase().contains(search) ||
                    c.getTitle().toLowerCase().contains(search) ||
                    c.getLocation().toLowerCase().contains(search);

            boolean matchesStatus = true;
            if ("Pending".equals(filter)) {
                matchesStatus = (c.getStatus() == ComplaintStatus.SUBMITTED || c.getStatus() == ComplaintStatus.UNDER_REVIEW || c.getStatus() == ComplaintStatus.ASSIGNED);
            } else if ("In Progress".equals(filter)) {
                matchesStatus = (c.getStatus() == ComplaintStatus.IN_PROGRESS);
            } else if ("Resolved".equals(filter)) {
                matchesStatus = (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED);
            } else if ("SLA Breached".equals(filter)) {
                matchesStatus = (c.getSlaStatus() == SLAStatus.SLA_BREACHED);
            }

            if (matchesSearch && matchesStatus) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getTitle(),
                        c.getCategory() != null ? c.getCategory().getDisplayName() : "N/A",
                        c.getPriority(),
                        c.getStatus(),
                        c.getSlaStatus(),
                        c.getRemainingTimeFormatted(),
                        c.getCreatedAt() != null ? sdf.format(c.getCreatedAt()) : ""
                });
            }
        }
    }
}
