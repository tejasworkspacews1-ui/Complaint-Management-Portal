package com.smartcomplaint.ui.components;

import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCard extends CardPanel {
    private final JLabel valLabel;
    private final JLabel titleLabel;
    private final JLabel subLabel;
    private final Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor, String iconEmoji) {
        super(new BorderLayout(10, 8), 14);
        this.accentColor = accentColor;
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setBackground(Theme.BG_CARD);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_SUBTITLE);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);

        JLabel iconLbl = new JLabel(iconEmoji);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(iconLbl, BorderLayout.EAST);

        valLabel = new JLabel(value);
        valLabel.setFont(Theme.FONT_STAT_VAL);
        valLabel.setForeground(Theme.TEXT_PRIMARY);

        subLabel = new JLabel(subtitle);
        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(Theme.TEXT_MUTED);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        centerPanel.setOpaque(false);
        centerPanel.add(valLabel);
        centerPanel.add(subLabel);

        add(topRow, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valLabel.setText(value);
    }

    public void setSubtitle(String text) {
        subLabel.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Accent line on left
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
        g2.dispose();
    }
}
