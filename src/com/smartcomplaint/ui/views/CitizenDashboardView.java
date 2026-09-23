package com.smartcomplaint.ui.views;

import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.User;
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
import java.util.List;

public class CitizenDashboardView extends JPanel {
    private final ComplaintService complaintService = new ComplaintService();
    private final NotificationService notificationService = new NotificationService();
    private final Frame parentFrame;
    private final Runnable onLogout;

    private JPanel contentContainer;
    private CardLayout cardLayout;

    // Sub views
    private JPanel homePanel;
    private ComplaintRegistrationView regView;
    private MyComplaintsView myComplaintsView;
    private ComplaintTrackingView trackView;

    // Sidebar buttons
    private SidebarButton btnDashboard;
    private SidebarButton btnRegister;
    private SidebarButton btnMyComplaints;
    private SidebarButton btnTrack;
    private SidebarButton btnNotifications;
    private SidebarButton btnProfile;

    // Dashboard Home Stat Cards
    private StatCard totalCard;
    private StatCard pendingCard;
    private StatCard inProgressCard;
    private StatCard resolvedCard;

    private JLabel welcomeLabel;
    private ModernTable recentTable;
    private DefaultTableModel recentModel;

    public CitizenDashboardView(Frame parentFrame, Runnable onLogout) {
        this.parentFrame = parentFrame;
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        // Sidebar (West)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content Area (Center with CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(Theme.BG_MAIN);

        // 1. Home Dashboard View
        homePanel = createHomePanel();
        contentContainer.add(homePanel, "HOME");

        // 2. Registration View
        regView = new ComplaintRegistrationView(this::showTrackingForId);
        contentContainer.add(regView, "REGISTER");

        // 3. My Complaints View
        myComplaintsView = new MyComplaintsView(this::showTrackingForId);
        contentContainer.add(myComplaintsView, "MY_COMPLAINTS");

        // 4. Track Complaint View
        trackView = new ComplaintTrackingView();
        contentContainer.add(trackView, "TRACK");

        add(contentContainer, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Sidebar Brand
        JPanel brandPanel = new JPanel(new BorderLayout(8, 4));
        brandPanel.setOpaque(false);
        JLabel brandLogo = new JLabel("🏛️ SmartPortal");
        brandLogo.setFont(Theme.FONT_TITLE);
        brandLogo.setForeground(Color.WHITE);
        JLabel roleTag = new JLabel("  CITIZEN PORTAL");
        roleTag.setFont(Theme.FONT_SMALL_BOLD);
        roleTag.setForeground(Theme.ACCENT);
        brandPanel.add(brandLogo, BorderLayout.NORTH);
        brandPanel.add(roleTag, BorderLayout.SOUTH);

        // Menu Buttons
        JPanel menu = new JPanel(new GridLayout(7, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(30, 0, 30, 0));

        btnDashboard = new SidebarButton("Dashboard", "📊");
        btnRegister = new SidebarButton("Register Complaint", "📝");
        btnMyComplaints = new SidebarButton("My Complaints", "📋");
        btnTrack = new SidebarButton("Track Complaint", "🔍");
        btnNotifications = new SidebarButton("Notifications", "🔔");
        btnProfile = new SidebarButton("My Profile", "👤");

        btnDashboard.addActionListener(e -> selectView("HOME", btnDashboard));
        btnRegister.addActionListener(e -> selectView("REGISTER", btnRegister));
        btnMyComplaints.addActionListener(e -> {
            myComplaintsView.refreshData();
            selectView("MY_COMPLAINTS", btnMyComplaints);
        });
        btnTrack.addActionListener(e -> selectView("TRACK", btnTrack));
        btnNotifications.addActionListener(e -> showNotifications());
        btnProfile.addActionListener(e -> showProfile());

        btnDashboard.setActive(true);

        menu.add(btnDashboard);
        menu.add(btnRegister);
        menu.add(btnMyComplaints);
        menu.add(btnTrack);
        menu.add(btnNotifications);
        menu.add(btnProfile);

        // Logout at bottom
        SidebarButton btnLogout = new SidebarButton("Logout", "🚪");
        btnLogout.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION && onLogout != null) {
                AuthService.logout();
                onLogout.run();
            }
        });

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createHomePanel() {
        JPanel p = new JPanel(new BorderLayout(15, 18));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Greeting Banner
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        welcomeLabel = new JLabel("Welcome back!");
        welcomeLabel.setFont(Theme.FONT_TITLE_LARGE);
        welcomeLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Monitor, submit, and escalate your civic issues in real-time.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        top.add(welcomeLabel, BorderLayout.NORTH);
        top.add(sub, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        // Center Area: Stat Cards + Recent Table
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);

        // 4 Stat Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);

        totalCard = new StatCard("Total Submitted", "0", "All time complaints", Theme.PRIMARY, "📑");
        pendingCard = new StatCard("Pending Review", "0", "Awaiting assignment", Theme.WARNING, "⏳");
        inProgressCard = new StatCard("In Progress", "0", "Field officer resolving", Theme.ACCENT, "⚙");
        resolvedCard = new StatCard("Resolved", "0", "Successfully closed", Theme.SUCCESS, "✔");

        statsGrid.add(totalCard);
        statsGrid.add(pendingCard);
        statsGrid.add(inProgressCard);
        statsGrid.add(resolvedCard);
        center.add(statsGrid, BorderLayout.NORTH);

        // Recent Complaints Card
        CardPanel recentCard = new CardPanel(new BorderLayout(10, 10), 14);
        recentCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);

