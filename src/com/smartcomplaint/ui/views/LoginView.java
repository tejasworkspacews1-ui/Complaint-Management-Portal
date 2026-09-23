package com.smartcomplaint.ui.views;

import com.smartcomplaint.model.Role;
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
import java.awt.event.ItemEvent;

public class LoginView extends JPanel {
    private final AuthService authService = new AuthService();
    private final Runnable onRegisterClick;
    private final java.util.function.Consumer<User> onLoginSuccess;
    private final Frame parentFrame;

    private JComboBox<Role> roleCombo;
    private ModernTextField userField;
    private ModernPasswordField passField;
    private JCheckBox showPassCheck;
    private JLabel errorLabel;

    public LoginView(Frame parentFrame, Runnable onRegisterClick, java.util.function.Consumer<User> onLoginSuccess) {
        this.parentFrame = parentFrame;
        this.onRegisterClick = onRegisterClick;
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        CardPanel card = new CardPanel(new BorderLayout(20, 20), 16);
        card.setPreferredSize(new Dimension(460, 620));
        card.setBorder(new EmptyBorder(30, 36, 30, 36));

        // Top Header / Branding
        JPanel header = new JPanel(new GridLayout(3, 1, 0, 4));
        header.setOpaque(false);

        JLabel logo = new JLabel("🏛️ Smart Complaint Portal", SwingConstants.CENTER);
        logo.setFont(Theme.FONT_TITLE_LARGE);
        logo.setForeground(Theme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Civic Grievance Registration & SLA Management", SwingConstants.CENTER);
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        JLabel signin = new JLabel("Sign In to Your Account", SwingConstants.CENTER);
        signin.setFont(Theme.FONT_HEADER);
        signin.setForeground(Theme.TEXT_PRIMARY);

        header.add(logo);
        header.add(subtitle);
        header.add(signin);
        card.add(header, BorderLayout.NORTH);

        // Form Center
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Role Selector
        JLabel roleLbl = new JLabel("Login as:");
        roleLbl.setFont(Theme.FONT_HEADER);
        roleLbl.setForeground(Theme.TEXT_PRIMARY);
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleCombo = new JComboBox<>(Role.values());
        roleCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Role) {
                    l.setText(((Role) value).getDisplayName());
                }
                return l;
            }
        });
        roleCombo.setFont(Theme.FONT_BODY);
        roleCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username
        JLabel userLbl = new JLabel("Email / Username:");
        userLbl.setFont(Theme.FONT_HEADER);
        userLbl.setForeground(Theme.TEXT_PRIMARY);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        userField = new ModernTextField("Enter your username or email");
        userField.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(Theme.FONT_HEADER);
        passLbl.setForeground(Theme.TEXT_PRIMARY);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passField = new ModernPasswordField("Enter your password");
        passField.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Show password & forgot
        JPanel passOptions = new JPanel(new BorderLayout());
        passOptions.setOpaque(false);
        passOptions.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        passOptions.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setFont(Theme.FONT_SMALL);
        showPassCheck.setOpaque(false);
        showPassCheck.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                passField.setEchoChar((char) 0);
            } else {
                passField.setEchoChar('•');
            }
        });

        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setFont(Theme.FONT_SMALL);
        forgotBtn.setForeground(Theme.PRIMARY);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.addActionListener(e -> showForgotPasswordDialog());

        passOptions.add(showPassCheck, BorderLayout.WEST);
        passOptions.add(forgotBtn, BorderLayout.EAST);

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(Theme.FONT_SMALL_BOLD);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login Button
        ModernButton loginBtn = new ModernButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());

        // Enter key triggers login
        userField.addActionListener(e -> handleLogin());
        passField.addActionListener(e -> handleLogin());

        form.add(roleLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(roleCombo);
        form.add(Box.createVerticalStrut(12));
        form.add(userLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(userField);
        form.add(Box.createVerticalStrut(12));
        form.add(passLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(passField);
        form.add(Box.createVerticalStrut(6));
        form.add(passOptions);
        form.add(Box.createVerticalStrut(6));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(loginBtn);

        card.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 4));
        footer.setOpaque(false);

        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regRow.setOpaque(false);
        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setFont(Theme.FONT_BODY);
        JButton regBtn = new JButton("Register as Citizen");
        regBtn.setFont(Theme.FONT_BODY_BOLD);
        regBtn.setForeground(Theme.PRIMARY);
        regBtn.setBorderPainted(false);
        regBtn.setContentAreaFilled(false);
        regBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regBtn.addActionListener(e -> {
            if (onRegisterClick != null) onRegisterClick.run();
        });
        regRow.add(noAcc);
        regRow.add(regBtn);

        footer.add(regRow);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }



    private void handleLogin() {
        errorLabel.setText(" ");
        String username = userField.getText().trim();
        String pass = new String(passField.getPassword());
        Role role = (Role) roleCombo.getSelectedItem();

        if (username.isEmpty() || username.equals("Enter your username or email") || pass.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        User user = authService.login(username, pass, role);
        if (user != null) {
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
        } else {
            errorLabel.setText("Invalid credentials or role mismatch.");
        }
    }

    private void showForgotPasswordDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter your registered Email or Username to reset password:",
                "Forgot Password", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            String newPass = JOptionPane.showInputDialog(this,
                    "Enter new password for " + input + ":",
                    "Reset Password", JOptionPane.PLAIN_MESSAGE);
            if (newPass != null && newPass.length() >= 4) {
                boolean ok = authService.resetPassword(input.trim(), newPass);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Password reset successfully! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "User not found or database error.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (newPass != null) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
