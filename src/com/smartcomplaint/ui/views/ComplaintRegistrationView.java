package com.smartcomplaint.ui.views;

import com.smartcomplaint.model.*;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.CardPanel;
import com.smartcomplaint.ui.components.ModernButton;
import com.smartcomplaint.ui.components.ModernTextField;
import com.smartcomplaint.ui.components.StatusBadge;
import com.smartcomplaint.util.SLAEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

public class ComplaintRegistrationView extends JPanel {
    private final ComplaintService complaintService = new ComplaintService();
    private final Consumer<String> onTrackRequested;

    private ModernTextField titleField;
    private JComboBox<ComplaintCategory> categoryCombo;
    private JComboBox<String> subCategoryCombo;
    private ModernTextField locationField;
    private JComboBox<ComplaintPriority> priorityCombo;
    private JLabel slaPreviewLabel;
    private JTextArea descArea;
    private ModernTextField attachmentField;
    private JCheckBox anonymousCheck;

    public ComplaintRegistrationView(Consumer<String> onTrackRequested) {
        this.onTrackRequested = onTrackRequested;
        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        // Top Banner
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("📝 Register a New Civic Complaint");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Fill in the details below. Our automated SLA system will track and escalate your grievance.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        top.add(title, BorderLayout.NORTH);
        top.add(sub, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Center Card Form
        CardPanel card = new CardPanel(new BorderLayout(15, 15), 14);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Fields
        titleField = new ModernTextField("Brief title of the issue (e.g. Water pipe burst near school)");
        
        categoryCombo = new JComboBox<>(ComplaintCategory.values());
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ComplaintCategory) {
                    l.setText(((ComplaintCategory) value).getDisplayName());
                }
                return l;
            }
        });

        subCategoryCombo = new JComboBox<>();
        categoryCombo.addActionListener(e -> updateSubCategories());

        locationField = new ModernTextField("Street, Ward, Landmark, City/Town");

        priorityCombo = new JComboBox<>(ComplaintPriority.values());
        priorityCombo.setSelectedItem(ComplaintPriority.MEDIUM);

        slaPreviewLabel = new JLabel("⚡ Expected SLA: 5 Days resolution window");
        slaPreviewLabel.setFont(Theme.FONT_SMALL_BOLD);
        slaPreviewLabel.setForeground(Theme.PRIMARY);

        priorityCombo.addActionListener(e -> {
            ComplaintPriority p = (ComplaintPriority) priorityCombo.getSelectedItem();
            if (p != null) {
                slaPreviewLabel.setText("⚡ Expected SLA Deadline: " + p.getSlaLabel() + " resolution guarantee");
                slaPreviewLabel.setForeground(p.getColor());
            }
        });

        descArea = new JTextArea(4, 30);
        descArea.setFont(Theme.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));

        attachmentField = new ModernTextField("No file selected (Optional)");
        attachmentField.setEditable(false);
        ModernButton browseBtn = ModernButton.neutral("📁 Browse File...");
        browseBtn.addActionListener(e -> chooseAttachment());

        JPanel attachPanel = new JPanel(new BorderLayout(8, 0));
        attachPanel.setOpaque(false);
        attachPanel.add(attachmentField, BorderLayout.CENTER);
        attachPanel.add(browseBtn, BorderLayout.EAST);

        anonymousCheck = new JCheckBox("Submit Anonymously (Hide my personal name/contact info from public officers)");
        anonymousCheck.setFont(Theme.FONT_BODY);
        anonymousCheck.setOpaque(false);

        // Populate GridBag
        int r = 0;
        addFormRow(formGrid, gbc, r++, "Complaint Title *:", titleField);
        addFormRow(formGrid, gbc, r++, "Category *:", categoryCombo);
        addFormRow(formGrid, gbc, r++, "Sub-Category *:", subCategoryCombo);
        addFormRow(formGrid, gbc, r++, "Location / Landmark *:", locationField);

        // Priority + SLA Preview
        JPanel prioPanel = new JPanel(new BorderLayout(10, 4));
        prioPanel.setOpaque(false);
        prioPanel.add(priorityCombo, BorderLayout.WEST);
        prioPanel.add(slaPreviewLabel, BorderLayout.CENTER);
        addFormRow(formGrid, gbc, r++, "Priority *:", prioPanel);

        addFormRow(formGrid, gbc, r++, "Detailed Description *:", descScroll);
        addFormRow(formGrid, gbc, r++, "Attachment (Photo/Doc):", attachPanel);
        addFormRow(formGrid, gbc, r++, "", anonymousCheck);

        card.add(formGrid, BorderLayout.CENTER);

        // Action Buttons Bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        ModernButton resetBtn = ModernButton.neutral("Reset Form");
        ModernButton submitBtn = ModernButton.success("🚀 Submit Complaint");

        resetBtn.addActionListener(e -> resetForm());
        submitBtn.addActionListener(e -> submitComplaint());

        btnPanel.add(resetBtn);
        btnPanel.add(submitBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        updateSubCategories();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.2;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_HEADER);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        panel.add(comp, gbc);
    }

    private void updateSubCategories() {
        subCategoryCombo.removeAllItems();
        ComplaintCategory cat = (ComplaintCategory) categoryCombo.getSelectedItem();
        if (cat != null) {
            for (String sub : cat.getSubCategories()) {
                subCategoryCombo.addItem(sub);
            }
        }
    }

    private void chooseAttachment() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            attachmentField.setText(f.getAbsolutePath());
        }
    }

    private void resetForm() {
        titleField.setText("");
        locationField.setText("");
        descArea.setText("");
        attachmentField.setText("");
        anonymousCheck.setSelected(false);
        categoryCombo.setSelectedIndex(0);
        priorityCombo.setSelectedItem(ComplaintPriority.MEDIUM);
    }

    private void submitComplaint() {
        String title = titleField.getText().trim();
        ComplaintCategory cat = (ComplaintCategory) categoryCombo.getSelectedItem();
        String subCat = (String) subCategoryCombo.getSelectedItem();
        String loc = locationField.getText().trim();
        ComplaintPriority prio = (ComplaintPriority) priorityCombo.getSelectedItem();
        String desc = descArea.getText().trim();
        String attach = attachmentField.getText().trim();
        boolean anon = anonymousCheck.isSelected();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a Complaint Title.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }

        if (loc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Location where the issue occurred.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            locationField.requestFocus();
            return;
        }

        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write a detailed description of the grievance.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            descArea.requestFocus();
            return;
        }

        User u = AuthService.getCurrentUser();

        Complaint c = new Complaint();
        if (u != null) {
            c.setUserId(u.getId());
            c.setCitizenName(u.getFullName());
            c.setCitizenEmail(u.getEmail());
            c.setCitizenPhone(u.getPhone());
        }
        c.setTitle(title);
        c.setCategory(cat);
        c.setSubCategory(subCat);
        c.setLocation(loc);
        c.setPriority(prio);
        c.setDescription(desc);
        c.setAttachmentPath(attach.equals("No file selected (Optional)") ? null : attach);
        c.setAnonymous(anon);
        c.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        c.setSlaDeadline(SLAEngine.calculateDeadline(c.getCreatedAt(), prio));
        c.setStatus(ComplaintStatus.SUBMITTED);
        c.setSlaStatus(SLAStatus.WITHIN_SLA);

        boolean success = complaintService.registerComplaint(c);
        if (success) {
            showConfirmationDialog(c);
            resetForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to submit complaint. Database error.", "Submission Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showConfirmationDialog(Complaint c) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setPreferredSize(new Dimension(420, 260));

        JPanel header = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("✔ Complaint Registered Successfully!", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.SUCCESS);
        JLabel sub = new JLabel("Your reference ID has been generated for tracking.", SwingConstants.CENTER);
        sub.setFont(Theme.FONT_SMALL);
        header.add(title);
        header.add(sub);
        p.add(header, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(5, 2, 8, 8));
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        info.add(new JLabel("Complaint ID:"));
        JLabel idLbl = new JLabel(c.getId());
        idLbl.setFont(Theme.FONT_HEADER);
        idLbl.setForeground(Theme.PRIMARY);
        info.add(idLbl);

        info.add(new JLabel("Category:"));
        info.add(new JLabel(c.getCategory().getDisplayName()));

        info.add(new JLabel("Priority:"));
        info.add(new JLabel(c.getPriority().getDisplayName() + " (" + c.getPriority().getSlaLabel() + ")"));

        info.add(new JLabel("SLA Deadline:"));
        info.add(new JLabel(sdf.format(c.getSlaDeadline())));

        info.add(new JLabel("Initial Status:"));
        info.add(StatusBadge.forStatus(c.getStatus()));

        p.add(info, BorderLayout.CENTER);

        Object[] options = {"Track Complaint Now", "Close"};
        int choice = JOptionPane.showOptionDialog(this, p, "Submission Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0 && onTrackRequested != null) {
            onTrackRequested.accept(c.getId());
        }
    }
}
