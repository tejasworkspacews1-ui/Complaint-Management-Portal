package com.smartcomplaint.ui.components;

import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class VisualStatusTracker extends JPanel {
    private static final String[] STAGES = {
            "Submitted", "Under Review", "Assigned", "In Progress", "Resolved", "Closed"
    };

    private int currentStageIndex = 0; // 0 to 5
    private boolean isRejected = false;

    public VisualStatusTracker() {
        setPreferredSize(new Dimension(750, 95));
        setMinimumSize(new Dimension(500, 95));
        setOpaque(false);
    }

    public void setStatus(ComplaintStatus status) {
        if (status == ComplaintStatus.REJECTED) {
            this.isRejected = true;
            this.currentStageIndex = -1;
        } else {
            this.isRejected = false;
            this.currentStageIndex = status != null ? status.getStepIndex() : 0;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (isRejected) {
            // Render rejected state banner
            g2.setColor(new Color(254, 226, 226));
            g2.fillRoundRect(10, 15, width - 20, 65, 12, 12);
            g2.setColor(Theme.DANGER);
            g2.drawRoundRect(10, 15, width - 20, 65, 12, 12);
            g2.setFont(Theme.FONT_TITLE);
            FontMetrics fm = g2.getFontMetrics();
            String msg = "✖ COMPLAINT REJECTED / CLOSED WITHOUT RESOLUTION";
            g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, 53);
            g2.dispose();
            return;
        }

        int nodeCount = STAGES.length;
        int paddingSide = 45;
        int usableWidth = width - (paddingSide * 2);
        int stepSpacing = usableWidth / (nodeCount - 1);
        int centerY = 32;
        int nodeRadius = 15;

        // Draw connecting track line
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Background line
        g2.setColor(new Color(226, 232, 240));
        g2.drawLine(paddingSide, centerY, width - paddingSide, centerY);

        // Progress colored line
        if (currentStageIndex > 0) {
            int progressEndX = paddingSide + Math.min(currentStageIndex, nodeCount - 1) * stepSpacing;
            g2.setColor(Theme.PRIMARY);
            g2.drawLine(paddingSide, centerY, progressEndX, centerY);
        }

        // Draw nodes and labels
        for (int i = 0; i < nodeCount; i++) {
            int cx = paddingSide + (i * stepSpacing);
            boolean isCompleted = i < currentStageIndex;
            boolean isCurrent = i == currentStageIndex;
            boolean isFuture = i > currentStageIndex;

            if (isCompleted) {
                // Completed node - Solid Primary with Checkmark
                g2.setColor(Theme.SUCCESS);
                g2.fillOval(cx - nodeRadius, centerY - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                // Draw checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 5, centerY, cx - 1, centerY + 4);
                g2.drawLine(cx - 1, centerY + 4, cx + 5, centerY - 4);
            } else if (isCurrent) {
                // Current active node - Pulsing ring + Solid Primary Center
                g2.setColor(Theme.PRIMARY_LIGHT);
                g2.fillOval(cx - nodeRadius - 4, centerY - nodeRadius - 4, (nodeRadius + 4) * 2, (nodeRadius + 4) * 2);

                g2.setColor(Theme.PRIMARY);
                g2.fillOval(cx - nodeRadius, centerY - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String numStr = String.valueOf(i + 1);
                g2.drawString(numStr, cx - (fm.stringWidth(numStr) / 2), centerY + 4);
            } else {
                // Future node - Light gray border
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - nodeRadius, centerY - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(cx - nodeRadius, centerY - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String numStr = String.valueOf(i + 1);
                g2.drawString(numStr, cx - (fm.stringWidth(numStr) / 2), centerY + 4);
            }

            // Draw Stage Label
            g2.setFont(isCurrent ? Theme.FONT_HEADER : Theme.FONT_SMALL);
            g2.setColor(isCurrent ? Theme.PRIMARY_DARK : (isCompleted ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED));
            FontMetrics fm = g2.getFontMetrics();
            String label = STAGES[i];
            int lx = cx - (fm.stringWidth(label) / 2);
            int ly = centerY + nodeRadius + 20;
            g2.drawString(label, lx, ly);
        }

        g2.dispose();
    }
}
