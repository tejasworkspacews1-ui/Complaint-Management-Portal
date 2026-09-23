package com.smartcomplaint.ui.views;

import com.smartcomplaint.model.Notification;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.AuthService;
import com.smartcomplaint.service.NotificationService;
import com.smartcomplaint.ui.Theme;
import com.smartcomplaint.ui.components.CardPanel;
import com.smartcomplaint.ui.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationDialog extends JDialog {
    private final NotificationService notificationService = new NotificationService();
    private final JPanel listContainer;
    private final Runnable onDismiss;

    public NotificationDialog(Frame parent, Runnable onDismiss) {
        super(parent, "Notifications & System Alerts", true);
        this.onDismiss = onDismiss;
        setSize(480, 520);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_MAIN);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        CardPanel card = new CardPanel(new BorderLayout(10, 10), 12);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("🔔 Notifications & Alerts");
        title.setFont(Theme.FONT_TITLE);
        top.add(title, BorderLayout.WEST);

        ModernButton markReadBtn = ModernButton.neutral("Mark all as read");
        markReadBtn.addActionListener(e -> {
            User u = AuthService.getCurrentUser();
            if (u != null) {
                notificationService.markAllAsRead(u.getId());
                loadNotifications();
                if (onDismiss != null) onDismiss.run();
            }
        });
        top.add(markReadBtn, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        card.add(scroll, BorderLayout.CENTER);

        ModernButton closeBtn = new ModernButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        btm.add(closeBtn);
        card.add(btm, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        loadNotifications();
    }

    private void loadNotifications() {
        listContainer.removeAll();
        User u = AuthService.getCurrentUser();
        if (u == null) return;

        List<Notification> list = notificationService.getUserNotifications(u.getId());
        if (list.isEmpty()) {
            JLabel empty = new JLabel("No notifications at this time.", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_BODY);
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setBorder(new EmptyBorder(40, 20, 40, 20));
            listContainer.add(empty);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a");
            for (Notification n : list) {
                JPanel item = new JPanel(new BorderLayout(8, 4));
                item.setOpaque(true);
                item.setBackground(n.isRead() ? Color.WHITE : new Color(238, 242, 255));
                item.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                        new EmptyBorder(10, 12, 10, 12)
                ));

                JLabel t = new JLabel((!n.isRead() ? "● " : "") + n.getTitle());
                t.setFont(Theme.FONT_HEADER);
                t.setForeground(n.isRead() ? Theme.TEXT_PRIMARY : Theme.PRIMARY_DARK);

                JLabel d = new JLabel(sdf.format(n.getCreatedAt()));
                d.setFont(Theme.FONT_SMALL);
                d.setForeground(Theme.TEXT_MUTED);

                JLabel msg = new JLabel("<html>" + n.getMessage() + "</html>");
                msg.setFont(Theme.FONT_BODY);
                msg.setForeground(Theme.TEXT_SECONDARY);

                JPanel header = new JPanel(new BorderLayout());
                header.setOpaque(false);
                header.add(t, BorderLayout.WEST);
                header.add(d, BorderLayout.EAST);

                item.add(header, BorderLayout.NORTH);
                item.add(msg, BorderLayout.CENTER);
                item.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));

                listContainer.add(item);
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }
}
