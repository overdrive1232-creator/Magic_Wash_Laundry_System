package org.magicwash;

import org.magicwash.dao.DatabaseInitializer;
import org.magicwash.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Database and Tables via DatabaseInitializer
        DatabaseInitializer.initializeDatabase();

        // 2. Instantiate and delegate execution flow to Authentication
        Authentication auth = new Authentication();
        auth.start();
    }
}