        JLabel recentTitle = new JLabel("Recent Complaints");
        recentTitle.setFont(Theme.FONT_TITLE);

        ModernButton newBtn = ModernButton.success("+ Register New");
        newBtn.addActionListener(e -> selectView("REGISTER", btnRegister));
        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentHeader.add(newBtn, BorderLayout.EAST);
        recentCard.add(recentHeader, BorderLayout.NORTH);

        recentModel = new DefaultTableModel(new String[]{"ID", "Title", "Category", "Priority", "Status", "SLA Status", "Submitted"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        recentTable = new ModernTable(recentModel);
        recentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = recentTable.getSelectedRow();
                    if (row >= 0) {
                        String id = (String) recentTable.getValueAt(row, 0);
                        showTrackingForId(id);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        recentCard.add(scroll, BorderLayout.CENTER);

        center.add(recentCard, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        return p;
    }

    private void selectView(String name, SidebarButton activeBtn) {
        btnDashboard.setActive(btnDashboard == activeBtn);
        btnRegister.setActive(btnRegister == activeBtn);
        btnMyComplaints.setActive(btnMyComplaints == activeBtn);
        btnTrack.setActive(btnTrack == activeBtn);
        btnNotifications.setActive(btnNotifications == activeBtn);
        btnProfile.setActive(btnProfile == activeBtn);

        cardLayout.show(contentContainer, name);
    }

    public void showTrackingForId(String complaintId) {
        selectView("TRACK", btnTrack);
        trackView.trackComplaint(complaintId);
    }

    private void showNotifications() {
        new NotificationDialog(parentFrame, this::updateNotificationBadge).setVisible(true);
    }

    private void showProfile() {
        new ProfileDialog(parentFrame).setVisible(true);
    }

    public void refreshDashboard() {
        User u = AuthService.getCurrentUser();
        if (u == null) return;

        welcomeLabel.setText("Welcome back, " + u.getFullName() + "!");
        List<Complaint> list = complaintService.getUserComplaints(u.getId());

        int total = list.size();
        int pending = 0;
        int inProgress = 0;
        int resolved = 0;

        for (Complaint c : list) {
            if (c.getStatus() == ComplaintStatus.SUBMITTED || c.getStatus() == ComplaintStatus.UNDER_REVIEW || c.getStatus() == ComplaintStatus.ASSIGNED) {
                pending++;
            } else if (c.getStatus() == ComplaintStatus.IN_PROGRESS) {
                inProgress++;
            } else if (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED) {
                resolved++;
            }
        }

        totalCard.setValue(String.valueOf(total));
        pendingCard.setValue(String.valueOf(pending));
        inProgressCard.setValue(String.valueOf(inProgress));
        resolvedCard.setValue(String.valueOf(resolved));

        // Populate recent
        recentModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int count = 0;
        for (Complaint c : list) {
            if (count++ >= 8) break;
            recentModel.addRow(new Object[]{
                    c.getId(),
                    c.getTitle(),
                    c.getCategory() != null ? c.getCategory().getDisplayName() : "N/A",
                    c.getPriority(),
                    c.getStatus(),
                    c.getSlaStatus(),
                    c.getCreatedAt() != null ? sdf.format(c.getCreatedAt()) : ""
            });
        }

        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        User u = AuthService.getCurrentUser();
        if (u != null) {
            int unread = notificationService.getUnreadCount(u.getId());
            btnNotifications.setText("  🔔  Notifications" + (unread > 0 ? " (" + unread + ")" : ""));
        }
    }
}
