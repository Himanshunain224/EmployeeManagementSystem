package com.employee.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() {
        try {
            // Try Railway environment variables first
            String host = System.getenv("MYSQLHOST");
            String port = System.getenv("MYSQLPORT");
            String database = System.getenv("MYSQLDATABASE");
            String user = System.getenv("MYSQLUSER");
            String password = System.getenv("MYSQLPASSWORD");

            String url;

            if (host != null) {
                // Running on Railway
                url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
            } else {
                // Running locally
                url = "jdbc:mysql://localhost:3306/employee_db?useSSL=false";
                user = "root";
                password = "root123";
            }

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
            return conn;

        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}