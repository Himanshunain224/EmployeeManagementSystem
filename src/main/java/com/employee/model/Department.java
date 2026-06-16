package com.employee.model;

public class Department {
    private int deptId;
    private String deptName;
    private String location;

    // Constructor
    public Department() {}

    public Department(String deptName, String location) {
        this.deptName = deptName;
        this.location = location;
    }

    // Getters and Setters
    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return "Department [ID=" + deptId + ", Name=" + deptName +
                ", Location=" + location + "]";
    }
}