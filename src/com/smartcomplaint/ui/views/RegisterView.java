package com.smartcomplaint.ui.views;

import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.CardPanel;
import com.smartcomplaint.ui.components.ModernButton;
import com.smartcomplaint.ui.components.ModernPasswordField;
import com.smartcomplaint.ui.components.ModernTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterView extends JPanel {
    private final AuthService authService = new AuthService();
    private final Runnable onBackToLogin;

    private JRadioButton joinModeBtn;
    private JRadioButton createModeBtn;
    
    private ModernTextField joinCodeField;
    private ModernTextField spaceNameField;
    private JComboBox<String> spaceCategoryCombo;

    private ModernTextField fullNameField;
    private ModernTextField emailField;
    private ModernTextField usernameField;
    private ModernTextField phoneField;
    private ModernPasswordField passwordField;
    private ModernPasswordField confirmPasswordField;
    private JLabel errorLabel;
    
    private JPanel dynamicPanel;

    public RegisterView(Runnable onBackToLogin) {
        this.onBackToLogin = onBackToLogin;
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        CardPanel card = new CardPanel(new BorderLayout(15, 15), 16);
        card.setPreferredSize(new Dimension(500, 720));
        card.setBorder(new EmptyBorder(26, 32, 26, 32));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel title = new JLabel("📝 Portal Registration", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.PRIMARY_DARK);

        JLabel sub = new JLabel("Create a new organization space or join an existing one", SwingConstants.CENTER);
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Toggle Modes
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        togglePanel.setOpaque(false);
        joinModeBtn = new JRadioButton("Join Existing Space", true);
        createModeBtn = new JRadioButton("Create New Space", false);
        joinModeBtn.setOpaque(false);
        createModeBtn.setOpaque(false);
        joinModeBtn.setFont(Theme.FONT_BODY_BOLD);
        createModeBtn.setFont(Theme.FONT_BODY_BOLD);
        ButtonGroup group = new ButtonGroup();
        group.add(joinModeBtn);
        group.add(createModeBtn);
        togglePanel.add(joinModeBtn);
        togglePanel.add(createModeBtn);
        form.add(togglePanel);
        
        dynamicPanel = new JPanel();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));
        dynamicPanel.setOpaque(false);
        form.add(dynamicPanel);
        
        // Common Fields
        fullNameField = new ModernTextField("Full Name (e.g. John Doe)");
        emailField = new ModernTextField("Email address (e.g. john@example.com)");
        usernameField = new ModernTextField("Desired Username");
        phoneField = new ModernTextField("Phone Number (10 digits)");
        passwordField = new ModernPasswordField("Password (min 4 characters)");
        confirmPasswordField = new ModernPasswordField("Confirm Password");

        addFormField(form, "Full Name:", fullNameField);
        addFormField(form, "Email Address:", emailField);
        addFormField(form, "Username:", usernameField);
        addFormField(form, "Phone Number:", phoneField);
        addFormField(form, "Password:", passwordField);
        addFormField(form, "Confirm Password:", confirmPasswordField);

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(Theme.FONT_SMALL_BOLD);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);

        card.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 8));
        footer.setOpaque(false);

        ModernButton registerBtn = ModernButton.success("Create Account");
        registerBtn.addActionListener(e -> handleRegister());

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backRow.setOpaque(false);
        JButton backBtn = new JButton("← Back to Sign In");
        backBtn.setFont(Theme.FONT_BODY_BOLD);
        backBtn.setForeground(Theme.TEXT_SECONDARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            if (onBackToLogin != null) onBackToLogin.run();
        });
        backRow.add(backBtn);

        footer.add(registerBtn);
        footer.add(backRow);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
        
        joinCodeField = new ModernTextField("Enter 8-character join code");
        spaceNameField = new ModernTextField("Enter your organization name");
        spaceCategoryCombo = new JComboBox<>(new String[]{"Society", "Hospital", "School", "Corporate", "Public", "Other"});
        spaceCategoryCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        spaceCategoryCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        updateDynamicFields();
        
        joinModeBtn.addActionListener(e -> updateDynamicFields());
        createModeBtn.addActionListener(e -> updateDynamicFields());
    }
    
    private void updateDynamicFields() {
        dynamicPanel.removeAll();
        if (joinModeBtn.isSelected()) {
            addFormField(dynamicPanel, "Space Join Code (Leave empty for Public Default):", joinCodeField);
        } else {
            addFormField(dynamicPanel, "New Space Name:", spaceNameField);
            JLabel lbl = new JLabel("Category:");
            lbl.setFont(Theme.FONT_HEADER);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            dynamicPanel.add(lbl);
            dynamicPanel.add(Box.createVerticalStrut(2));
            dynamicPanel.add(spaceCategoryCombo);
            dynamicPanel.add(Box.createVerticalStrut(8));
        }
        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    private void addFormField(JPanel container, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_HEADER);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(label);
        container.add(Box.createVerticalStrut(2));
        container.add(field);
        container.add(Box.createVerticalStrut(8));
    }

    private void handleRegister() {
        errorLabel.setText(" ");
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String user = usernameField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String conf = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("All required fields must be filled.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }

        if (pass.length() < 4) {
            errorLabel.setText("Password must be at least 4 characters.");
            return;
        }

        if (!pass.equals(conf)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        if (joinModeBtn.isSelected()) {
            String joinCode = joinCodeField.getText().trim();
            if (joinCode.isEmpty() || joinCode.equals("Enter 8-character join code")) {
                joinCode = "DEFAULT";
            }
            boolean success = authService.registerCitizen(joinCode, user, pass, name, email, phone);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Joined successfully!\nPlease sign in with your new credentials.",
                        "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                if (onBackToLogin != null) onBackToLogin.run();
            } else {
                errorLabel.setText("Username/Email taken, or invalid Space Join Code.");
            }
        } else {
            String sName = spaceNameField.getText().trim();
            if (sName.isEmpty() || sName.equals("Enter your organization name")) {
                errorLabel.setText("Please provide a Space Name.");
                return;
            }
            String sCat = (String) spaceCategoryCombo.getSelectedItem();
            
            String newCode = authService.createSpaceAndAdmin(sName, sCat, user, pass, name, email, phone);
            if (newCode != null) {
                JOptionPane.showMessageDialog(this,
                        "Space created successfully!\n\nYour Space Join Code is: " + newCode + "\n\nShare this code with other members to join your space.",
                        "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                if (onBackToLogin != null) onBackToLogin.run();
            } else {
                errorLabel.setText("Failed to create Space. Username or Email already exists.");
            }
        }
    }
}
