package com.smartcomplaint.ui.views;

import com.smartcomplaint.dao.AuditLogDAO;
import com.smartcomplaint.dao.OfficerDAO;
import com.smartcomplaint.model.*;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.service.NotificationService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OfficerDashboardView extends JPanel {
    private final ComplaintService complaintService = new ComplaintService();
    private final NotificationService notificationService = new NotificationService();
    private final OfficerDAO officerDAO = new OfficerDAO();
    private final Frame parentFrame;
    private final Runnable onLogout;

    private JLabel officerNameLabel;
    private JLabel officerDeptLabel;
    private StatCard assignedCard;
    private StatCard inProgressCard;
    private StatCard resolvedCard;
    private StatCard breachedCard;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private ModernTextField searchField;
    private JComboBox<String> filterCombo;
    private List<Complaint> currentList = new ArrayList<>();

    private SidebarButton btnTasks;
    private SidebarButton btnDeptQueue;
    private SidebarButton btnNotifications;
    private SidebarButton btnProfile;

    private boolean isDepartmentQueueMode = false;

    public OfficerDashboardView(Frame parentFrame, Runnable onLogout) {
        this.parentFrame = parentFrame;
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main Center
        JPanel main = new JPanel(new BorderLayout(15, 18));
        main.setBackground(Theme.BG_MAIN);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel headerInfo = new JPanel(new GridLayout(2, 1, 0, 4));
        headerInfo.setOpaque(false);

        officerNameLabel = new JLabel("Officer Workspace");
        officerNameLabel.setFont(Theme.FONT_TITLE_LARGE);
        officerNameLabel.setForeground(Theme.TEXT_PRIMARY);

        officerDeptLabel = new JLabel("Department: Engineering & Works");
        officerDeptLabel.setFont(Theme.FONT_SUBTITLE);
        officerDeptLabel.setForeground(Theme.PRIMARY);

        headerInfo.add(officerNameLabel);
        headerInfo.add(officerDeptLabel);
        top.add(headerInfo, BorderLayout.WEST);

        main.add(top, BorderLayout.NORTH);

        // Center Area
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);

        // 4 KPI Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);

        assignedCard = new StatCard("Assigned Queue", "0", "Awaiting action", Theme.PRIMARY, "📥");
        inProgressCard = new StatCard("In Progress", "0", "Work underway", Theme.WARNING, "⚙");
        resolvedCard = new StatCard("Resolved", "0", "Finished tickets", Theme.SUCCESS, "✔");
        breachedCard = new StatCard("SLA Breached", "0", "Urgent escalation", Theme.DANGER, "🚨");

        statsGrid.add(assignedCard);
        statsGrid.add(inProgressCard);
        statsGrid.add(resolvedCard);
        statsGrid.add(breachedCard);
        center.add(statsGrid, BorderLayout.NORTH);

        // Table Card
        CardPanel tableCard = new CardPanel(new BorderLayout(10, 12), 14);
        tableCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Table Action Controls
        JPanel ctrlRow = new JPanel(new BorderLayout(10, 0));
        ctrlRow.setOpaque(false);

        searchField = new ModernTextField("Search complaints by ID, title, citizen...");
        searchField.addActionListener(e -> applyFilter());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        filterCombo = new JComboBox<>(new String[]{"All Complaints", "Pending / Assigned", "In Progress", "Resolved", "🚨 SLA Breached"});
        filterCombo.addActionListener(e -> applyFilter());

        ModernButton refreshBtn = ModernButton.neutral("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        ModernButton updateBtn = ModernButton.success("✏ Update Status & Remarks");
        updateBtn.addActionListener(e -> openStatusUpdateModal());

        actions.add(new JLabel("Filter:"));
        actions.add(filterCombo);
        actions.add(refreshBtn);
        actions.add(updateBtn);

        ctrlRow.add(searchField, BorderLayout.CENTER);
        ctrlRow.add(actions, BorderLayout.EAST);
        tableCard.add(ctrlRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"Complaint ID", "Title", "Category", "Priority", "Status", "SLA Status", "Remaining SLA", "Citizen", "Location"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openStatusUpdateModal();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scroll, BorderLayout.CENTER);

        center.add(tableCard, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        JPanel brand = new JPanel(new BorderLayout(8, 4));
        brand.setOpaque(false);
        JLabel logo = new JLabel("🏛️ SmartPortal");
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Color.WHITE);
        JLabel role = new JLabel("  OFFICER DESK");
        role.setFont(Theme.FONT_SMALL_BOLD);
        role.setForeground(Theme.WARNING);
        brand.add(logo, BorderLayout.NORTH);
        brand.add(role, BorderLayout.SOUTH);

        JPanel menu = new JPanel(new GridLayout(6, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(30, 0, 30, 0));

        btnTasks = new SidebarButton("My Assigned Tasks", "📋");
        btnDeptQueue = new SidebarButton("Department Queue", "🏢");
        btnNotifications = new SidebarButton("Notifications", "🔔");
        btnProfile = new SidebarButton("My Profile", "👤");

        btnTasks.setActive(true);

        btnTasks.addActionListener(e -> {
            isDepartmentQueueMode = false;
            btnTasks.setActive(true);
            btnDeptQueue.setActive(false);
            refreshData();
        });

        btnDeptQueue.addActionListener(e -> {
            isDepartmentQueueMode = true;
            btnTasks.setActive(false);
            btnDeptQueue.setActive(true);
            refreshData();
        });

        btnNotifications.addActionListener(e -> new NotificationDialog(parentFrame, this::updateNotificationBadge).setVisible(true));
        btnProfile.addActionListener(e -> new ProfileDialog(parentFrame).setVisible(true));

        menu.add(btnTasks);
        menu.add(btnDeptQueue);
        menu.add(btnNotifications);
        menu.add(btnProfile);

        SidebarButton btnLogout = new SidebarButton("Logout", "🚪");
        btnLogout.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "Sign out from Officer Desk?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION && onLogout != null) {
                AuthService.logout();
                onLogout.run();
            }
        });

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        return sidebar;
    }

    public void refreshData() {
        User u = AuthService.getCurrentUser();
        if (u == null) return;

        complaintService.triggerSLACheck();

        Officer officer = officerDAO.getOfficerByUserId(u.getId());
        String deptTitle = (officer != null && officer.getDepartmentName() != null) ? officer.getDepartmentName() : "Civic Grievances";
        String desig = (officer != null && officer.getDesignation() != null) ? officer.getDesignation() : "Field Officer";
        String empCode = (officer != null && officer.getEmployeeCode() != null) ? " [" + officer.getEmployeeCode() + "]" : "";

        officerNameLabel.setText(u.getFullName() + empCode);
        officerDeptLabel.setText(desig + " • " + deptTitle + (isDepartmentQueueMode ? " (All Dept Tickets)" : " (My Assigned Tickets)"));

        if (isDepartmentQueueMode && officer != null) {
            currentList = complaintService.getDepartmentComplaints(officer.getDepartmentId());
        } else {
            currentList = complaintService.getOfficerComplaints(u.getId());
        }

        // Calculate KPI stats
        int assigned = 0;
        int inProg = 0;
        int resolved = 0;
        int breached = 0;

        for (Complaint c : currentList) {
            if (c.getStatus() == ComplaintStatus.ASSIGNED || c.getStatus() == ComplaintStatus.SUBMITTED || c.getStatus() == ComplaintStatus.UNDER_REVIEW) {
                assigned++;
            } else if (c.getStatus() == ComplaintStatus.IN_PROGRESS) {
                inProg++;
            } else if (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED) {
                resolved++;
            }

            if (c.getSlaStatus() == SLAStatus.SLA_BREACHED) {
                breached++;
            }
        }

        assignedCard.setValue(String.valueOf(assigned));
        inProgressCard.setValue(String.valueOf(inProg));
        resolvedCard.setValue(String.valueOf(resolved));
        breachedCard.setValue(String.valueOf(breached));

        applyFilter();
        updateNotificationBadge();
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String search = searchField.getText().trim().toLowerCase();
        String filter = (String) filterCombo.getSelectedItem();

        for (Complaint c : currentList) {
            boolean matchSearch = search.isEmpty() ||
                    c.getId().toLowerCase().contains(search) ||
                    c.getTitle().toLowerCase().contains(search) ||
                    c.getLocation().toLowerCase().contains(search) ||
                    (c.getCitizenName() != null && c.getCitizenName().toLowerCase().contains(search));

            boolean matchFilter = true;
            if ("Pending / Assigned".equals(filter)) {
                matchFilter = (c.getStatus() == ComplaintStatus.ASSIGNED || c.getStatus() == ComplaintStatus.SUBMITTED || c.getStatus() == ComplaintStatus.UNDER_REVIEW);
            } else if ("In Progress".equals(filter)) {
                matchFilter = (c.getStatus() == ComplaintStatus.IN_PROGRESS);
            } else if ("Resolved".equals(filter)) {
                matchFilter = (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED);
            } else if ("🚨 SLA Breached".equals(filter)) {
                matchFilter = (c.getSlaStatus() == SLAStatus.SLA_BREACHED);
            }

            if (matchSearch && matchFilter) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getTitle(),
                        c.getCategory() != null ? c.getCategory().getDisplayName() : "N/A",
                        c.getPriority(),
                        c.getStatus(),
                        c.getSlaStatus(),
                        c.getRemainingTimeFormatted(),
                        c.isAnonymous() ? "Anonymous" : (c.getCitizenName() != null ? c.getCitizenName() : "N/A"),
                        c.getLocation()
                });
            }
        }
    }

    private void openStatusUpdateModal() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint row to update.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String complaintId = (String) table.getValueAt(row, 0);
        Complaint c = complaintService.trackComplaint(complaintId);
        if (c == null) return;

        JDialog dialog = new JDialog(parentFrame, "Update Complaint: " + c.getId(), true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(12, 14));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        CardPanel card = new CardPanel(new BorderLayout(10, 12), 12);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Details overview
        JPanel header = new JPanel(new GridLayout(4, 1, 0, 4));
        header.setOpaque(false);
        JLabel idT = new JLabel(c.getId() + " - " + c.getTitle());
        idT.setFont(Theme.FONT_TITLE);
        JLabel locT = new JLabel("📍 " + c.getLocation() + " | Category: " + (c.getCategory() != null ? c.getCategory().getDisplayName() : ""));
        locT.setFont(Theme.FONT_SMALL);
        JLabel slaT = new JLabel("⚡ Priority: " + c.getPriority().getDisplayName() + " | SLA: " + c.getRemainingTimeFormatted());
        slaT.setFont(Theme.FONT_SMALL_BOLD);
        slaT.setForeground(c.isBreached() ? Theme.DANGER : Theme.PRIMARY);

        header.add(idT);
        header.add(locT);
        header.add(slaT);
        card.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(3, 1, 0, 8));
        form.setOpaque(false);

        JPanel statusRow = new JPanel(new BorderLayout(8, 0));
        statusRow.setOpaque(false);
        statusRow.add(new JLabel("New Status: "), BorderLayout.WEST);
        JComboBox<ComplaintStatus> statusCombo = new JComboBox<>(new ComplaintStatus[]{
                ComplaintStatus.UNDER_REVIEW,
                ComplaintStatus.IN_PROGRESS,
                ComplaintStatus.RESOLVED,
                ComplaintStatus.CLOSED,
                ComplaintStatus.REJECTED
        });
        statusCombo.setSelectedItem(c.getStatus());
        statusRow.add(statusCombo, BorderLayout.CENTER);
        form.add(statusRow);

        JLabel remLbl = new JLabel("Officer Action Remarks / Work Progress Note *:");
        remLbl.setFont(Theme.FONT_HEADER);
        form.add(remLbl);

        JTextArea remarksArea = new JTextArea(4, 30);
        remarksArea.setFont(Theme.FONT_BODY);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane rScroll = new JScrollPane(remarksArea);
        form.add(rScroll);

        card.add(form, BorderLayout.CENTER);

        // Bottom
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btm.setOpaque(false);

        ModernButton cancelBtn = ModernButton.neutral("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        ModernButton saveBtn = ModernButton.success("Save Status Update");
        saveBtn.addActionListener(e -> {
            String remarks = remarksArea.getText().trim();
            if (remarks.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter action remarks or progress details.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ComplaintStatus newStat = (ComplaintStatus) statusCombo.getSelectedItem();
            User currUser = AuthService.getCurrentUser();

            boolean ok = complaintService.updateStatus(c.getId(), newStat, remarks, currUser);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Complaint " + c.getId() + " updated successfully!", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update complaint in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btm.add(cancelBtn);
        btm.add(saveBtn);
        card.add(btm, BorderLayout.SOUTH);

        p.add(card, BorderLayout.CENTER);
        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    private void updateNotificationBadge() {
        User u = AuthService.getCurrentUser();
        if (u != null) {
            int unread = notificationService.getUnreadCount(u.getId());
            btnNotifications.setText("  🔔  Notifications" + (unread > 0 ? " (" + unread + ")" : ""));
        }
    }
}
