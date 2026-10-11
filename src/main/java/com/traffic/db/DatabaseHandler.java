package com.traffic.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DatabaseHandler {

    // --- ⚠️ CONFIGURATION ⚠️ ---
    // Make sure 'root' and '' (empty) match your MySQL settings!
    // If you use XAMPP, password is usually empty.
    // If you use MySQL Workbench, you likely set a password.
    private static final String URL = "jdbc:mysql://localhost:3306/traffic_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String INSERT_SQL = "INSERT INTO traffic_logs (event_type, timestamp, details) VALUES (?, ?, ?)";

    public void logEvent(String eventType, String details) {
        // Run in a separate thread to prevent lagging the UI
        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

                stmt.setString(1, eventType);
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(3, details);

                stmt.executeUpdate();
                System.out.println("[DB SUCCESS] Saved: " + eventType);

            } catch (SQLException e) {
                System.err.println("[DB ERROR] Connection Failed! Check your User/Password.");
                e.printStackTrace();
            }
        }).start();
    }

    // --- MAIN METHOD FOR TESTING ---
    // Run this file directly to test the connection before running the full app
    public static void main(String[] args) {
        new DatabaseHandler().logEvent("TEST_CONNECTION", "Checking if database works...");
    }
}