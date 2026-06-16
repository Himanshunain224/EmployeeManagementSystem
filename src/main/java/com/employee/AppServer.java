package com.employee.main;

import com.employee.dao.AttendanceDAO;
import com.employee.dao.DepartmentDAO;
import com.employee.dao.EmployeeDAO;
import com.employee.model.Department;
import com.employee.model.Employee;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AppServer {

    static EmployeeDAO empDAO = new EmployeeDAO();
    static DepartmentDAO deptDAO = new DepartmentDAO();
    static AttendanceDAO attDAO = new AttendanceDAO();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", AppServer::handleIndex);
        server.createContext("/employees", AppServer::handleEmployees);
        server.createContext("/departments", AppServer::handleDepartments);
        server.createContext("/attendance", AppServer::handleAttendance);
        server.createContext("/api/employees", AppServer::apiEmployees);
        server.createContext("/api/departments", AppServer::apiDepartments);
        server.createContext("/api/attendance", AppServer::apiAttendance);
        server.createContext("/api/stats", AppServer::apiStats);

        server.start();
        System.out.println("Server started at http://localhost:8080");
        System.out.println("Open your browser and go to: http://localhost:8080");
    }

    // ===================== PAGE HANDLERS =====================

    static void handleIndex(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) { ex.sendResponseHeaders(405, -1); return; }
        String html = buildDashboardPage();
        sendHtml(ex, html);
    }

    static void handleEmployees(HttpExchange ex) throws IOException {
        String html = buildEmployeesPage();
        sendHtml(ex, html);
    }

    static void handleDepartments(HttpExchange ex) throws IOException {
        String html = buildDepartmentsPage();
        sendHtml(ex, html);
    }

    static void handleAttendance(HttpExchange ex) throws IOException {
        String html = buildAttendancePage();
        sendHtml(ex, html);
    }

    // ===================== API HANDLERS =====================

    static void apiStats(HttpExchange ex) throws IOException {
        int empCount = empDAO.getAllEmployees().size();
        int deptCount = deptDAO.getAllDepartments().size();
        String json = "{\"employees\":" + empCount + ",\"departments\":" + deptCount + "}";
        sendJson(ex, json);
    }

    static void apiEmployees(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        if (method.equals("GET")) {
            String query = ex.getRequestURI().getQuery();
            List<Employee> list;
            if (query != null && query.startsWith("search=")) {
                String name = URLDecoder.decode(query.substring(7), StandardCharsets.UTF_8);
                list = empDAO.searchByName(name);
            } else {
                list = empDAO.getAllEmployees();
            }
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Employee e = list.get(i);
                sb.append("{\"empId\":").append(e.getEmpId())
                        .append(",\"firstName\":\"").append(e.getFirstName()).append("\"")
                        .append(",\"lastName\":\"").append(e.getLastName()).append("\"")
                        .append(",\"email\":\"").append(e.getEmail()).append("\"")
                        .append(",\"phone\":\"").append(e.getPhone()).append("\"")
                        .append(",\"salary\":").append(e.getSalary())
                        .append(",\"designation\":\"").append(e.getDesignation()).append("\"")
                        .append(",\"deptId\":").append(e.getDeptId())
                        .append("}");
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(ex, sb.toString());

        } else if (method.equals("POST")) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseForm(body);
            Employee emp = new Employee(
                    params.get("firstName"), params.get("lastName"),
                    params.get("email"), params.get("phone"),
                    Double.parseDouble(params.getOrDefault("salary", "0")),
                    params.get("designation"),
                    Integer.parseInt(params.getOrDefault("deptId", "1"))
            );
            boolean ok = empDAO.addEmployee(emp);
            sendJson(ex, "{\"success\":" + ok + "}");

        } else if (method.equals("DELETE")) {
            String query = ex.getRequestURI().getQuery();
            int id = Integer.parseInt(query.replace("id=", ""));
            boolean ok = empDAO.deleteEmployee(id);
            sendJson(ex, "{\"success\":" + ok + "}");

        } else if (method.equals("PUT")) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseForm(body);
            Employee emp = new Employee(
                    params.get("firstName"), params.get("lastName"),
                    params.get("email"), params.get("phone"),
                    Double.parseDouble(params.getOrDefault("salary", "0")),
                    params.get("designation"),
                    Integer.parseInt(params.getOrDefault("deptId", "1"))
            );
            emp.setEmpId(Integer.parseInt(params.getOrDefault("empId", "0")));
            boolean ok = empDAO.updateEmployee(emp);
            sendJson(ex, "{\"success\":" + ok + "}");

        } else {
            ex.sendResponseHeaders(405, -1);
        }
    }

    static void apiDepartments(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (method.equals("GET")) {
            var list = deptDAO.getAllDepartments();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                var d = list.get(i);
                sb.append("{\"deptId\":").append(d.getDeptId())
                        .append(",\"deptName\":\"").append(d.getDeptName()).append("\"")
                        .append(",\"location\":\"").append(d.getLocation()).append("\"")
                        .append("}");
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(ex, sb.toString());
        } else if (method.equals("POST")) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseForm(body);
            Department dept = new Department(params.get("deptName"), params.get("location"));
            boolean ok = deptDAO.addDepartment(dept);
            sendJson(ex, "{\"success\":" + ok + "}");
        } else if (method.equals("DELETE")) {
            String query = ex.getRequestURI().getQuery();
            int id = Integer.parseInt(query.replace("id=", ""));
            boolean ok = deptDAO.deleteDepartment(id);
            sendJson(ex, "{\"success\":" + ok + "}");
        }
    }

    static void apiAttendance(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (method.equals("POST")) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseForm(body);
            int empId = Integer.parseInt(params.getOrDefault("empId", "0"));
            String status = params.getOrDefault("status", "Present");
            boolean ok = attDAO.markAttendance(empId, status);
            sendJson(ex, "{\"success\":" + ok + "}");
        } else if (method.equals("GET")) {
            String query = ex.getRequestURI().getQuery();
            if (query != null && query.startsWith("empId=")) {
                int empId = Integer.parseInt(query.substring(6));
                String result = attDAO.getAttendancePercentage(empId);
                sendJson(ex, "{\"result\":\"" + result.replace("\n", "\\n") + "\"}");
            }
        }
    }

    // ===================== HTML PAGES =====================

    static String getNavBar(String active) {
        return """
        <nav>
            <div class="nav-brand">👥 EMS</div>
            <div class="nav-links">
                <a href="/" class="%s">🏠 Dashboard</a>
                <a href="/employees" class="%s">👤 Employees</a>
                <a href="/departments" class="%s">🏢 Departments</a>
                <a href="/attendance" class="%s">📅 Attendance</a>
            </div>
        </nav>
        """.formatted(
                active.equals("dashboard") ? "active" : "",
                active.equals("employees") ? "active" : "",
                active.equals("departments") ? "active" : "",
                active.equals("attendance") ? "active" : ""
        );
    }

    static String getCSS() {
        return """
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: 'Segoe UI', sans-serif; background: #f0f2f5; color: #333; }
            nav { background: linear-gradient(135deg, #1e3c72, #2a5298); padding: 15px 30px;
                  display: flex; align-items: center; justify-content: space-between;
                  box-shadow: 0 2px 10px rgba(0,0,0,0.2); }
            .nav-brand { color: white; font-size: 22px; font-weight: bold; }
            .nav-links a { color: rgba(255,255,255,0.8); text-decoration: none;
                           margin-left: 20px; padding: 8px 16px; border-radius: 20px;
                           transition: all 0.3s; font-size: 14px; }
            .nav-links a:hover, .nav-links a.active { background: rgba(255,255,255,0.2);
                           color: white; }
            .container { max-width: 1100px; margin: 30px auto; padding: 0 20px; }
            .page-title { font-size: 26px; font-weight: bold; margin-bottom: 25px;
                          color: #1e3c72; border-left: 4px solid #2a5298; padding-left: 12px; }
            .card { background: white; border-radius: 12px; padding: 25px;
                    box-shadow: 0 2px 15px rgba(0,0,0,0.08); margin-bottom: 25px; }
            .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                          gap: 20px; margin-bottom: 25px; }
            .stat-card { background: white; border-radius: 12px; padding: 25px; text-align: center;
                         box-shadow: 0 2px 15px rgba(0,0,0,0.08); border-top: 4px solid #2a5298; }
            .stat-number { font-size: 42px; font-weight: bold; color: #2a5298; }
            .stat-label { color: #888; margin-top: 8px; font-size: 14px; }
            .btn { padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer;
                   font-size: 14px; font-weight: 600; transition: all 0.3s; }
            .btn-primary { background: #2a5298; color: white; }
            .btn-primary:hover { background: #1e3c72; transform: translateY(-1px); }
            .btn-danger { background: #e74c3c; color: white; }
            .btn-danger:hover { background: #c0392b; }
            .btn-success { background: #27ae60; color: white; }
            .btn-success:hover { background: #219a52; }
            table { width: 100%%; border-collapse: collapse; }
            th { background: #f8f9fa; padding: 12px 15px; text-align: left;
                 font-weight: 600; color: #555; border-bottom: 2px solid #eee; }
            td { padding: 12px 15px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
            tr:hover td { background: #f8f9ff; }
            .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
            .form-group { display: flex; flex-direction: column; gap: 6px; }
            .form-group label { font-size: 13px; font-weight: 600; color: #555; }
            .form-group input, .form-group select {
                padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px;
                font-size: 14px; transition: border 0.3s; outline: none; }
            .form-group input:focus, .form-group select:focus { border-color: #2a5298; }
            .form-full { grid-column: 1 / -1; }
            .alert { padding: 12px 18px; border-radius: 8px; margin-bottom: 15px;
                     display: none; font-weight: 500; }
            .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
            .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
            .search-bar { display: flex; gap: 10px; margin-bottom: 20px; }
            .search-bar input { flex: 1; padding: 10px 14px; border: 1px solid #ddd;
                                border-radius: 8px; font-size: 14px; outline: none; }
            .badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
            .badge-present { background: #d4edda; color: #155724; }
            .badge-absent { background: #f8d7da; color: #721c24; }
        </style>
        """;
    }

    static String buildDashboardPage() {
        return """
        <!DOCTYPE html>
        <html>
        <head><title>EMS Dashboard</title>%s</head>
        <body>
        %s
        <div class="container">
            <div class="page-title">🏠 Dashboard</div>
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-number" id="empCount">...</div>
                    <div class="stat-label">Total Employees</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number" id="deptCount">...</div>
                    <div class="stat-label">Departments</div>
                </div>
            </div>
            <div class="card">
                <h3 style="margin-bottom:15px;color:#1e3c72;">📋 Recent Employees</h3>
                <table id="recentTable">
                    <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Designation</th><th>Salary</th></tr></thead>
                    <tbody id="recentBody"><tr><td colspan="5">Loading...</td></tr></tbody>
                </table>
            </div>
        </div>
        <script>
            fetch('/api/stats').then(r=>r.json()).then(d=>{
                document.getElementById('empCount').textContent = d.employees;
                document.getElementById('deptCount').textContent = d.departments;
            });
            fetch('/api/employees').then(r=>r.json()).then(data=>{
                const tbody = document.getElementById('recentBody');
                if(data.length === 0){ tbody.innerHTML='<tr><td colspan="5">No employees found</td></tr>'; return; }
                tbody.innerHTML = data.slice(0,5).map(e=>`
                    <tr>
                        <td>${e.empId}</td>
                        <td>${e.firstName} ${e.lastName}</td>
                        <td>${e.email}</td>
                        <td>${e.designation}</td>
                        <td>₹${e.salary.toLocaleString()}</td>
                    </tr>`).join('');
            });
        </script>
        </body></html>
        """.formatted(getCSS(), getNavBar("dashboard"));
    }

    static String buildEmployeesPage() {
        return """
    <!DOCTYPE html>
    <html>
    <head><title>Employees</title>%s</head>
    <body>
    %s
    <div class="container">
        <div class="page-title">👤 Employee Management</div>
        <div class="card">
            <h3 style="margin-bottom:15px;color:#1e3c72;">➕ Add New Employee</h3>
            <div id="alert" class="alert"></div>
            <div class="form-grid">
                <div class="form-group"><label>First Name</label><input id="firstName" placeholder="Enter first name"/></div>
                <div class="form-group"><label>Last Name</label><input id="lastName" placeholder="Enter last name"/></div>
                <div class="form-group"><label>Email</label><input id="email" type="email" placeholder="Enter email"/></div>
                <div class="form-group"><label>Phone</label><input id="phone" placeholder="Enter phone number"/></div>
                <div class="form-group"><label>Salary</label><input id="salary" type="number" placeholder="Enter salary"/></div>
                <div class="form-group"><label>Designation</label><input id="designation" placeholder="Enter designation"/></div>
                <div class="form-group"><label>Department ID</label><input id="deptId" type="number" placeholder="Enter dept ID"/></div>
                <div class="form-group" style="justify-content:flex-end;padding-top:20px;">
                    <button class="btn btn-primary" onclick="addEmployee()">➕ Add Employee</button>
                </div>
            </div>
        </div>
        <div class="card">
            <h3 style="margin-bottom:15px;color:#1e3c72;">👥 All Employees</h3>
            <div class="search-bar">
                <input id="searchInput" placeholder="🔍 Search by name..." onkeyup="searchEmployees()"/>
                <button class="btn btn-primary" onclick="loadEmployees()">Refresh</button>
            </div>
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Designation</th><th>Salary</th><th>Actions</th></tr></thead>
                <tbody id="empBody"><tr><td colspan="7">Loading...</td></tr></tbody>
            </table>
        </div>
    </div>

    <!-- Edit Modal -->
    <div id="editModal" style="display:none;position:fixed;top:0;left:0;width:100%%;height:100%%;
         background:rgba(0,0,0,0.5);z-index:1000;justify-content:center;align-items:center;">
        <div style="background:white;border-radius:12px;padding:30px;width:600px;max-width:90%%;
                    box-shadow:0 10px 40px rgba(0,0,0,0.3);">
            <h3 style="margin-bottom:20px;color:#1e3c72;">✏️ Update Employee</h3>
            <div id="editAlert" class="alert"></div>
            <input type="hidden" id="editEmpId"/>
            <div class="form-grid">
                <div class="form-group"><label>First Name</label><input id="editFirstName"/></div>
                <div class="form-group"><label>Last Name</label><input id="editLastName"/></div>
                <div class="form-group"><label>Email</label><input id="editEmail" type="email"/></div>
                <div class="form-group"><label>Phone</label><input id="editPhone"/></div>
                <div class="form-group"><label>Salary</label><input id="editSalary" type="number"/></div>
                <div class="form-group"><label>Designation</label><input id="editDesignation"/></div>
                <div class="form-group"><label>Department ID</label><input id="editDeptId" type="number"/></div>
            </div>
            <div style="display:flex;gap:10px;margin-top:20px;justify-content:flex-end;">
                <button class="btn" style="background:#eee;color:#333;"
                    onclick="document.getElementById('editModal').style.display='none'">Cancel</button>
                <button class="btn btn-primary" onclick="updateEmployee()">💾 Save Changes</button>
            </div>
        </div>
    </div>

    <script>
        function showAlert(msg, type) {
            const a = document.getElementById('alert');
            a.className = 'alert alert-' + type;
            a.textContent = msg;
            a.style.display = 'block';
            setTimeout(()=>a.style.display='none', 3000);
        }
        function loadEmployees(search='') {
            const url = search ? '/api/employees?search='+encodeURIComponent(search) : '/api/employees';
            fetch(url).then(r=>r.json()).then(data=>{
                const tbody = document.getElementById('empBody');
                if(data.length===0){ tbody.innerHTML='<tr><td colspan="7">No employees found</td></tr>'; return; }
                tbody.innerHTML = data.map(e=>`
                    <tr>
                        <td>${e.empId}</td>
                        <td>${e.firstName} ${e.lastName}</td>
                        <td>${e.email}</td>
                        <td>${e.phone}</td>
                        <td>${e.designation}</td>
                        <td>₹${e.salary.toLocaleString()}</td>
                        <td style="display:flex;gap:8px;">
                            <button class="btn btn-primary" style="padding:6px 12px;font-size:12px;"
                                onclick='editEmployee(${JSON.stringify(e)})'>✏️ Edit</button>
                            <button class="btn btn-danger" style="padding:6px 12px;font-size:12px;"
                                onclick="deleteEmployee(${e.empId})">🗑 Delete</button>
                        </td>
                    </tr>`).join('');
            });
        }
        function searchEmployees() {
            loadEmployees(document.getElementById('searchInput').value);
        }
        function addEmployee() {
            const data = new URLSearchParams({
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value,
                salary: document.getElementById('salary').value,
                designation: document.getElementById('designation').value,
                deptId: document.getElementById('deptId').value,
            });
            fetch('/api/employees', {method:'POST', body: data})
                .then(r=>r.json()).then(d=>{
                    if(d.success){ showAlert('Employee added successfully!', 'success'); loadEmployees(); }
                    else showAlert('Failed to add employee!', 'error');
                });
        }
        function editEmployee(e) {
            document.getElementById('editModal').style.display = 'flex';
            document.getElementById('editEmpId').value = e.empId;
            document.getElementById('editFirstName').value = e.firstName;
            document.getElementById('editLastName').value = e.lastName;
            document.getElementById('editEmail').value = e.email;
            document.getElementById('editPhone').value = e.phone;
            document.getElementById('editSalary').value = e.salary;
            document.getElementById('editDesignation').value = e.designation;
            document.getElementById('editDeptId').value = e.deptId;
        }
        function updateEmployee() {
            const data = new URLSearchParams({
                empId: document.getElementById('editEmpId').value,
                firstName: document.getElementById('editFirstName').value,
                lastName: document.getElementById('editLastName').value,
                email: document.getElementById('editEmail').value,
                phone: document.getElementById('editPhone').value,
                salary: document.getElementById('editSalary').value,
                designation: document.getElementById('editDesignation').value,
                deptId: document.getElementById('editDeptId').value,
            });
            fetch('/api/employees', {method:'PUT', body: data})
                .then(r=>r.json()).then(d=>{
                    if(d.success){
                        document.getElementById('editModal').style.display = 'none';
                        showAlert('Employee updated successfully!', 'success');
                        loadEmployees();
                    } else showAlert('Failed to update!', 'error');
                });
        }
        function deleteEmployee(id) {
            if(!confirm('Delete this employee?')) return;
            fetch('/api/employees?id='+id, {method:'DELETE'})
                .then(r=>r.json()).then(d=>{
                    if(d.success){ showAlert('Employee deleted!', 'success'); loadEmployees(); }
                    else showAlert('Failed to delete!', 'error');
                });
        }
        loadEmployees();
    </script>
    </body></html>
    """.formatted(getCSS(), getNavBar("employees"));
    }

    static String buildDepartmentsPage() {
        return """
        <!DOCTYPE html>
        <html>
        <head><title>Departments</title>%s</head>
        <body>
        %s
        <div class="container">
            <div class="page-title">🏢 Department Management</div>
            <div class="card">
                <h3 style="margin-bottom:15px;color:#1e3c72;">➕ Add New Department</h3>
                <div id="alert" class="alert"></div>
                <div class="form-grid">
                    <div class="form-group"><label>Department Name</label><input id="deptName" placeholder="Enter department name"/></div>
                    <div class="form-group"><label>Location</label><input id="location" placeholder="Enter location"/></div>
                    <div class="form-group form-full" style="align-items:flex-start;">
                        <button class="btn btn-primary" onclick="addDept()">➕ Add Department</button>
                    </div>
                </div>
            </div>
            <div class="card">
                <h3 style="margin-bottom:15px;color:#1e3c72;">🏢 All Departments</h3>
                <table>
                    <thead><tr><th>ID</th><th>Department Name</th><th>Location</th><th>Action</th></tr></thead>
                    <tbody id="deptBody"><tr><td colspan="4">Loading...</td></tr></tbody>
                </table>
            </div>
        </div>
        <script>
            function showAlert(msg, type) {
                const a = document.getElementById('alert');
                a.className = 'alert alert-' + type;
                a.textContent = msg;
                a.style.display = 'block';
                setTimeout(()=>a.style.display='none', 3000);
            }
            function loadDepts() {
                fetch('/api/departments').then(r=>r.json()).then(data=>{
                    const tbody = document.getElementById('deptBody');
                    if(data.length===0){ tbody.innerHTML='<tr><td colspan="4">No departments found</td></tr>'; return; }
                    tbody.innerHTML = data.map(d=>`
                        <tr>
                            <td>${d.deptId}</td>
                            <td>${d.deptName}</td>
                            <td>${d.location}</td>
                            <td><button class="btn btn-danger" onclick="deleteDept(${d.deptId})">🗑 Delete</button></td>
                        </tr>`).join('');
                });
            }
            function addDept() {
                const data = new URLSearchParams({
                    deptName: document.getElementById('deptName').value,
                    location: document.getElementById('location').value,
                });
                fetch('/api/departments', {method:'POST', body: data})
                    .then(r=>r.json()).then(d=>{
                        if(d.success){ showAlert('Department added!', 'success'); loadDepts(); }
                        else showAlert('Failed!', 'error');
                    });
            }
            function deleteDept(id) {
                if(!confirm('Delete this department?')) return;
                fetch('/api/departments?id='+id, {method:'DELETE'})
                    .then(r=>r.json()).then(d=>{
                        if(d.success){ showAlert('Deleted!', 'success'); loadDepts(); }
                        else showAlert('Failed!', 'error');
                    });
            }
            loadDepts();
        </script>
        </body></html>
        """.formatted(getCSS(), getNavBar("departments"));
    }

    static String buildAttendancePage() {
        return """
    <!DOCTYPE html>
    <html>
    <head><title>Attendance</title>%s</head>
    <body>
    %s
    <div class="container">
        <div class="page-title">📅 Attendance Management</div>
        <div class="card">
            <h3 style="margin-bottom:15px;color:#1e3c72;">✅ Mark Attendance</h3>
            <div id="alert" class="alert"></div>
            <div class="form-grid">
                <div class="form-group"><label>Employee ID</label><input id="empId" type="number" placeholder="Enter employee ID"/></div>
                <div class="form-group"><label>Status</label>
                    <select id="status">
                        <option value="Present">Present</option>
                        <option value="Absent">Absent</option>
                    </select>
                </div>
                <div class="form-group form-full" style="align-items:flex-start;">
                    <button class="btn btn-success" onclick="markAtt()">✅ Mark Attendance</button>
                </div>
            </div>
        </div>
        <div class="card">
            <h3 style="margin-bottom:15px;color:#1e3c72;">📊 Check Attendance Percentage</h3>
            <div class="form-grid">
                <div class="form-group"><label>Employee ID</label><input id="checkEmpId" type="number" placeholder="Enter employee ID"/></div>
                <div class="form-group" style="justify-content:flex-end;padding-top:20px;">
                    <button class="btn btn-primary" onclick="checkAtt()">📊 Check</button>
                </div>
            </div>
            <div id="attResult" style="margin-top:15px;padding:15px;background:#f8f9fa;border-radius:8px;display:none;white-space:pre-line;font-family:monospace;font-size:15px;line-height:1.8;"></div>
        </div>
        <div class="card">
            <h3 style="margin-bottom:15px;color:#1e3c72;">📁 Export Employees to CSV</h3>
            <p style="color:#666;margin-bottom:15px;">Download all employee records as a CSV file.</p>
            <button class="btn btn-success" onclick="exportCSV()">⬇️ Export to CSV</button>
            <div id="exportAlert" class="alert" style="margin-top:10px;"></div>
        </div>
    </div>
    <script>
        function showAlert(msg, type) {
            const a = document.getElementById('alert');
            a.className = 'alert alert-' + type;
            a.textContent = msg;
            a.style.display = 'block';
            setTimeout(()=>a.style.display='none', 3000);
        }
        function markAtt() {
            const empId = document.getElementById('empId').value;
            if(!empId){ showAlert('Please enter Employee ID!', 'error'); return; }
            const data = new URLSearchParams({
                empId: empId,
                status: document.getElementById('status').value,
            });
            fetch('/api/attendance', {method:'POST', body: data})
                .then(r=>r.json()).then(d=>{
                    if(d.success) showAlert('Attendance marked successfully!', 'success');
                    else showAlert('Failed! Already marked today or invalid ID.', 'error');
                });
        }
        function checkAtt() {
            const id = document.getElementById('checkEmpId').value;
            if(!id){ alert('Please enter Employee ID!'); return; }
            fetch('/api/attendance?empId=' + id)
                .then(r=>r.json())
                .then(d=>{
                    const div = document.getElementById('attResult');
                    div.style.display = 'block';
                    div.textContent = d.result || 'No records found';
                })
                .catch(err => {
                    const div = document.getElementById('attResult');
                    div.style.display = 'block';
                    div.textContent = 'Error loading data!';
                });
        }
        function exportCSV() {
            fetch('/api/employees').then(r=>r.json()).then(data=>{
                if(data.length===0){ alert('No employees to export!'); return; }
                let csv = 'ID,First Name,Last Name,Email,Phone,Salary,Designation,Dept ID\\n';
                data.forEach(e=>{
                    csv += `${e.empId},${e.firstName},${e.lastName},${e.email},${e.phone},${e.salary},${e.designation},${e.deptId}\\n`;
                });
                const blob = new Blob([csv], {type:'text/csv'});
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'employees.csv';
                a.click();
                const ea = document.getElementById('exportAlert');
                ea.className = 'alert alert-success';
                ea.textContent = 'CSV exported successfully!';
                ea.style.display = 'block';
                setTimeout(()=>ea.style.display='none', 3000);
            });
        }
    </script>
    </body></html>
    """.formatted(getCSS(), getNavBar("attendance"));
    }
    // ===================== HELPERS =====================

    static void sendHtml(HttpExchange ex, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    static void sendJson(HttpExchange ex, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    static Map<String, String> parseForm(String body) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2)
                map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return map;
    }
}