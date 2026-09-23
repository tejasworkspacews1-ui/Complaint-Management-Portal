package com.smartcomplaint.ui.components;

import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {
    private int cornerRadius = 16;
    private Color backgroundColor = Theme.BG_CARD;
    private Color borderColor = Theme.BORDER_COLOR;
    private boolean showBorder = true;

    public CardPanel() {
        setOpaque(false);
    }

    public CardPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public CardPanel(LayoutManager layout, int radius) {
        super(layout);
        this.cornerRadius = radius;
        setOpaque(false);
    }

    public void setCardBackground(Color bg) {
        this.backgroundColor = bg;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.borderColor = border;
        repaint();
    }

    public void setShowBorder(boolean show) {
        this.showBorder = show;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // Border
        if (showBorder && borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
