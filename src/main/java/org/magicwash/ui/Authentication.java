package org.magicwash.ui;

import org.magicwash.dao.UserDao;
import org.magicwash.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;
    private final ConsoleMenu consoleMenu;

    public Authentication() {
        this.userDao = new UserDao();
        this.scanner = new Scanner(System.in);
        this.consoleMenu = new ConsoleMenu(this.scanner);
    }

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

        System.out.println("Select User Role:");
        System.out.println("[1] System Administrator");
        System.out.println("[2] Laundry Staff");
        System.out.println("[3] Inventory Staff");
        System.out.print("Enter choice: ");
        String roleChoice = scanner.nextLine();

        String role = switch (roleChoice) {
            case "1" -> "System Administrator";
            case "2" -> "Laundry Staff";
            default -> "Inventory Staff";
        };

        boolean success = userDao.registerUser(username, password, fullName, role);
        if (success) {
            System.out.println("Registration successful! Account status is PENDING admin approval.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
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

        System.out.println("\nLogin successful! Welcome, " + user.fullName() + " (" + user.userRole() + ")");
        // Route user to role-based UI menu passing shared userDao instance
        consoleMenu.runDashboard(user, userDao);
    }
}