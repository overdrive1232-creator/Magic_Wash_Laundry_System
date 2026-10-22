package org.magicwash.ui;

import org.magicwash.dao.UserDao;
import org.magicwash.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner;

    public AdminMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void displayAdminMenu(UserDao userDao) {
        boolean managing = true;
        while (managing) {
            System.out.println("\n=== SYSTEM ADMINISTRATOR MENU ===");
            System.out.println("[1] View All System Users");
            System.out.println("[2] View Pending Registrations");
            System.out.println("[3] Approve User Account");
            System.out.println("[4] Back to Main Menu");
            System.out.print("Select choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> viewAllUsers(userDao);
                case "2" -> viewPendingRegistrations(userDao);
                case "3" -> approveUserAccount(userDao);
                case "4" -> {
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

        System.out.print("Enter Target User ID to APPROVE (or enter 0 to cancel): ");
        try {
            int targetId = Integer.parseInt(scanner.nextLine());
            if (targetId == 0) {
                System.out.println("Approval action canceled.");
                return;
            }

            // Verify if ID exists in the pending list
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
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format input. Action canceled.");
        }
    }

    /**
     * Structured table rendering helper using standard string formatters
     * (%-5s, %-15s, %-12s, %-10s)
     */
    private void printUserTable(List<User> users, String title) {
        System.out.println("\n==========================================================================");
        System.out.println("                         " + title);
        System.out.println("==========================================================================");
        if (users.isEmpty()) {
            System.out.println(" [ No records found ]");
            System.out.println("==========================================================================");
            return;
        }

        // Header Row formatting
        String formatString = "%-8s | %-18s | %-20s | %-22s | %-10s%n";
        System.out.printf(formatString, "ID", "USERNAME", "FULL NAME", "ROLE", "STATUS");
        System.out.println("--------------------------------------------------------------------------");

        // Data Rows
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