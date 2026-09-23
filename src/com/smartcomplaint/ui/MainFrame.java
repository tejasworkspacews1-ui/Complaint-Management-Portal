package com.smartcomplaint.ui;

import com.smartcomplaint.dao.DBConnection;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.ui.views.AdminDashboardView;
import com.smartcomplaint.ui.views.CitizenDashboardView;
import com.smartcomplaint.ui.views.LoginView;
import com.smartcomplaint.ui.views.OfficerDashboardView;
import com.smartcomplaint.ui.views.RegisterView;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout rootCardLayout;
    private JPanel rootPanel;

    private LoginView loginView;
    private RegisterView registerView;
    private CitizenDashboardView citizenView;
    private OfficerDashboardView officerView;
    private AdminDashboardView adminView;

    private Timer slaBackgroundTimer;
    private final ComplaintService complaintService = new ComplaintService();

    public MainFrame() {
        super("Smart Complaint Registration & Management Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        initDatabaseOnStartup();
        initUI();
        startSLATimer();
    }

    private void initDatabaseOnStartup() {
        new Thread(() -> {
            boolean ok = DBConnection.initDatabase();
            if (!ok) {
                System.err.println("Warning: Database auto-initialization deferred. Configure via Database Settings if needed.");
            }
        }).start();
    }

    private void initUI() {
        rootCardLayout = new CardLayout();
        rootPanel = new JPanel(rootCardLayout);
        rootPanel.setBackground(Theme.BG_MAIN);

        // 1. Login View
        loginView = new LoginView(this, this::showRegister, this::onLoginSuccess);
        rootPanel.add(loginView, "LOGIN");

        // 2. Register View
        registerView = new RegisterView(this::showLogin);
        rootPanel.add(registerView, "REGISTER");

        // 3. Citizen View
        citizenView = new CitizenDashboardView(this, this::showLogin);
        rootPanel.add(citizenView, "CITIZEN");

        // 4. Officer View
        officerView = new OfficerDashboardView(this, this::showLogin);
        rootPanel.add(officerView, "OFFICER");

        // 5. Admin View
        adminView = new AdminDashboardView(this, this::showLogin);
        rootPanel.add(adminView, "ADMIN");

        setContentPane(rootPanel);
        showLogin();
    }

    public void showLogin() {
        AuthService.logout();
        rootCardLayout.show(rootPanel, "LOGIN");
    }

    public void showRegister() {
        rootCardLayout.show(rootPanel, "REGISTER");
    }

    private void onLoginSuccess(User user) {
        if (user.getRole() == Role.CITIZEN) {
            citizenView.refreshDashboard();
            rootCardLayout.show(rootPanel, "CITIZEN");
        } else if (user.getRole() == Role.OFFICER) {
            officerView.refreshData();
            rootCardLayout.show(rootPanel, "OFFICER");
        } else if (user.getRole() == Role.ADMIN) {
            adminView.refreshDashboard();
            rootCardLayout.show(rootPanel, "ADMIN");
        }
    }

    private void startSLATimer() {
        // Run SLA monitor daemon every 60 seconds
        slaBackgroundTimer = new Timer(60000, e -> {
            new Thread(complaintService::triggerSLACheck).start();
        });
        slaBackgroundTimer.start();
    }
}
