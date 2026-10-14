package org.magicwash.dao;

import org.magicwash.model.User;
import java.sql.*;

public class UserDao {

    public boolean registerUser(String username, String passwordHash, String fullName, String role) {
        String query = "INSERT INTO users (username, password_hash, full_name, user_role, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash); // Note: Hash your password in production (e.g., BCrypt)
            pstmt.setString(3, fullName);
            pstmt.setString(4, role);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration Error: " + e.getMessage());
            return false;
        }
    }

    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("user_role"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Login Error: " + e.getMessage());
        }
        return null;
    }
    // 1. Get all users with PENDING status
    public java.util.List<org.magicwash.model.User> getPendingUsers() {
        java.util.List<org.magicwash.model.User> pendingList = new java.util.ArrayList<>();
        String query = "SELECT * FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                pendingList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("user_role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending users: " + e.getMessage());
        }
        return pendingList;
    }

    // 2. Approve a user by ID
    public boolean approveUser(int userId) {
        String query = "UPDATE users SET status = 'APPROVED' WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }
}