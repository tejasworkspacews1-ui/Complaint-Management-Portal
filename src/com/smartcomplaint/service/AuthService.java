package com.smartcomplaint.service;

import com.smartcomplaint.dao.UserDAO;
import com.smartcomplaint.dao.SpaceDAO;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;
import com.smartcomplaint.util.PasswordHasher;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private static User currentUser = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static String getTenantWhereClause(String tableAlias) {
        if (currentUser == null || currentUser.getRole() == Role.ADMIN) {
            return " (1=1) ";
        }
        String t = currentUser.getTenantId() != null ? currentUser.getTenantId() : "DEFAULT";
        return " (" + tableAlias + "tenant_id = '" + t.replace("'", "''") + "') ";
    }

    public User login(String usernameOrEmail, String plainPassword, Role expectedRole) {
        if (usernameOrEmail == null || plainPassword == null || usernameOrEmail.trim().isEmpty() || plainPassword.isEmpty()) {
            return null;
        }

        User user = usernameOrEmail.contains("@") ? userDAO.findByEmail(usernameOrEmail.trim()) : userDAO.findByUsername(usernameOrEmail.trim());
        if (user == null) {
            return null;
        }

        if (expectedRole != null && user.getRole() != expectedRole) {
            return null;
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }

        boolean valid = PasswordHasher.verifyPassword(plainPassword, user.getSalt(), user.getPasswordHash());
        if (valid) {
            currentUser = user;
            return user;
        }
        return null;
    }

    public boolean registerCitizen(String tenantId, String username, String password, String fullName, String email, String phone) {
        if (username == null || username.trim().isEmpty() || password == null || password.length() < 4) {
            return false;
        }
        if (userDAO.findByUsername(username) != null || userDAO.findByEmail(email) != null) {
            return false;
        }
        
        SpaceDAO spaceDAO = new SpaceDAO();
        if (tenantId != null && !tenantId.trim().isEmpty() && !spaceDAO.spaceExists(tenantId.trim())) {
            return false; // Invalid join code
        }

        User u = new User();
        u.setTenantId(tenantId != null && !tenantId.trim().isEmpty() ? tenantId.trim() : "DEFAULT");
        u.setUsername(username.trim());
        u.setFullName(fullName.trim());
        u.setEmail(email.trim());
        u.setPhone(phone.trim());
        u.setRole(Role.CITIZEN);
        u.setStatus("ACTIVE");

        return userDAO.createUser(u, password);
    }
    
    public String createSpaceAndAdmin(String spaceName, String category, String username, String password, String fullName, String email, String phone) {
        if (username == null || username.trim().isEmpty() || password == null || password.length() < 4) {
            return null;
        }
        if (userDAO.findByUsername(username) != null || userDAO.findByEmail(email) != null) {
            return null;
        }
        
        String spaceId = "SPC-" + java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        
        SpaceDAO spaceDAO = new SpaceDAO();
        spaceDAO.createSpace(spaceId, spaceName, category, 0);
        
        User u = new User();
        u.setTenantId(spaceId);
        u.setUsername(username.trim());
        u.setFullName(fullName.trim());
        u.setEmail(email.trim());
        u.setPhone(phone.trim());
        u.setRole(Role.MODERATOR);
        u.setStatus("ACTIVE");
        
        if (userDAO.createUser(u, password)) {
            return spaceId;
        }
        return null;
    }

    public boolean resetPassword(String usernameOrEmail, String newPassword) {
        if (usernameOrEmail == null || newPassword == null || newPassword.length() < 4) return false;
        User user = usernameOrEmail.contains("@") ? userDAO.findByEmail(usernameOrEmail.trim()) : userDAO.findByUsername(usernameOrEmail.trim());
        if (user == null) return false;

        return userDAO.updatePassword(user.getId(), newPassword);
    }
}
