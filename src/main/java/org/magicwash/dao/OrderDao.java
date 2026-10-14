package org.magicwash.dao;

import org.magicwash.model.LaundryOrder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public boolean createOrder(int customerId, String serviceType, double weightKg, int userId) {
        // Price rates per kg: Wash-Dry-Fold = 50.0, Dry Cleaning = 120.0, Pressing = 40.0
        double ratePerKg = switch (serviceType) {
            case "Dry Cleaning" -> 120.0;
            case "Pressing" -> 40.0;
            default -> 50.0; // Wash-Dry-Fold
        };
        double totalAmount = weightKg * ratePerKg;

        String query = "INSERT INTO laundry_orders (customer_id, service_type, weight_kg, total_amount, order_status, processed_by_user_id) VALUES (?, ?, ?, ?, 'Pending', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            pstmt.setString(2, serviceType);
            pstmt.setDouble(3, weightKg);
            pstmt.setDouble(4, totalAmount);
            pstmt.setInt(5, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            return false;
        }
    }

    public List<LaundryOrder> getAllOrders() {
        List<LaundryOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM laundry_orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                orders.add(new LaundryOrder(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("service_type"),
                        rs.getDouble("weight_kg"),
                        rs.getDouble("total_amount"),
                        rs.getString("order_status"),
                        rs.getString("order_date"),
                        rs.getInt("processed_by_user_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        String query = "UPDATE laundry_orders SET order_status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePendingOrder(int orderId) {
        String query = "DELETE FROM laundry_orders WHERE order_id = ? AND order_status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            return false;
        }
    }
}