package org.magicwash.dao;

import org.magicwash.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public boolean addCustomer(String firstName, String lastName, String contactNumber) {
        String query = "INSERT INTO customers (first_name, last_name, contact_number) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, contactNumber);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding customer: " + e.getMessage());
            return false;
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("contact_number"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
        }
        return customers;
    }
}