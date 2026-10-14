package org.magicwash.dao;

import org.magicwash.model.InventoryItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {

    public boolean addInventoryItem(String itemName, String category, int quantity, String unit, int reorderLevel) {
        String query = "INSERT INTO inventory_items (item_name, category, quantity_in_stock, unit_of_measure, reorder_level) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, category);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, unit);
            pstmt.setInt(5, reorderLevel);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding inventory item: " + e.getMessage());
            return false;
        }
    }

    public List<InventoryItem> getAllInventory() {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT * FROM inventory_items";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                items.add(new InventoryItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getInt("quantity_in_stock"),
                        rs.getString("unit_of_measure"),
                        rs.getInt("reorder_level")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching inventory: " + e.getMessage());
        }
        return items;
    }

    public boolean updateStock(int itemId, int newQuantity) {
        String query = "UPDATE inventory_items SET quantity_in_stock = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }
}