-- =====================================================================
-- SMART COMPLAINT REGISTRATION & MANAGEMENT PORTAL
-- MySQL 8.0 Database Schema DDL & Initial Seed Data
-- =====================================================================

CREATE DATABASE IF NOT EXISTS smart_complaint_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_complaint_db;

-- 1. Spaces Table (Tenants)
CREATE TABLE IF NOT EXISTS spaces (
    id VARCHAR(50) PRIMARY KEY, -- e.g. SPC-X7K9
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL, -- Foreign Key to spaces.id
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- CITIZEN, OFFICER, ADMIN
    department_id INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES spaces(id) ON DELETE CASCADE
);

-- 2. Departments Table
CREATE TABLE IF NOT EXISTS departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    description TEXT,
    head_officer_id INT
);

-- 3. Officers Table
CREATE TABLE IF NOT EXISTS officers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id INT NOT NULL,
    department_id INT NOT NULL,
    designation VARCHAR(100) NOT NULL,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- 4. Complaints Table
CREATE TABLE IF NOT EXISTS complaints (
    id VARCHAR(30) PRIMARY KEY, -- e.g. CMP20260001
    tenant_id VARCHAR(50) NOT NULL,
    user_id INT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    sub_category VARCHAR(100),
    location VARCHAR(200) NOT NULL,
    priority VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, EMERGENCY
    status VARCHAR(20) NOT NULL,   -- SUBMITTED, UNDER_REVIEW, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, REJECTED
    assigned_department_id INT,
    assigned_officer_id INT,
    attachment_path VARCHAR(500),
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sla_deadline DATETIME,
    resolved_at DATETIME,
    sla_status VARCHAR(20) NOT NULL, -- WITHIN_SLA, DUE_SOON, SLA_BREACHED, RESOLVED
    citizen_rating INT,
    citizen_feedback TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_officer_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 5. Complaint Logs (Audit Trail)
CREATE TABLE IF NOT EXISTS complaint_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    complaint_id VARCHAR(30) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    action_remarks TEXT,
    action_by_user_id INT,
    action_by_name VARCHAR(100),
    action_by_role VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE
);

-- 6. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id INT NOT NULL,
    complaint_id VARCHAR(30),
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
