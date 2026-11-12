package org.magicwash.dao;

import org.magicwash.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public boolean logAction(int userId, String actionType, String description) {
        String query = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType.toUpperCase());
            pstmt.setString(3, description);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String query = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs ORDER BY timestamp DESC, id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all logs: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String query = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs for user ID " + userId + ": " + e.getMessage());
        }
        return logs;
    }

    public boolean clearHistoricalLogs() {
        String query = "DELETE FROM activity_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            return pstmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
            return false;
        }
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}