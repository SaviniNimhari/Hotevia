package com.hotevia.service;

import com.hotevia.dao.UserDAO;
import com.hotevia.model.User;
import com.hotevia.util.PasswordUtil;

import java.sql.SQLException;

public class AuthenticationService {

    private static User currentUser;
    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static void logout() {
        currentUser = null;
    }

    public User authenticate(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null;
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException("Your account is deactivated. Please contact an administrator.");
        }

        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            currentUser = user;
            return user;
        }

        return null;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        User user = userDAO.findByUsername(currentUser.getUsername());
        if (user == null || !PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            return false;
        }
        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }
}
