package com.smartcomplaint.ui.components;

import com.smartcomplaint.model.ComplaintPriority;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.SLAStatus;
import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class StatusBadge extends JPanel {
    private String text;
    private Color fgColor;
    private Color bgColor;

    public StatusBadge(String text, Color fgColor, Color bgColor) {
        this.text = text;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        setOpaque(false);
        setFont(Theme.FONT_SMALL_BOLD);
    }

    public static StatusBadge forStatus(ComplaintStatus status) {
        if (status == null) status = ComplaintStatus.SUBMITTED;
        Color bg;
        Color fg = Color.WHITE;
        switch (status) {
            case SUBMITTED:
                bg = new Color(59, 130, 246); // Blue
                break;
            case UNDER_REVIEW:
                bg = new Color(139, 92, 246); // Purple
                break;
            case ASSIGNED:
                bg = new Color(14, 165, 233); // Sky
                break;
            case IN_PROGRESS:
                bg = new Color(245, 158, 11); // Amber
                break;
            case RESOLVED:
                bg = new Color(16, 185, 129); // Green
                break;
            case CLOSED:
                bg = new Color(100, 116, 139); // Slate
                break;
            case REJECTED:
            default:
                bg = new Color(239, 68, 68); // Red
                break;
        }
        return new StatusBadge(status.getDisplayName(), fg, bg);
    }

    public static StatusBadge forSLA(SLAStatus sla) {
        if (sla == null) sla = SLAStatus.WITHIN_SLA;
        return new StatusBadge(sla.getDisplayName(), sla.getFgColor(), sla.getBgColor());
    }

    public static StatusBadge forPriority(ComplaintPriority priority) {
        if (priority == null) priority = ComplaintPriority.MEDIUM;
        Color bg;
        switch (priority) {
            case EMERGENCY:
                bg = new Color(254, 226, 226);
                return new StatusBadge("EMERGENCY", priority.getColor(), bg);
            case HIGH:
                bg = new Color(254, 237, 213);
                return new StatusBadge("HIGH", priority.getColor(), bg);
            case MEDIUM:
                bg = new Color(254, 249, 195);
                return new StatusBadge("MEDIUM", priority.getColor(), bg);
            case LOW:
            default:
                bg = new Color(224, 242, 254);
                return new StatusBadge("LOW", priority.getColor(), bg);
        }
    }

    public void updateStatus(ComplaintStatus status) {
        StatusBadge b = forStatus(status);
        this.text = b.text;
        this.fgColor = b.fgColor;
        this.bgColor = b.bgColor;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(text) + 20;
        int h = fm.getHeight() + 8;
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(bgColor);
        g2.fillRoundRect(1, 1, w - 2, h - 2, h, h);

        g2.setColor(fgColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = ((h - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
