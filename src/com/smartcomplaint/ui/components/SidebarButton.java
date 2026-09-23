package com.smartcomplaint.ui.components;

import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarButton extends JButton {
    private boolean active = false;
    private boolean hovered = false;
    private final String iconEmoji;

    public SidebarButton(String text, String iconEmoji) {
        super("  " + iconEmoji + "  " + text);
        this.iconEmoji = iconEmoji;
        init();
    }

    private void init() {
        setFont(Theme.FONT_HEADER);
        setForeground(new Color(203, 213, 225));
        setHorizontalAlignment(SwingConstants.LEFT);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(12, 20, 12, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Color.WHITE : (hovered ? Color.WHITE : new Color(203, 213, 225)));
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (active) {
            g2.setColor(Theme.SIDEBAR_ACTIVE);
            g2.fillRoundRect(8, 2, w - 16, h - 4, 8, 8);
        } else if (hovered) {
            g2.setColor(Theme.SIDEBAR_HOVER);
            g2.fillRoundRect(8, 2, w - 16, h - 4, 8, 8);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
