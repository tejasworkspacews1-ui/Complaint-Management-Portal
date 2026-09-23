package com.smartcomplaint.ui.components;

import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    private Color normalBg;
    private Color hoverBg;
    private Color pressedBg;
    private Color textColor = Color.WHITE;
    private int cornerRadius = 10;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isOutlined = false;

    public ModernButton(String text) {
        this(text, Theme.PRIMARY);
    }

    public ModernButton(String text, Color bg) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = bg.brighter();
        this.pressedBg = bg.darker();
        init();
    }

    public static ModernButton outlined(String text, Color color) {
        ModernButton btn = new ModernButton(text, Color.WHITE);
        btn.isOutlined = true;
        btn.textColor = color;
        btn.normalBg = Color.WHITE;
        btn.hoverBg = new Color(241, 245, 249);
        btn.pressedBg = new Color(226, 232, 240);
        return btn;
    }

    public static ModernButton success(String text) {
        return new ModernButton(text, Theme.SUCCESS);
    }

    public static ModernButton danger(String text) {
        return new ModernButton(text, Theme.DANGER);
    }

    public static ModernButton warning(String text) {
        return new ModernButton(text, Theme.WARNING);
    }

    public static ModernButton neutral(String text) {
        ModernButton btn = new ModernButton(text, new Color(226, 232, 240));
        btn.textColor = Theme.TEXT_PRIMARY;
        btn.hoverBg = new Color(203, 213, 225);
        btn.pressedBg = new Color(148, 163, 184);
        return btn;
    }

    private void init() {
        setFont(Theme.FONT_HEADER);
        setForeground(textColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bg = normalBg;
        if (!isEnabled()) {
            bg = new Color(203, 213, 225);
        } else if (isPressed) {
            bg = pressedBg;
        } else if (isHovered) {
            bg = hoverBg;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        if (isOutlined) {
            g2.setColor(textColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
