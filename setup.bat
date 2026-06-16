@echo off
echo =========================================
echo   Employee Management System - Setup
echo =========================================
echo.

:: Step 1 - Clone the repo (skip if already in the folder)
IF NOT EXIST "EmployeeManagementSystem" (
    echo [1/4] Cloning repository...
    git clone https://github.com/Himanshunain224/EmployeeManagementSystem.git
    cd EmployeeManagementSystem
) ELSE (
    echo [1/4] Repository already exists, skipping clone...
    cd EmployeeManagementSystem
)

:: Step 2 - Build the project
echo.
echo [2/4] Building project with Maven...
mvn clean package -DskipTests
IF ERRORLEVEL 1 (
    echo ERROR: Maven build failed. Make sure Java 17+ and Maven are installed.
    pause
    exit /b 1
)

:: Step 3 - Create the database
echo.
echo [3/4] Setting up MySQL database...
echo Please enter your MySQL root password when prompted:
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS employee_db;"
IF ERRORLEVEL 1 (
    echo ERROR: Could not connect to MySQL. Make sure MySQL is running.
    pause
    exit /b 1
)

:: Step 4 - Run the server and open browser
echo.
echo [4/4] Starting the server...
start http://localhost:8080
java -cp target/EmployeeManagementSystem-1.0-SNAPSHOT.jar com.employee.AppServer

pause
