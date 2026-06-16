package com.employee.main;

import com.employee.dao.AttendanceDAO;
import com.employee.dao.DepartmentDAO;
import com.employee.dao.EmployeeDAO;
import com.employee.model.Department;
import com.employee.model.Employee;

import java.util.List;
import java.util.Scanner;

public class MainMenu {

    static Scanner sc = new Scanner(System.in);
    static EmployeeDAO empDAO = new EmployeeDAO();
    static DepartmentDAO deptDAO = new DepartmentDAO();
    static AttendanceDAO attDAO = new AttendanceDAO();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=============================");
            System.out.println("  EMPLOYEE MANAGEMENT SYSTEM ");
            System.out.println("=============================");
            System.out.println("1. Add Employee");
            System.out.println("2. View All Employees");
            System.out.println("3. Search Employee by Name");
            System.out.println("4. Update Employee");
            System.out.println("5. Delete Employee");
            System.out.println("6. Add Department");
            System.out.println("7. View All Departments");
            System.out.println("8. Mark Attendance");
            System.out.println("9. View Attendance");
            System.out.println("10. Attendance Percentage");
            System.out.println("11. Export Employees to CSV");
            System.out.println("12. Exit");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> addEmployee();
                case 2 -> viewAllEmployees();
                case 3 -> searchEmployee();
                case 4 -> updateEmployee();
                case 5 -> deleteEmployee();
                case 6 -> addDepartment();
                case 7 -> viewAllDepartments();
                case 8 -> markAttendance();
                case 9 -> viewAttendance();
                case 10 -> attendancePercentage();
                case 11 -> exportToCSV();
                case 12 -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option! Try again.");
            }
        }
    }

    static void addEmployee() {
        System.out.println("\n--- Add New Employee ---");
        System.out.print("First Name: ");
        String firstName = sc.nextLine();
        System.out.print("Last Name: ");
        String lastName = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Phone: ");
        String phone = sc.nextLine();
        System.out.print("Salary: ");
        double salary = sc.nextDouble();
        sc.nextLine();
        System.out.print("Designation: ");
        String designation = sc.nextLine();
        System.out.print("Department ID: ");
        int deptId = sc.nextInt();
        sc.nextLine();

        Employee emp = new Employee(firstName, lastName, email, phone, salary, designation, deptId);
        if (empDAO.addEmployee(emp)) {
            System.out.println("Employee added successfully!");
        } else {
            System.out.println("Failed to add employee.");
        }
    }

    static void viewAllEmployees() {
        System.out.println("\n--- All Employees ---");
        List<Employee> list = empDAO.getAllEmployees();
        if (list.isEmpty()) {
            System.out.println("No employees found.");
        } else {
            list.forEach(System.out::println);
        }
    }

    static void searchEmployee() {
        System.out.println("\n--- Search Employee ---");
        System.out.print("Enter name to search: ");
        String name = sc.nextLine();
        List<Employee> list = empDAO.searchByName(name);
        if (list.isEmpty()) {
            System.out.println("No employees found.");
        } else {
            list.forEach(System.out::println);
        }
    }

    static void updateEmployee() {
        System.out.println("\n--- Update Employee ---");
        System.out.print("Enter Employee ID to update: ");
        int id = sc.nextInt();
        sc.nextLine();
        Employee emp = empDAO.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }
        System.out.print("New First Name (" + emp.getFirstName() + "): ");
        String firstName = sc.nextLine();
        System.out.print("New Last Name (" + emp.getLastName() + "): ");
        String lastName = sc.nextLine();
        System.out.print("New Email (" + emp.getEmail() + "): ");
        String email = sc.nextLine();
        System.out.print("New Phone (" + emp.getPhone() + "): ");
        String phone = sc.nextLine();
        System.out.print("New Salary (" + emp.getSalary() + "): ");
        double salary = sc.nextDouble();
        sc.nextLine();
        System.out.print("New Designation (" + emp.getDesignation() + "): ");
        String designation = sc.nextLine();
        System.out.print("New Department ID (" + emp.getDeptId() + "): ");
        int deptId = sc.nextInt();
        sc.nextLine();

        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setEmail(email);
        emp.setPhone(phone);
        emp.setSalary(salary);
        emp.setDesignation(designation);
        emp.setDeptId(deptId);

        if (empDAO.updateEmployee(emp)) {
            System.out.println("Employee updated successfully!");
        } else {
            System.out.println("Failed to update employee.");
        }
    }

    static void deleteEmployee() {
        System.out.println("\n--- Delete Employee ---");
        System.out.print("Enter Employee ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();
        if (empDAO.deleteEmployee(id)) {
            System.out.println("Employee deleted successfully!");
        } else {
            System.out.println("Failed to delete employee.");
        }
    }

    static void addDepartment() {
        System.out.println("\n--- Add New Department ---");
        System.out.print("Department Name: ");
        String name = sc.nextLine();
        System.out.print("Location: ");
        String location = sc.nextLine();

        Department dept = new Department(name, location);
        if (deptDAO.addDepartment(dept)) {
            System.out.println("Department added successfully!");
        } else {
            System.out.println("Failed to add department.");
        }
    }

    static void viewAllDepartments() {
        System.out.println("\n--- All Departments ---");
        List<Department> list = deptDAO.getAllDepartments();
        if (list.isEmpty()) {
            System.out.println("No departments found.");
        } else {
            list.forEach(System.out::println);
        }
    }

    static void markAttendance() {
        System.out.println("\n--- Mark Attendance ---");
        System.out.print("Enter Employee ID: ");
        int empId = sc.nextInt();
        sc.nextLine();
        System.out.println("Status: 1. Present  2. Absent");
        System.out.print("Choose: ");
        int s = sc.nextInt();
        sc.nextLine();
        String status = (s == 1) ? "Present" : "Absent";
        if (attDAO.markAttendance(empId, status)) {
            System.out.println("Attendance marked as " + status + "!");
        } else {
            System.out.println("Failed to mark attendance.");
        }
    }

    static void viewAttendance() {
        System.out.println("\n--- View Attendance ---");
        System.out.print("Enter Employee ID: ");
        int empId = sc.nextInt();
        sc.nextLine();
        attDAO.viewAttendance(empId);
    }

    static void attendancePercentage() {
        System.out.println("\n--- Attendance Percentage ---");
        System.out.print("Enter Employee ID: ");
        int empId = sc.nextInt();
        sc.nextLine();
        attDAO.getAttendancePercentage(empId);
    }

    static void exportToCSV() {
        System.out.println("\n--- Exporting Employees to CSV ---");
        List<Employee> list = empDAO.getAllEmployees();
        if (list.isEmpty()) {
            System.out.println("No employees to export.");
            return;
        }

        String fileName = "employees.csv";
        try (java.io.FileWriter fw = new java.io.FileWriter(fileName)) {
            fw.write("ID,First Name,Last Name,Email,Phone,Salary,Designation,Dept ID\n");
            for (Employee emp : list) {
                fw.write(emp.getEmpId() + "," +
                        emp.getFirstName() + "," +
                        emp.getLastName() + "," +
                        emp.getEmail() + "," +
                        emp.getPhone() + "," +
                        emp.getSalary() + "," +
                        emp.getDesignation() + "," +
                        emp.getDeptId() + "\n");
            }
            System.out.println("Exported successfully! File saved as: " + fileName);
        } catch (java.io.IOException e) {
            System.out.println("Error exporting: " + e.getMessage());
        }
    }
}