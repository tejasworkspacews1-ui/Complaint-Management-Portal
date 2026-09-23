package com.smartcomplaint.ui.views;

import com.smartcomplaint.dao.UserDAO;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.CardPanel;
import com.smartcomplaint.ui.components.ModernButton;
import com.smartcomplaint.ui.components.ModernPasswordField;
import com.smartcomplaint.ui.components.ModernTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileDialog extends JDialog {
    private final UserDAO userDAO = new UserDAO();
    private ModernTextField nameField;
    private ModernTextField emailField;
    private ModernTextField phoneField;

    private ModernPasswordField oldPassField;
    private ModernPasswordField newPassField;
    private ModernPasswordField confirmPassField;

    public ProfileDialog(Frame parent) {
        super(parent, "My Profile & Security", true);
        setSize(520, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        User u = AuthService.getCurrentUser();
        if (u == null) return;

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Theme.BG_MAIN);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_HEADER);

        // Tab 1: Profile Details
        CardPanel pPanel = new CardPanel(new BorderLayout(10, 15), 10);
        pPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 12));
        form.setOpaque(false);

        JLabel uLbl = new JLabel(u.getUsername());
        uLbl.setFont(Theme.FONT_BODY_BOLD);

        JLabel rLbl = new JLabel(u.getRole().getDisplayName());
        rLbl.setFont(Theme.FONT_BODY_BOLD);

        nameField = new ModernTextField(u.getFullName() != null ? u.getFullName() : "");
        emailField = new ModernTextField(u.getEmail() != null ? u.getEmail() : "");
        phoneField = new ModernTextField(u.getPhone() != null ? u.getPhone() : "");

        form.add(new JLabel("Username:"));
        form.add(uLbl);
        form.add(new JLabel("Role:"));
        form.add(rLbl);
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        pPanel.add(form, BorderLayout.CENTER);

        ModernButton saveProfileBtn = ModernButton.success("Update Profile");
        saveProfileBtn.addActionListener(e -> {
            u.setFullName(nameField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setPhone(phoneField.getText().trim());
            if (userDAO.updateProfile(u)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pBtm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBtm.setOpaque(false);
        pBtm.add(saveProfileBtn);
        pPanel.add(pBtm, BorderLayout.SOUTH);

        tabs.addTab("👤 Profile Info", pPanel);

        // Tab 2: Change Password
        CardPanel passPanel = new CardPanel(new BorderLayout(10, 15), 10);
        passPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel passForm = new JPanel(new GridLayout(3, 2, 8, 12));
        passForm.setOpaque(false);

        oldPassField = new ModernPasswordField();
        newPassField = new ModernPasswordField();
        confirmPassField = new ModernPasswordField();

        passForm.add(new JLabel("Current Password:"));
        passForm.add(oldPassField);
        passForm.add(new JLabel("New Password:"));
        passForm.add(newPassField);
        passForm.add(new JLabel("Confirm New Password:"));
        passForm.add(confirmPassField);

        passPanel.add(passForm, BorderLayout.CENTER);

        ModernButton changePassBtn = new ModernButton("Change Password");
        changePassBtn.addActionListener(e -> {
            String oldP = new String(oldPassField.getPassword());
            String newP = new String(newPassField.getPassword());
            String confP = new String(confirmPassField.getPassword());

            if (!com.smartcomplaint.util.PasswordHasher.verifyPassword(oldP, u.getSalt(), u.getPasswordHash())) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newP.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newP.equals(confP)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (userDAO.updatePassword(u.getId(), newP)) {
                u.setPasswordHash(com.smartcomplaint.util.PasswordHasher.hashPassword(newP, u.getSalt()));
                JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                oldPassField.setText("");
                newPassField.setText("");
                confirmPassField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel passBtm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        passBtm.setOpaque(false);
        passBtm.add(changePassBtn);
        passPanel.add(passBtm, BorderLayout.SOUTH);

        tabs.addTab("🔑 Change Password", passPanel);

        content.add(tabs, BorderLayout.CENTER);
        setContentPane(content);
    }
}
