package org.magicwash.ui;

import org.magicwash.dao.CustomerDao;
import org.magicwash.dao.InventoryDao;
import org.magicwash.dao.OrderDao;
import org.magicwash.dao.UserDao;
import org.magicwash.model.Customer;
import org.magicwash.model.InventoryItem;
import org.magicwash.model.LaundryOrder;
import org.magicwash.model.User;
import java.util.Scanner;

public class ConsoleMenu {
    private final UserDao userDao = new UserDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final OrderDao orderDao = new OrderDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== MAGIC WASH LAUNDRY SYSTEM ===");
            System.out.println("[1] Register Account");
            System.out.println("[2] Login");
            System.out.println("[3] Exit");
            System.out.print("Select choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> handleRegistration();
                case "2" -> handleLogin();
                case "3" -> {
                    System.out.println("Exiting application. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void handleRegistration() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Full Name: ");
        String fullName = scanner.nextLine();

        System.out.println("Select Role: [1] System Administrator [2] Laundry Staff [3] Inventory Staff");
        String roleChoice = scanner.nextLine();
        String role = switch (roleChoice) {
            case "1" -> "System Administrator";
            case "2" -> "Laundry Staff";
            default -> "Inventory Staff";
        };

        boolean success = userDao.registerUser(username, password, fullName, role);
        if (success) {
            System.out.println("Registration successful! Status is currently PENDING admin approval.");
        } else {
            System.out.println("Registration failed. Username might already exist.");
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);
        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        if ("PENDING".equalsIgnoreCase(user.status())) {
            System.out.println("Login denied. Your account is still pending administrator approval.");
            return;
        }

        System.out.println("\nWelcome back, " + user.fullName() + " (" + user.userRole() + ")!");
        runDashboard(user);
    }

    private void runDashboard(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- Dashboard [" + user.userRole() + "] ---");

            if ("System Administrator".equalsIgnoreCase(user.userRole())) {
                System.out.println("[1] Manage & Approve Pending Users");
                System.out.println("[2] View Daily Sales Summary Report");
                System.out.println("[3] Logout");
            } else if ("Laundry Staff".equalsIgnoreCase(user.userRole())) {
                System.out.println("[1] Register New Customer");
                System.out.println("[2] Create Laundry Order");
                System.out.println("[3] View & Update Order Status");
                System.out.println("[4] View Customer Transaction History");
                System.out.println("[5] Cancel/Delete Pending Order");
                System.out.println("[6] Logout");
            } else {
                // Inventory Staff Dashboard Options
                System.out.println("[1] View Inventory Stock Levels");
                System.out.println("[2] Add New Inventory Item");
                System.out.println("[3] Restock / Update Item Quantity");
                System.out.println("[4] Delete Inventory Item");
                System.out.println("[5] Logout");
            }

            System.out.print("Choice: ");
            String choice = scanner.nextLine();

            if ("System Administrator".equalsIgnoreCase(user.userRole())) {
                if (choice.equals("1")) handleUserApprovals();
                else if (choice.equals("2")) showDailySalesReport();
                else if (choice.equals("3")) {
                    System.out.println("Logging out...");
                    loggedIn = false;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            } else if ("Laundry Staff".equalsIgnoreCase(user.userRole())) {
                if (choice.equals("1")) handleAddCustomer();
                else if (choice.equals("2")) handleCreateOrder(user.userId());
                else if (choice.equals("3")) handleViewAndUpdateOrders();
                else if (choice.equals("4")) handleViewCustomerTransactions();
                else if (choice.equals("5")) handleDeleteOrder();
                else if (choice.equals("6")) {
                    System.out.println("Logging out...");
                    loggedIn = false;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            } else {
                // Inventory Staff Actions
                if (choice.equals("1")) handleViewInventory();
                else if (choice.equals("2")) handleAddInventoryItem();
                else if (choice.equals("3")) handleUpdateStock();
                else if (choice.equals("4")) handleDeleteInventoryItem();
                else if (choice.equals("5")) {
                    System.out.println("Logging out...");
                    loggedIn = false;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }

    private void handleUserApprovals() {
        var pendingUsers = userDao.getPendingUsers();
        if (pendingUsers.isEmpty()) {
            System.out.println("\nNo pending user accounts found.");
            return;
        }

        System.out.println("\n--- PENDING USER ACCOUNTS ---");
        for (User pUser : pendingUsers) {
            System.out.println("ID: " + pUser.userId() + " | Username: " + pUser.username() + " | Name: " + pUser.fullName() + " | Role: " + pUser.userRole());
        }

        System.out.print("Enter User ID to APPROVE (or type 0 to go back): ");
        try {
            int idToApprove = Integer.parseInt(scanner.nextLine());
            if (idToApprove == 0) return;

            boolean success = userDao.approveUser(idToApprove);
            if (success) System.out.println("User account successfully APPROVED!");
            else System.out.println("Failed to approve user.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void handleAddCustomer() {
        System.out.print("Enter First Name: ");
        String fname = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lname = scanner.nextLine();
        System.out.print("Enter Contact Number: ");
        String contact = scanner.nextLine();

        if (customerDao.addCustomer(fname, lname, contact)) {
            System.out.println("Customer registered successfully!");
        } else {
            System.out.println("Failed to register customer.");
        }
    }

    private void handleCreateOrder(int userId) {
        var customers = customerDao.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers found! Please register a customer first.");
            return;
        }

        System.out.println("\n--- Registered Customers ---");
        for (Customer c : customers) {
            System.out.println("ID: " + c.customerId() + " | Name: " + c.firstName() + " " + c.lastName());
        }

        System.out.print("Enter Customer ID for this order: ");
        try {
            int customerId = Integer.parseInt(scanner.nextLine());
            System.out.println("Select Service Type: [1] Wash-Dry-Fold (50/kg) [2] Dry Cleaning (120/kg) [3] Pressing (40/kg)");
            String sChoice = scanner.nextLine();
            String serviceType = switch (sChoice) {
                case "2" -> "Dry Cleaning";
                case "3" -> "Pressing";
                default -> "Wash-Dry-Fold";
            };

            System.out.print("Enter weight in kg: ");
            double weight = Double.parseDouble(scanner.nextLine());

            if (orderDao.createOrder(customerId, serviceType, weight, userId)) {
                System.out.println("Laundry order created successfully!");
            } else {
                System.out.println("Failed to create order.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input.");
        }
    }

    private void handleViewAndUpdateOrders() {
        var orders = orderDao.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        System.out.println("\n--- ALL LAUNDRY ORDERS ---");
        for (LaundryOrder o : orders) {
            System.out.println("Order ID: " + o.orderId() + " | Customer ID: " + o.customerId() + " | Service: " + o.serviceType() + " | Weight: " + o.weightKg() + "kg | Total: PHP " + o.totalAmount() + " | Status: " + o.orderStatus());
        }

        System.out.print("Enter Order ID to update status (or 0 to go back): ");
        try {
            int orderId = Integer.parseInt(scanner.nextLine());
            if (orderId == 0) return;

            System.out.println("Select New Status: [1] Pending [2] In-Washing [3] Ready for Pickup [4] Completed");
            String statusChoice = scanner.nextLine();
            String newStatus = switch (statusChoice) {
                case "2" -> "In-Washing";
                case "3" -> "Ready for Pickup";
                case "4" -> "Completed";
                default -> "Pending";
            };

            if (orderDao.updateOrderStatus(orderId, newStatus)) {
                System.out.println("Order status updated successfully!");
            } else {
                System.out.println("Failed to update status.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void handleViewCustomerTransactions() {
        var customers = customerDao.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        System.out.println("\n--- REGISTERED CUSTOMERS ---");
        for (Customer c : customers) {
            System.out.println("ID: " + c.customerId() + " | Name: " + c.firstName() + " " + c.lastName() + " | Contact: " + c.contactNumber());
        }

        System.out.print("Enter Customer ID to view their transactions: ");
        try {
            int customerId = Integer.parseInt(scanner.nextLine());
            var customerOrders = orderDao.getOrdersByCustomer(customerId);

            if (customerOrders.isEmpty()) {
                System.out.println("\nNo transaction history found for Customer ID: " + customerId);
                return;
            }

            System.out.println("\n--- TRANSACTION HISTORY FOR CUSTOMER ID: " + customerId + " ---");
            for (LaundryOrder o : customerOrders) {
                System.out.println("Order ID: " + o.orderId() + " | Service: " + o.serviceType() +
                        " | Weight: " + o.weightKg() + "kg | Total: PHP " + o.totalAmount() +
                        " | Status: " + o.orderStatus() + " | Date: " + o.orderDate());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid customer ID number.");
        }
    }

    private void handleDeleteOrder() {
        System.out.print("Enter Pending Order ID to cancel/delete: ");
        try {
            int orderId = Integer.parseInt(scanner.nextLine());
            if (orderDao.deletePendingOrder(orderId)) {
                System.out.println("Pending order canceled and deleted successfully.");
            } else {
                System.out.println("Could not delete. Make sure the order exists and is still 'Pending'.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void showDailySalesReport() {
        var orders = orderDao.getAllOrders();
        double totalRevenue = 0;
        double totalWeight = 0;
        int washDryFoldCount = 0;
        int dryCleaningCount = 0;

        for (LaundryOrder o : orders) {
            totalRevenue += o.totalAmount();
            totalWeight += o.weightKg();
            if ("Dry Cleaning".equalsIgnoreCase(o.serviceType())) {
                dryCleaningCount++;
            } else {
                washDryFoldCount++;
            }
        }

        System.out.println("\n========================================");
        System.out.println("     DAILY SALES & SERVICE SUMMARY REPORT");
        System.out.println("========================================");
        System.out.println("Total Orders Processed : " + orders.size());
        System.out.println("Wash-Dry-Fold Orders   : " + washDryFoldCount);
        System.out.println("Dry Cleaning Orders    : " + dryCleaningCount);
        System.out.println("Total Weight Processed : " + totalWeight + " kg");
        System.out.println("Total Revenue Collected: PHP " + totalRevenue);
        System.out.println("========================================");
    }

    private void handleViewInventory() {
        var items = inventoryDao.getAllInventory();
        if (items.isEmpty()) {
            System.out.println("\nNo inventory items found in stock.");
            return;
        }

        System.out.println("\n--- CURRENT INVENTORY & STOCK LEVELS ---");
        for (var item : items) {
            String alert = item.quantityInStock() <= item.reorderLevel() ? " [LOW STOCK ALERT! Needs restock!]" : "";
            System.out.println("ID: " + item.itemId() + " | Item: " + item.itemName() + " | Category: " + item.category() +
                    " | Remaining: " + item.quantityInStock() + " " + item.unitOfMeasure() + alert);
        }
    }

    private void handleAddInventoryItem() {
        try {
            System.out.print("Enter Item Name (e.g., Liquid Detergent): ");
            String name = scanner.nextLine();
            System.out.print("Enter Category (e.g., Cleaning Supplies): ");
            String category = scanner.nextLine();
            System.out.print("Enter Initial Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Unit of Measure (e.g., Bottles, kg, Liters): ");
            String unit = scanner.nextLine();
            System.out.print("Enter Reorder Alert Level: ");
            int reorder = Integer.parseInt(scanner.nextLine());

            if (inventoryDao.addInventoryItem(name, category, qty, unit, reorder)) {
                System.out.println("Inventory item added successfully!");
            } else {
                System.out.println("Failed to add inventory item.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input! Please enter a valid integer for quantity and reorder level.");
        }
    }

    private void handleUpdateStock() {
        handleViewInventory();
        System.out.print("Enter Item ID to update stock quantity: ");
        try {
            int itemId = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter new remaining quantity: ");
            int newQty = Integer.parseInt(scanner.nextLine());

            if (inventoryDao.updateStock(itemId, newQty)) {
                System.out.println("Stock quantity updated successfully!");
            } else {
                System.out.println("Failed to update stock.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input! Please enter valid numbers.");
        }
    }

    private void handleDeleteInventoryItem() {
        handleViewInventory();
        System.out.print("Enter Item ID to delete: ");
        try {
            int itemId = Integer.parseInt(scanner.nextLine());
            if (inventoryDao.deleteInventoryItem(itemId)) {
                System.out.println("Inventory item deleted successfully!");
            } else {
                System.out.println("Failed to delete. Make sure the Item ID exists.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input!");
        }
    }
}