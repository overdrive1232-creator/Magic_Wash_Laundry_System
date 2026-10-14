package org.magicwash.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:magic_wash.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Users Table
            stmt.execute("""
    CREATE TABLE IF NOT EXISTS users (
        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        full_name TEXT NOT NULL,
        user_role TEXT NOT NULL,
        status TEXT DEFAULT 'PENDING'
    );
""");

// Ensure default admin exists AND is APPROVED
            stmt.execute("""
    INSERT OR IGNORE INTO users (username, password_hash, full_name, user_role, status)
    VALUES ('admin', 'admin', 'System Administrator', 'System Administrator', 'APPROVED');
""");

// Force update 'admin' user status to APPROVED
            stmt.execute("UPDATE users SET status = 'APPROVED' WHERE username = 'admin';");
            // 2. Customers Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    contact_number TEXT NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 3. Laundry Orders Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS laundry_orders (
                    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER,
                    service_type TEXT NOT NULL,
                    weight_kg REAL NOT NULL,
                    total_amount REAL NOT NULL,
                    order_status TEXT DEFAULT 'Pending',
                    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    processed_by_user_id INTEGER,
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                    FOREIGN KEY (processed_by_user_id) REFERENCES users(user_id)
                );
            """);

            // 4. Inventory Items Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory_items (
                    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    quantity_in_stock INTEGER NOT NULL,
                    unit_of_measure TEXT NOT NULL,
                    reorder_level INTEGER NOT NULL
                );
            """);

            // 5. System Audit Logs Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS system_audit_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action_performed TEXT NOT NULL,
                    target_table TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                );
            """);

            System.out.println("[Database] SQLite database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to initialize database: " + e.getMessage());
        }
    }
}