package com.employee.model;

import java.util.Date;

public class Employee {
    private int empId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private double salary;
    private String designation;
    private int deptId;
    private Date joinDate;

    // Constructor
    public Employee() {}

    public Employee(String firstName, String lastName, String email,
                    String phone, double salary, String designation,
                    int deptId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.salary = salary;
        this.designation = designation;
        this.deptId = deptId;
    }

    // Getters and Setters
    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }

    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }

    @Override
    public String toString() {
        return "Employee [ID=" + empId + ", Name=" + firstName + " " + lastName +
                ", Email=" + email + ", Phone=" + phone +
                ", Salary=" + salary + ", Designation=" + designation + "]";
    }
}