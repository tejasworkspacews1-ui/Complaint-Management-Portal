package com.smartcomplaint.ui.views;

import com.smartcomplaint.dao.DBConnection;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.CardPanel;
import com.smartcomplaint.ui.components.ModernButton;
import com.smartcomplaint.ui.components.ModernPasswordField;
import com.smartcomplaint.ui.components.ModernTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DBSettingsDialog extends JDialog {
    private JComboBox<String> typeCombo;
    private ModernTextField hostField;
    private ModernTextField portField;
    private ModernTextField dbNameField;
    private ModernTextField userField;
    private ModernPasswordField passField;
    private JLabel statusLabel;

    public DBSettingsDialog(Frame parent) {
        super(parent, "Database Configuration & Setup", true);
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BG_MAIN);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        CardPanel card = new CardPanel(new BorderLayout(15, 15), 12);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel title = new JLabel("Database Connection Setup");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Configure MySQL Server or SQLite Embedded Database");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setOpaque(false);

        typeCombo = new JComboBox<>(new String[]{"mysql", "sqlite"});
        typeCombo.setSelectedItem(DBConnection.getDbType());

        hostField = new ModernTextField(DBConnection.getHost());
        portField = new ModernTextField(String.valueOf(DBConnection.getPort()));
        dbNameField = new ModernTextField(DBConnection.getDbName());
        userField = new ModernTextField(DBConnection.getUsername());
        passField = new ModernPasswordField(DBConnection.getPassword());

        typeCombo.addActionListener(e -> {
            boolean isMySql = "mysql".equals(typeCombo.getSelectedItem());
            hostField.setEnabled(isMySql);
            portField.setEnabled(isMySql);
            dbNameField.setEnabled(isMySql);
            userField.setEnabled(isMySql);
            passField.setEnabled(isMySql);
        });

        form.add(createLabel("Database Type:"));
        form.add(typeCombo);
        form.add(createLabel("Host:"));
        form.add(hostField);
        form.add(createLabel("Port:"));
        form.add(portField);
        form.add(createLabel("Database Name:"));
        form.add(dbNameField);
        form.add(createLabel("Username:"));
        form.add(userField);
        form.add(createLabel("Password:"));
        form.add(passField);

        card.add(form, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setOpaque(false);

        statusLabel = new JLabel("Ready to connect", SwingConstants.CENTER);
        statusLabel.setFont(Theme.FONT_SMALL_BOLD);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        bottom.add(statusLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton testBtn = ModernButton.neutral("Test Connection");
        ModernButton saveBtn = ModernButton.success("Save & Initialize");

        testBtn.addActionListener(e -> testConn());
        saveBtn.addActionListener(e -> saveAndInit());

        btnPanel.add(testBtn);
        btnPanel.add(saveBtn);
        bottom.add(btnPanel, BorderLayout.SOUTH);

        card.add(bottom, BorderLayout.SOUTH);
        content.add(card, BorderLayout.CENTER);
        setContentPane(content);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_HEADER);
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private void testConn() {
        String type = (String) typeCombo.getSelectedItem();
        String h = hostField.getText().trim();
        int p = 3306;
        try { p = Integer.parseInt(portField.getText().trim()); } catch (Exception ignored) {}
        String name = dbNameField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        statusLabel.setText("Testing connection...");
        statusLabel.setForeground(Theme.PRIMARY);

        boolean ok = DBConnection.testConnection(type, h, p, name, user, pass);
        if (ok) {
            statusLabel.setText("✔ Connection successful!");
            statusLabel.setForeground(Theme.SUCCESS);
        } else {
            statusLabel.setText("✖ Connection failed! Check credentials & service.");
            statusLabel.setForeground(Theme.DANGER);
        }
    }

    private void saveAndInit() {
        String type = (String) typeCombo.getSelectedItem();
        String h = hostField.getText().trim();
        int p = 3306;
        try { p = Integer.parseInt(portField.getText().trim()); } catch (Exception ignored) {}
        String name = dbNameField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        DBConnection.saveConfig(type, h, p, name, user, pass, "smart_complaint.db");
        statusLabel.setText("Initializing database tables and seed data...");

        boolean initialized = DBConnection.initDatabase();
        if (initialized) {
            JOptionPane.showMessageDialog(this,
                    "Database configured & initialized successfully with default seed data!",
                    "Database Ready", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize database. Please ensure MySQL service is running.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
