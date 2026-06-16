package com.employee.dao;

import com.employee.model.Department;
import com.employee.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    // ADD department
    public boolean addDepartment(Department dept) {
        String sql = "INSERT INTO departments (dept_name, location) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getLocation());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding department: " + e.getMessage());
            return false;
        }
    }

    // GET all departments
    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM departments";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Department dept = new Department();
                dept.setDeptId(rs.getInt("dept_id"));
                dept.setDeptName(rs.getString("dept_name"));
                dept.setLocation(rs.getString("location"));
                list.add(dept);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching departments: " + e.getMessage());
        }
        return list;
    }

    // DELETE department
    public boolean deleteDepartment(int id) {
        String sql = "DELETE FROM departments WHERE dept_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting department: " + e.getMessage());
            return false;
        }
    }
}