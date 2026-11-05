package org.magicwash.ui;

import org.magicwash.dao.UserDao;
import org.magicwash.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(Scanner scanner, User currentAdmin) {
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void displayAdminMenu(UserDao userDao) {
        boolean managing = true;
        while (managing) {
            System.out.println("\n=== SYSTEM ADMINISTRATOR MENU ===");
            System.out.println("[1] View All System Users");
            System.out.println("[2] View Pending Registrations");
            System.out.println("[3] Approve User Account");
            System.out.println("[4] Update User Role & Status");
            System.out.println("[5] Back to Main Menu / Logout");
            System.out.print("Select choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> viewAllUsers(userDao);
                case "2" -> viewPendingRegistrations(userDao);
                case "3" -> approveUserAccount(userDao);
                case "4" -> updateUserRoleAndStatus(userDao);
                case "5" -> {
                    System.out.println("Returning to main menu...");
                    managing = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewAllUsers(UserDao userDao) {
        List<User> users = userDao.getAllUsers();
        printUserTable(users, "ALL REGISTERED SYSTEM USERS");
    }

    private void viewPendingRegistrations(UserDao userDao) {
        List<User> pendingUsers = userDao.getPendingUsers();
        printUserTable(pendingUsers, "PENDING USER REGISTRATIONS");
    }

    private void approveUserAccount(UserDao userDao) {
        List<User> pendingUsers = userDao.getPendingUsers();
        if (pendingUsers.isEmpty()) {
            System.out.println("\nNo pending user accounts awaiting approval.");
            return;
        }

        printUserTable(pendingUsers, "PENDING ACCOUNTS AWAITING APPROVAL");

        int targetId = readIntInput("Enter Target User ID to APPROVE (or enter 0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Approval action canceled.");
            return;
        }

        boolean existsInPending = pendingUsers.stream().anyMatch(u -> u.userId() == targetId);
        if (!existsInPending) {
            System.out.println("Error: Invalid User ID or user is not currently in the pending list.");
            return;
        }

        boolean success = userDao.approveUser(targetId);
        if (success) {
            System.out.println("Success! User ID " + targetId + " status has been updated from PENDING to APPROVED.");
        } else {
            System.out.println("Failed to approve account in database.");
        }
    }

    private void updateUserRoleAndStatus(UserDao userDao) {
        List<User> users = userDao.getAllUsers();
        printUserTable(users, "ALL REGISTERED USERS - UPDATE MANAGEMENT");

        int targetId = readIntInput("Enter Target User ID to UPDATE (or enter 0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Update action canceled.");
            return;
        }

        // Self-Edit Restriction Guardrail
        if (targetId == currentAdmin.userId()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        boolean userExists = users.stream().anyMatch(u -> u.userId() == targetId);
        if (!userExists) {
            System.out.println("Error: User ID does not exist in the system.");
            return;
        }

        String newRole = readValidRole();
        String newStatus = readValidStatus();

        boolean success = userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
        if (success) {
            System.out.println("Success! User ID " + targetId + " has been updated to Role: " + newRole.toUpperCase() + " and Status: " + newStatus.toUpperCase());
        } else {
            System.out.println("Failed to update user in the database.");
        }
    }

    // --- Input Validation Wrappers ---

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid numeric number.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new Role ([1] System Administrator, [2] Laundry Staff, [3] Inventory Staff or type role name): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("1") || input.equals("SYSTEM ADMINISTRATOR")) return "System Administrator";
            if (input.equals("2") || input.equals("LAUNDRY STAFF")) return "Laundry Staff";
            if (input.equals("3") || input.equals("INVENTORY STAFF")) return "Inventory Staff";

            // Allow general custom roles if needed, or enforce specific ones
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Invalid role. Please provide a recognized role name.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status (APPROVED or PENDING): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("APPROVED") || input.equals("PENDING")) {
                return input;
            }
            System.out.println("Invalid status! Status must be either APPROVED or PENDING.");
        }
    }

    private void printUserTable(List<User> users, String title) {
        System.out.println("\n==========================================================================");
        System.out.println("                         " + title);
        System.out.println("==========================================================================");
        if (users.isEmpty()) {
            System.out.println(" [ No records found ]");
            System.out.println("==========================================================================");
            return;
        }

        String formatString = "%-8s | %-18s | %-20s | %-22s | %-10s%n";
        System.out.printf(formatString, "ID", "USERNAME", "FULL NAME", "ROLE", "STATUS");
        System.out.println("--------------------------------------------------------------------------");

        for (User u : users) {
            System.out.printf(formatString,
                    u.userId(),
                    u.username(),
                    u.fullName(),
                    u.userRole(),
                    u.status()
            );
        }
        System.out.println("==========================================================================");
    }
}