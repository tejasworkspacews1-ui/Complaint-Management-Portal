package com.smartcomplaint.ui.components;

import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ModernPasswordField extends JPasswordField {
    private String placeholder = "";
    private boolean isFocused = false;

    public ModernPasswordField() {
        this("");
    }

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder;
        init();
    }

    private void init() {
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT_PRIMARY);
        setBackground(Color.WHITE);
        setCaretColor(Theme.PRIMARY);
        updateBorder();

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                updateBorder();
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                updateBorder();
                repaint();
            }
        });
    }

    private void updateBorder() {
        Color borderCol = isFocused ? Theme.PRIMARY : Theme.BORDER_COLOR;
        setBorder(new CompoundBorder(
                new LineBorder(borderCol, isFocused ? 2 : 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (placeholder != null && !placeholder.isEmpty() && getPassword().length == 0 && !isFocused) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(getFont());
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, insets.left, y);
            g2.dispose();
        }
    }
}
