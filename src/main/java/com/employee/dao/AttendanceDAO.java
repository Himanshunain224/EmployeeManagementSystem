package com.employee.dao;

import com.employee.util.DBConnection;

import java.sql.*;

public class AttendanceDAO {

    public boolean markAttendance(int empId, String status) {
        String checkSql = "SELECT * FROM attendance WHERE emp_id = ? AND att_date = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, empId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

        String sql = "INSERT INTO attendance (emp_id, att_date, status) VALUES (?, CURDATE(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, status);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public void viewAttendance(int empId) {
        String sql = "SELECT att_date, status FROM attendance WHERE emp_id = ? ORDER BY att_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getDate("att_date") + " - " + rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String getAttendancePercentage(int empId) {
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status='Present' THEN 1 ELSE 0 END) as present FROM attendance WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                if (total == 0) return "No attendance records found.";
                double percentage = (present * 100.0) / total;
                return String.format("Total Days: %d\nPresent: %d\nAbsent: %d\nAttendance: %.2f%%",
                        total, present, total - present, percentage);
            }
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
        return "No records found.";
    }
}