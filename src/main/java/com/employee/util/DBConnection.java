package com.employee.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            if (url == null) {
                // Local
                url = "jdbc:mysql://localhost:3306/employee_db?useSSL=false&allowPublicKeyRetrieval=true";
                user = "root";
                password = "root123";
            } else {
                // Railway — convert mysql:// to jdbc:mysql://
                if (url.startsWith("mysql://")) {
                    url = "jdbc:mysql://" + url.substring(8);
                }
                if (!url.contains("?")) {
                    url = url + "?useSSL=false&allowPublicKeyRetrieval=true";
                }
                // Extract user and password from URL if not set separately
                if (user == null || password == null) {
                    // Parse from URL: mysql://user:password@host:port/db
                    String withoutScheme = url.replace("jdbc:mysql://", "");
                    if (withoutScheme.contains("@")) {
                        String credentials = withoutScheme.substring(0, withoutScheme.indexOf("@"));
                        user = credentials.split(":")[0];
                        password = credentials.split(":")[1];
                        String hostPart = withoutScheme.substring(withoutScheme.indexOf("@") + 1);
                        url = "jdbc:mysql://" + hostPart;
                    }
                }
            }

            System.out.println("Connecting to DB...");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}