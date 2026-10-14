package org.magicwash;

import org.magicwash.dao.DatabaseConnection;
import org.magicwash.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        ConsoleMenu menu = new ConsoleMenu();
        menu.start();
    }
}