package com.smartcomplaint.service;

import com.smartcomplaint.dao.NotificationDAO;
import com.smartcomplaint.model.Notification;

import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public List<Notification> getUserNotifications(int userId) {
        return notificationDAO.getUserNotifications(userId);
    }

    public int getUnreadCount(int userId) {
        return notificationDAO.getUnreadCount(userId);
    }

    public void markAllAsRead(int userId) {
        notificationDAO.markAllAsRead(userId);
    }

    public void sendAlert(int userId, String complaintId, String title, String msg) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setComplaintId(complaintId);
        n.setTitle(title);
        n.setMessage(msg);
        notificationDAO.createNotification(n);
    }
}
