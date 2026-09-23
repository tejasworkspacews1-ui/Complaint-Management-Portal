package com.smartcomplaint.ui.views;

import com.smartcomplaint.dao.DepartmentDAO;
import com.smartcomplaint.dao.OfficerDAO;
import com.smartcomplaint.dao.UserDAO;
import com.smartcomplaint.model.*;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.service.ReportService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class AdminDashboardView extends JPanel {
    private final ComplaintService complaintService = new ComplaintService();
    private final ReportService reportService = new ReportService();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final OfficerDAO officerDAO = new OfficerDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Frame parentFrame;
    private final Runnable onLogout;

    private JPanel contentContainer;
    private CardLayout cardLayout;

    private SidebarButton btnOverview;
    private SidebarButton btnBreachCenter;
    private SidebarButton btnAllComplaints;
    private SidebarButton btnDepartments;
    private SidebarButton btnUsers;
    private SidebarButton btnAnalytics;
    private SidebarButton btnProfile;

    private StatCard statTotal;
    private StatCard statPending;
    private StatCard statResolved;
    private StatCard statBreached;
    private StatCard statCompliance;

    private JPanel overviewPanel;
    private JPanel breachPanel;
    private JPanel complaintsPanel;
    private JPanel deptsPanel;
    private JPanel usersPanel;
    private JPanel analyticsPanel;

    private ModernTable breachTable;
    private DefaultTableModel breachModel;

    private ModernTable masterTable;
    private DefaultTableModel masterModel;
    private ModernTextField masterSearchField;
    private JComboBox<String> masterStatusFilter;

    private ModernTable deptTable;
    private DefaultTableModel deptModel;
    private ModernTable officerTable;
    private DefaultTableModel officerModel;

    private ModernTable userTable;
    private DefaultTableModel userModel;

    public AdminDashboardView(Frame parentFrame, Runnable onLogout) {
        this.parentFrame = parentFrame;
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(Theme.BG_MAIN);

        overviewPanel = createOverviewPanel();
        contentContainer.add(overviewPanel, "OVERVIEW");

        breachPanel = createBreachPanel();
        contentContainer.add(breachPanel, "BREACH");

        complaintsPanel = createComplaintsPanel();
        contentContainer.add(complaintsPanel, "COMPLAINTS");

        deptsPanel = createDepartmentsPanel();
        contentContainer.add(deptsPanel, "DEPTS");

        usersPanel = createUsersPanel();
        contentContainer.add(usersPanel, "USERS");

        analyticsPanel = createAnalyticsPanel();
        contentContainer.add(analyticsPanel, "ANALYTICS");

        add(contentContainer, BorderLayout.CENTER);
    }
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        JPanel brand = new JPanel(new BorderLayout(8, 4));
        brand.setOpaque(false);
        JLabel logo = new JLabel("🏛️ SmartPortal");
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Color.WHITE);
        
        JPanel rolePanel = new JPanel(new GridLayout(2, 1));
        rolePanel.setOpaque(false);
        User cur = AuthService.getCurrentUser();
        boolean isSuperAdmin = cur != null && cur.getRole() == Role.ADMIN;

        JLabel role = new JLabel(isSuperAdmin ? "  SUPER ADMIN CONTROL" : "  MODERATOR PORTAL");
        role.setFont(Theme.FONT_SMALL_BOLD);
        role.setForeground(Theme.DANGER);
        
        String joinCode = (cur != null && cur.getTenantId() != null) ? cur.getTenantId() : "DEFAULT";
        JLabel spaceLbl = new JLabel(isSuperAdmin ? "  Master View" : "  Join Code: " + joinCode);
        spaceLbl.setFont(Theme.FONT_SMALL_BOLD);
        spaceLbl.setForeground(Theme.WARNING);
        
        rolePanel.add(role);
        rolePanel.add(spaceLbl);
        
        brand.add(logo, BorderLayout.NORTH);
        brand.add(rolePanel, BorderLayout.SOUTH);

        JPanel menu = new JPanel(new GridLayout(8, 1, 0, 6));
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(24, 0, 24, 0));

        btnOverview = new SidebarButton("Overview & KPIs", "📊");
        btnBreachCenter = new SidebarButton("SLA Breach Center", "🚨");
        btnAllComplaints = new SidebarButton("Master Complaints", "🗂");
        btnDepartments = new SidebarButton("Depts & Officers", "🏢");
        btnUsers = new SidebarButton("User Accounts", "👥");
        btnAnalytics = new SidebarButton("Reports & Export", "📈");
        btnProfile = new SidebarButton("Admin Profile", "👤");

        btnOverview.setActive(true);

        btnOverview.addActionListener(e -> selectView("OVERVIEW", btnOverview));
        btnBreachCenter.addActionListener(e -> {
            refreshBreachData();
            selectView("BREACH", btnBreachCenter);
        });
        btnAllComplaints.addActionListener(e -> {
            refreshMasterComplaints();
            selectView("COMPLAINTS", btnAllComplaints);
        });
        btnDepartments.addActionListener(e -> {
            refreshDeptData();
            selectView("DEPTS", btnDepartments);
        });
        btnUsers.addActionListener(e -> {
            refreshUserData();
            selectView("USERS", btnUsers);
        });
        btnAnalytics.addActionListener(e -> {
            refreshAnalyticsData();
            selectView("ANALYTICS", btnAnalytics);
        });
        btnProfile.addActionListener(e -> new ProfileDialog(parentFrame).setVisible(true));

        menu.add(btnOverview);
        menu.add(btnBreachCenter);
        menu.add(btnAllComplaints);
        menu.add(btnDepartments);
        menu.add(btnUsers);
        menu.add(btnAnalytics);
        menu.add(btnProfile);

        SidebarButton btnLogout = new SidebarButton("Logout", "🚪");
        btnLogout.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "Sign out from Administrator Panel?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
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

    private void selectView(String name, SidebarButton activeBtn) {
        btnOverview.setActive(btnOverview == activeBtn);
        btnBreachCenter.setActive(btnBreachCenter == activeBtn);
        btnAllComplaints.setActive(btnAllComplaints == activeBtn);
        btnDepartments.setActive(btnDepartments == activeBtn);
        btnUsers.setActive(btnUsers == activeBtn);
        btnAnalytics.setActive(btnAnalytics == activeBtn);
        btnProfile.setActive(btnProfile == activeBtn);

        cardLayout.show(contentContainer, name);
    }
    private JPanel createOverviewPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 18));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        User cur = AuthService.getCurrentUser();
        boolean isSuperAdmin = cur != null && cur.getRole() == Role.ADMIN;
        JLabel title = new JLabel(isSuperAdmin ? "📊 Super Admin Portal Overview" : "📊 Moderator Portal Overview");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Real-time municipal grievance metrics and SLA compliance status.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        top.add(title, BorderLayout.NORTH);
        top.add(sub, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);

        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        statsGrid.setOpaque(false);

        statTotal = new StatCard("Total Registered", "0", "System complaints", Theme.PRIMARY, "📑");
        statPending = new StatCard("Pending / Active", "0", "Requires attention", Theme.WARNING, "⏳");
        statResolved = new StatCard("Resolved", "0", "Completed tickets", Theme.SUCCESS, "✔");
        statBreached = new StatCard("SLA Breached", "0", "Overdue deadlines", Theme.DANGER, "🚨");
        statCompliance = new StatCard("SLA Compliance", "0%", "Target ≥ 90%", Theme.ACCENT, "🎯");

        statsGrid.add(statTotal);
        statsGrid.add(statPending);
        statsGrid.add(statResolved);
        statsGrid.add(statBreached);
        statsGrid.add(statCompliance);
        center.add(statsGrid, BorderLayout.NORTH);

        CardPanel summaryCard = new CardPanel(new BorderLayout(10, 12), 14);
        summaryCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel sumTitle = new JLabel("System Management Actions");
        sumTitle.setFont(Theme.FONT_TITLE);
        summaryCard.add(sumTitle, BorderLayout.NORTH);

        JPanel actionBtnGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        actionBtnGrid.setOpaque(false);

        ModernButton b1 = ModernButton.danger("🚨 View Breached Complaints");
        b1.addActionListener(e -> {
            refreshBreachData();
            selectView("BREACH", btnBreachCenter);
        });

        ModernButton b2 = new ModernButton("🗂 Master Complaint Directory");
        b2.addActionListener(e -> {
            refreshMasterComplaints();
            selectView("COMPLAINTS", btnAllComplaints);
        });

        ModernButton b3 = ModernButton.success("🏢 Manage Departments & Officers");
        b3.addActionListener(e -> {
            refreshDeptData();
            selectView("DEPTS", btnDepartments);
        });

        ModernButton b4 = ModernButton.warning("📈 Analytics & CSV Export");
        b4.addActionListener(e -> {
            refreshAnalyticsData();
            selectView("ANALYTICS", btnAnalytics);
        });

        actionBtnGrid.add(b1);
        actionBtnGrid.add(b2);
        actionBtnGrid.add(b3);
        actionBtnGrid.add(b4);
        summaryCard.add(actionBtnGrid, BorderLayout.CENTER);

        center.add(summaryCard, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        return p;
    }

    private JPanel createBreachPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 18));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("🚨 SLA Breach Escalation Center");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.DANGER);
        JLabel sub = new JLabel("Complaints that have exceeded their SLA resolution deadline without being resolved.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        top.add(title, BorderLayout.NORTH);
        top.add(sub, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        CardPanel card = new CardPanel(new BorderLayout(10, 12), 14);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        ctrl.setOpaque(false);
        ModernButton refBtn = ModernButton.neutral("🔄 Refresh Breaches");
        refBtn.addActionListener(e -> refreshBreachData());

        ModernButton reassignBtn = ModernButton.danger("⚡ Reassign & Escalate");
        reassignBtn.addActionListener(e -> openReassignModal(breachTable));

        ctrl.add(refBtn);
        ctrl.add(reassignBtn);
        card.add(ctrl, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Category", "Priority", "Status", "SLA Status", "Overdue By", "Assigned Officer", "Department"};
        breachModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        breachTable = new ModernTable(breachModel);
        JScrollPane scroll = new JScrollPane(breachTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        p.add(card, BorderLayout.CENTER);
        return p;
    }
    private JPanel createComplaintsPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 18));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);
        JLabel title = new JLabel("🗂 Master Complaint Management");
        title.setFont(Theme.FONT_TITLE_LARGE);
        top.add(title, BorderLayout.NORTH);

        CardPanel filterCard = new CardPanel(new BorderLayout(10, 0), 10);
        filterCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        masterSearchField = new ModernTextField("Search by ID, title, citizen, location...");
        masterSearchField.addActionListener(e -> refreshMasterComplaints());

        JPanel rightF = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightF.setOpaque(false);

        masterStatusFilter = new JComboBox<>(new String[]{"All Statuses", "SUBMITTED", "UNDER_REVIEW", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED", "REJECTED"});
        masterStatusFilter.addActionListener(e -> refreshMasterComplaints());

        ModernButton refBtn = ModernButton.neutral("🔄 Refresh");
        refBtn.addActionListener(e -> refreshMasterComplaints());

        ModernButton reassignBtn = new ModernButton("🏢 Reassign Dept/Officer");
        reassignBtn.addActionListener(e -> openReassignModal(masterTable));

        rightF.add(new JLabel("Status:"));
        rightF.add(masterStatusFilter);
        rightF.add(refBtn);
        rightF.add(reassignBtn);

        filterCard.add(masterSearchField, BorderLayout.CENTER);
        filterCard.add(rightF, BorderLayout.EAST);
        top.add(filterCard, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        CardPanel card = new CardPanel(new BorderLayout(), 12);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Title", "Category", "Priority", "Status", "SLA Status", "Time Rem.", "Citizen", "Department", "Officer"};
        masterModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        masterTable = new ModernTable(masterModel);
        JScrollPane scroll = new JScrollPane(masterTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private JPanel createDepartmentsPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 18));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("🏢 Departments & Officers Management");
        title.setFont(Theme.FONT_TITLE_LARGE);
        top.add(title, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton addDeptBtn = new ModernButton("+ Add Department");
        addDeptBtn.addActionListener(e -> openAddDepartmentModal());

        ModernButton addOfficerBtn = ModernButton.success("+ Add Officer");
        addOfficerBtn.addActionListener(e -> openAddOfficerModal());

        btnRow.add(addDeptBtn);
        btnRow.add(addOfficerBtn);
        top.add(btnRow, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.4);
        split.setDividerSize(8);

        CardPanel dCard = new CardPanel(new BorderLayout(8, 8), 12);
        dCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        dCard.add(new JLabel("Municipal Departments"), BorderLayout.NORTH);
        deptModel = new DefaultTableModel(new String[]{"ID", "Department Name", "Code", "Description", "Active Grievances"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        deptTable = new ModernTable(deptModel);
        dCard.add(new JScrollPane(deptTable), BorderLayout.CENTER);
        split.setTopComponent(dCard);

        CardPanel oCard = new CardPanel(new BorderLayout(8, 8), 12);
        oCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        oCard.add(new JLabel("Registered Field & Grievance Officers"), BorderLayout.NORTH);
        officerModel = new DefaultTableModel(new String[]{"Officer ID", "Full Name", "Employee Code", "Designation", "Department", "Email", "Active Tickets", "Resolved Tickets"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        officerTable = new ModernTable(officerModel);
        oCard.add(new JScrollPane(officerTable), BorderLayout.CENTER);
        split.setBottomComponent(oCard);

        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel createUsersPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 18));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("👥 User Account Administration");
        title.setFont(Theme.FONT_TITLE_LARGE);
        top.add(title, BorderLayout.NORTH);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        ModernButton refBtn = ModernButton.neutral("🔄 Refresh Users");
        refBtn.addActionListener(e -> refreshUserData());
        rightBtns.add(refBtn);
        top.add(rightBtns, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        CardPanel card = new CardPanel(new BorderLayout(), 12);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        userModel = new DefaultTableModel(new String[]{"User ID", "Username", "Full Name", "Email", "Phone", "Role", "Status", "Joined On"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new ModernTable(userModel);
        card.add(new JScrollPane(userTable), BorderLayout.CENTER);

        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private JPanel createAnalyticsPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 18));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("📈 Analytics & Grievance Export Center");
        title.setFont(Theme.FONT_TITLE_LARGE);
        top.add(title, BorderLayout.NORTH);

        ModernButton exportBtn = ModernButton.success("📥 Export All Complaints to CSV");
        exportBtn.addActionListener(e -> exportComplaintsCSV());
        top.add(exportBtn, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        CardPanel centerCard = new CardPanel(new BorderLayout(12, 14), 14);
        centerCard.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextArea statsReportArea = new JTextArea();
        statsReportArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        statsReportArea.setEditable(false);
        statsReportArea.setBackground(new Color(248, 250, 252));
        statsReportArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        centerCard.add(new JLabel("Comprehensive System Audit & SLA Breakdown:"), BorderLayout.NORTH);
        centerCard.add(new JScrollPane(statsReportArea), BorderLayout.CENTER);

        p.add(centerCard, BorderLayout.CENTER);

        p.putClientProperty("reportArea", statsReportArea);
        return p;
    }
    public void refreshDashboard() {
        complaintService.triggerSLACheck();
        ReportService.SystemStats stats = reportService.getOverallStatistics();

        statTotal.setValue(String.valueOf(stats.total));
        statPending.setValue(String.valueOf(stats.pending));
        statResolved.setValue(String.valueOf(stats.resolved));
        statBreached.setValue(String.valueOf(stats.breached));
        statCompliance.setValue(String.format("%.1f%%", stats.slaComplianceRate));

        btnBreachCenter.setText("  🚨  SLA Breach Center" + (stats.breached > 0 ? " (" + stats.breached + ")" : ""));
    }

    public void refreshBreachData() {
        complaintService.triggerSLACheck();
        breachModel.setRowCount(0);
        List<Complaint> breaches = complaintService.getBreachedComplaints();
        for (Complaint c : breaches) {
            breachModel.addRow(new Object[]{
                    c.getId(),
                    c.getTitle(),
                    c.getCategory() != null ? c.getCategory().getDisplayName() : "N/A",
                    c.getPriority(),
                    c.getStatus(),
                    c.getSlaStatus(),
                    c.getRemainingTimeFormatted(),
                    c.getAssignedOfficerName() != null ? c.getAssignedOfficerName() : "Unassigned",
                    c.getAssignedDepartmentName() != null ? c.getAssignedDepartmentName() : "N/A"
            });
        }
        btnBreachCenter.setText("  🚨  SLA Breach Center" + (breaches.size() > 0 ? " (" + breaches.size() + ")" : ""));
    }

    public void refreshMasterComplaints() {
        complaintService.triggerSLACheck();
        masterModel.setRowCount(0);
        String search = masterSearchField.getText().trim().toLowerCase();
        String statusSel = (String) masterStatusFilter.getSelectedItem();

        List<Complaint> all = complaintService.getAllComplaints();
        for (Complaint c : all) {
            boolean matchSearch = search.isEmpty() ||
                    c.getId().toLowerCase().contains(search) ||
                    c.getTitle().toLowerCase().contains(search) ||
                    c.getLocation().toLowerCase().contains(search) ||
                    (c.getCitizenName() != null && c.getCitizenName().toLowerCase().contains(search));

            boolean matchStatus = "All Statuses".equals(statusSel) || c.getStatus().name().equals(statusSel);

            if (matchSearch && matchStatus) {
                masterModel.addRow(new Object[]{
                        c.getId(),
                        c.getTitle(),
                        c.getCategory() != null ? c.getCategory().getDisplayName() : "N/A",
                        c.getPriority(),
                        c.getStatus(),
                        c.getSlaStatus(),
                        c.getRemainingTimeFormatted(),
                        c.isAnonymous() ? "Anonymous" : (c.getCitizenName() != null ? c.getCitizenName() : "N/A"),
                        c.getAssignedDepartmentName() != null ? c.getAssignedDepartmentName() : "N/A",
                        c.getAssignedOfficerName() != null ? c.getAssignedOfficerName() : "Unassigned"
                });
            }
        }
    }

    public void refreshDeptData() {
        deptModel.setRowCount(0);
        List<Department> depts = departmentDAO.getAllDepartments();
        for (Department d : depts) {
            deptModel.addRow(new Object[]{
                    d.getId(),
                    d.getName(),
                    d.getCode(),
                    d.getDescription(),
                    d.getActiveComplaintsCount()
            });
        }

        officerModel.setRowCount(0);
        List<Officer> officers = officerDAO.getAllOfficers();
        for (Officer o : officers) {
            officerModel.addRow(new Object[]{
                    o.getId(),
                    o.getFullName(),
                    o.getEmployeeCode(),
                    o.getDesignation(),
                    o.getDepartmentName(),
                    o.getEmail(),
                    o.getActiveComplaints(),
                    o.getResolvedComplaints()
            });
        }
    }

    public void refreshUserData() {
        userModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (User u : users) {
            userModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getPhone(),
                    u.getRole().getDisplayName(),
                    u.getStatus(),
                    u.getCreatedAt() != null ? sdf.format(u.getCreatedAt()) : ""
            });
        }
    }

    public void refreshAnalyticsData() {
        ReportService.SystemStats stats = reportService.getOverallStatistics();
        Map<ComplaintCategory, Integer> dist = reportService.getCategoryDistribution();

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================================\n");
        sb.append("            SMART COMPLAINT PORTAL - EXECUTIVE ANALYTICS REPORT\n");
        sb.append("=========================================================================\n\n");
        sb.append(String.format("Total Grievances Registered  : %d\n", stats.total));
        sb.append(String.format("Pending / Under Review       : %d\n", stats.pending));
        sb.append(String.format("Currently In Progress        : %d\n", stats.inProgress));
        sb.append(String.format("Resolved & Closed Tickets    : %d\n", stats.resolved));
        sb.append(String.format("SLA Breached (Overdue)       : %d\n", stats.breached));
        sb.append(String.format("Resolution Rate              : %.2f%%\n", stats.resolutionRate));
        sb.append(String.format("Overall SLA Compliance Rate  : %.2f%%\n\n", stats.slaComplianceRate));

        sb.append("-------------------------------------------------------------------------\n");
        sb.append("CATEGORY DISTRIBUTION BREAKDOWN:\n");
        sb.append("-------------------------------------------------------------------------\n");
        for (Map.Entry<ComplaintCategory, Integer> e : dist.entrySet()) {
            sb.append(String.format("  %-35s : %d complaints\n", e.getKey().getDisplayName(), e.getValue()));
        }

        JTextArea area = (JTextArea) analyticsPanel.getClientProperty("reportArea");
        if (area != null) {
            area.setText(sb.toString());
        }
    }

    private void openReassignModal(ModernTable sourceTable) {
        int row = sourceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint row to reassign.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String complaintId = (String) sourceTable.getValueAt(row, 0);
        Complaint c = complaintService.trackComplaint(complaintId);
        if (c == null) return;

        JDialog dialog = new JDialog(parentFrame, "Reassign & Escalate: " + c.getId(), true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        CardPanel card = new CardPanel(new BorderLayout(10, 12), 12);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 12));
        form.setOpaque(false);

        List<Department> depts = departmentDAO.getAllDepartments();
        JComboBox<Department> deptCombo = new JComboBox<>(depts.toArray(new Department[0]));
        JComboBox<Officer> officerCombo = new JComboBox<>();

        Runnable updateOfficers = () -> {
            officerCombo.removeAllItems();
            Department d = (Department) deptCombo.getSelectedItem();
            if (d != null) {
                List<Officer> offs = officerDAO.getOfficersByDepartment(d.getId());
                for (Officer o : offs) officerCombo.addItem(o);
            }
        };

        deptCombo.addActionListener(e -> updateOfficers.run());
        updateOfficers.run();

        ModernTextField remarksField = new ModernTextField("Administrative re-assignment / escalation");

        form.add(new JLabel("Complaint ID:"));
        form.add(new JLabel(c.getId() + " (" + c.getTitle() + ")"));
        form.add(new JLabel("Assign Department:"));
        form.add(deptCombo);
        form.add(new JLabel("Assign Officer:"));
        form.add(officerCombo);
        form.add(new JLabel("Escalation Remarks:"));
        form.add(remarksField);

        card.add(form, BorderLayout.CENTER);

        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btm.setOpaque(false);
        ModernButton cancelBtn = ModernButton.neutral("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        ModernButton saveBtn = ModernButton.success("Reassign & Notify");
        saveBtn.addActionListener(e -> {
            Department d = (Department) deptCombo.getSelectedItem();
            Officer o = (Officer) officerCombo.getSelectedItem();
            if (d == null) return;

            User actionUser = AuthService.getCurrentUser();
            boolean ok = complaintService.assignOfficerAndDepartment(c.getId(), d.getId(), o != null ? o.getUserId() : null, remarksField.getText().trim(), actionUser);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Complaint reassigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshBreachData();
                refreshMasterComplaints();
                refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to reassign complaint.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btm.add(cancelBtn);
        btm.add(saveBtn);
        card.add(btm, BorderLayout.SOUTH);

        p.add(card, BorderLayout.CENTER);
        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    private void openAddDepartmentModal() {
        JDialog dialog = new JDialog(parentFrame, "Create New Municipal Department", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        CardPanel card = new CardPanel(new BorderLayout(10, 12), 12);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
        form.setOpaque(false);

        ModernTextField nameF = new ModernTextField("e.g. Parks & Gardens");
        ModernTextField codeF = new ModernTextField("e.g. PRK");
        ModernTextField descF = new ModernTextField("Description of duties");

        form.add(new JLabel("Department Name *:"));
        form.add(nameF);
        form.add(new JLabel("Department Code *:"));
        form.add(codeF);
        form.add(new JLabel("Description:"));
        form.add(descF);

        card.add(form, BorderLayout.CENTER);

        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        ModernButton save = ModernButton.success("Save Department");
        save.addActionListener(e -> {
            String name = nameF.getText().trim();
            String code = codeF.getText().trim();
            if (name.isEmpty() || code.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Code are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Department d = new Department(0, name, code, descF.getText().trim());
            if (departmentDAO.createDepartment(d)) {
                JOptionPane.showMessageDialog(dialog, "Department created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshDeptData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create department. Name/code may be duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btm.add(save);
        card.add(btm, BorderLayout.SOUTH);

        p.add(card, BorderLayout.CENTER);
        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    private void openAddOfficerModal() {
        JDialog dialog = new JDialog(parentFrame, "Register New Complaint Officer", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Theme.BG_MAIN);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        CardPanel card = new CardPanel(new BorderLayout(10, 12), 12);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 10));
        form.setOpaque(false);

        ModernTextField userF = new ModernTextField("Username");
        ModernPasswordField passF = new ModernPasswordField("Password");
        ModernTextField nameF = new ModernTextField("Full Name");
        ModernTextField emailF = new ModernTextField("Email Address");
        ModernTextField phoneF = new ModernTextField("Phone Number");

        List<Department> depts = departmentDAO.getAllDepartments();
        JComboBox<Department> deptCombo = new JComboBox<>(depts.toArray(new Department[0]));
        ModernTextField desigF = new ModernTextField("e.g. Senior Inspector");
        ModernTextField empCodeF = new ModernTextField("e.g. EMP-WTR-201");

        form.add(new JLabel("Username *:"));
        form.add(userF);
        form.add(new JLabel("Password *:"));
        form.add(passF);
        form.add(new JLabel("Full Name *:"));
        form.add(nameF);
        form.add(new JLabel("Email Address *:"));
        form.add(emailF);
        form.add(new JLabel("Department *:"));
        form.add(deptCombo);
        form.add(new JLabel("Designation *:"));
        form.add(desigF);
        form.add(new JLabel("Employee Code *:"));
        form.add(empCodeF);

        card.add(form, BorderLayout.CENTER);

        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        ModernButton save = ModernButton.success("Create Officer Account");
        save.addActionListener(e -> {
            String u = userF.getText().trim();
            String pass = new String(passF.getPassword());
            String nm = nameF.getText().trim();
            String em = emailF.getText().trim();
            Department d = (Department) deptCombo.getSelectedItem();
            String des = desigF.getText().trim();
            String emp = empCodeF.getText().trim();

            if (u.isEmpty() || pass.isEmpty() || nm.isEmpty() || em.isEmpty() || d == null || emp.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please complete all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User user = new User();
            user.setUsername(u);
            user.setFullName(nm);
            user.setEmail(em);
            user.setPhone(phoneF.getText().trim());

            boolean ok = officerDAO.createOfficer(user, pass, d.getId(), des, emp);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Officer created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshDeptData();
                refreshUserData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create officer. Username or code duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btm.add(save);
        card.add(btm, BorderLayout.SOUTH);

        p.add(card, BorderLayout.CENTER);
        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    private void exportComplaintsCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("SmartComplaints_Export_" + System.currentTimeMillis() + ".csv"));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            boolean ok = reportService.exportAllToCSV(f);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Complaints exported successfully to:\n" + f.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export CSV file.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
