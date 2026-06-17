package com.employee.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() {
        try {
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            // Fallback to local settings if env vars not set
            if (url == null) url = "jdbc:mysql://localhost:3306/employee_db";
            if (user == null) user = "root";
            if (password == null) password = "root123"; // your local password

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
            return conn;
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}