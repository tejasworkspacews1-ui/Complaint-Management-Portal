package com.smartcomplaint.dao;

import com.smartcomplaint.model.ComplaintCategory;
import com.smartcomplaint.model.ComplaintPriority;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.SLAStatus;
import com.smartcomplaint.util.IDGenerator;
import com.smartcomplaint.util.PasswordHasher;
import com.smartcomplaint.util.SLAEngine;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private static final String CONFIG_FILE = "db.properties";
    private static Properties config = new Properties();

    private static String dbType = "mysql"; // "mysql" or "sqlite"
    private static String host = "localhost";
    private static int port = 3306;
    private static String dbName = "smart_complaint_db";
    private static String username = "root";
    private static String password = "";
    private static String sqlitePath = "smart_complaint.db";

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                config.load(in);
                dbType = config.getProperty("db.type", "mysql");
                host = config.getProperty("db.host", "localhost");
                port = Integer.parseInt(config.getProperty("db.port", "3306"));
                dbName = config.getProperty("db.name", "smart_complaint_db");
                username = config.getProperty("db.username", "root");
                password = config.getProperty("db.password", "");
                sqlitePath = config.getProperty("db.sqlite.path", "smart_complaint.db");
            } catch (Exception e) {
                System.err.println("Warning: Could not read db.properties, using defaults: " + e.getMessage());
            }
        } else {
            saveConfig(dbType, host, port, dbName, username, password, sqlitePath);
        }
    }

    public static synchronized void saveConfig(String type, String h, int p, String name, String user, String pass, String sqPath) {
        dbType = type;
        host = h;
        port = p;
        dbName = name;
        username = user;
        password = pass;
        sqlitePath = sqPath;

        config.setProperty("db.type", dbType);
        config.setProperty("db.host", host);
        config.setProperty("db.port", String.valueOf(port));
        config.setProperty("db.name", dbName);
        config.setProperty("db.username", username);
        config.setProperty("db.password", password);
        config.setProperty("db.sqlite.path", sqlitePath);

        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            config.store(out, "Smart Complaint Portal Database Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if ("sqlite".equalsIgnoreCase(dbType)) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC Driver not found", e);
            }
            return DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
        } else {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
            return DriverManager.getConnection(url, username, password);
        }
    }

    public static boolean testConnection(String type, String h, int p, String name, String user, String pass) {
        try {
            if ("sqlite".equalsIgnoreCase(type)) {
                Class.forName("org.sqlite.JDBC");
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test_temp.db")) {
                    return conn != null && !conn.isClosed();
                } finally {
                    new File("test_temp.db").delete();
                }
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://" + h + ":" + p + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                    return conn != null && !conn.isClosed();
                }
            }
        } catch (Exception e) {
            System.err.println("DB Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Initializes database, tables and sample seed data.
     */
    public static synchronized boolean initDatabase() {
        try {
            if ("mysql".equalsIgnoreCase(dbType)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                // 1. Create DB if not exists
                String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }
            }

            // 2. Create tables
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                boolean isMySql = !"sqlite".equalsIgnoreCase(dbType);
                String autoInc = isMySql ? "AUTO_INCREMENT" : "AUTOINCREMENT";
                String timestampType = isMySql ? "DATETIME DEFAULT CURRENT_TIMESTAMP" : "TEXT";

                // Spaces Table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS spaces (" +
                        "id VARCHAR(50) PRIMARY KEY, " +
                        "name VARCHAR(150) NOT NULL, " +
                        "category VARCHAR(50) NOT NULL, " +
                        "created_by INTEGER, " +
                        "created_at " + timestampType + ")");

                // Users Table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY " + autoInc + ", " +
                        "tenant_id VARCHAR(50) DEFAULT 'DEFAULT', " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password_hash VARCHAR(128) NOT NULL, " +
                        "salt VARCHAR(64) NOT NULL, " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "phone VARCHAR(20), " +
                        "role VARCHAR(20) NOT NULL, " +
                        "department_id INTEGER, " +
                        "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                        "created_at " + timestampType + ")");

                // Backward compatibility for old databases
                try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'DEFAULT'"); } catch (Exception ignore) {}
                try { stmt.executeUpdate("ALTER TABLE departments ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'DEFAULT'"); } catch (Exception ignore) {}
                try { stmt.executeUpdate("ALTER TABLE officers ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'DEFAULT'"); } catch (Exception ignore) {}
                try { stmt.executeUpdate("ALTER TABLE complaints ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'DEFAULT'"); } catch (Exception ignore) {}
                try { stmt.executeUpdate("ALTER TABLE complaint_logs ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'DEFAULT'"); } catch (Exception ignore) {}
                try { stmt.executeUpdate("ALTER TABLE notifications ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'DEFAULT'"); } catch (Exception ignore) {}

                // Departments Table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS departments (" +
                        "id INTEGER PRIMARY KEY " + autoInc + ", " +
                        "name VARCHAR(100) UNIQUE NOT NULL, " +
                        "code VARCHAR(20) UNIQUE NOT NULL, " +
                        "description TEXT, " +
                        "head_officer_id INTEGER)");

                // Officers Table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS officers (" +
                        "id INTEGER PRIMARY KEY " + autoInc + ", " +
                        "user_id INTEGER UNIQUE NOT NULL, " +
                        "department_id INTEGER NOT NULL, " +
                        "designation VARCHAR(100) NOT NULL, " +
                        "employee_code VARCHAR(50) UNIQUE NOT NULL)");

                // Complaints Table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS complaints (" +
                        "id VARCHAR(30) PRIMARY KEY, " +
                        "user_id INTEGER, " +
                        "title VARCHAR(200) NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "category VARCHAR(50) NOT NULL, " +
                        "sub_category VARCHAR(100), " +
                        "location VARCHAR(200) NOT NULL, " +
                        "priority VARCHAR(20) NOT NULL, " +
                        "status VARCHAR(20) NOT NULL, " +
                        "assigned_department_id INTEGER, " +
                        "assigned_officer_id INTEGER, " +
                        "attachment_path VARCHAR(500), " +
                        "is_anonymous BOOLEAN DEFAULT FALSE, " +
                        "created_at " + timestampType + ", " +
                        "sla_deadline " + (isMySql ? "DATETIME" : "TEXT") + ", " +
                        "resolved_at " + (isMySql ? "DATETIME" : "TEXT") + ", " +
                        "sla_status VARCHAR(20) NOT NULL, " +
                        "citizen_rating INTEGER, " +
                        "citizen_feedback TEXT)");

                // Complaint Logs (Audit Trail)
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS complaint_logs (" +
                        "id INTEGER PRIMARY KEY " + autoInc + ", " +
                        "complaint_id VARCHAR(30) NOT NULL, " +
                        "from_status VARCHAR(20), " +
                        "to_status VARCHAR(20) NOT NULL, " +
                        "action_remarks TEXT, " +
                        "action_by_user_id INTEGER, " +
                        "action_by_name VARCHAR(100), " +
                        "action_by_role VARCHAR(20), " +
                        "created_at " + timestampType + ")");

                // Notifications
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notifications (" +
                        "id INTEGER PRIMARY KEY " + autoInc + ", " +
                        "user_id INTEGER NOT NULL, " +
                        "complaint_id VARCHAR(30), " +
                        "title VARCHAR(150) NOT NULL, " +
                        "message TEXT NOT NULL, " +
                        "is_read BOOLEAN DEFAULT FALSE, " +
                        "created_at " + timestampType + ")");

                // Seed Default Data if empty
                seedInitialData(conn);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Seed DEFAULT space
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM spaces WHERE id = 'DEFAULT'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps2 = conn.prepareStatement("INSERT INTO spaces (id, name, category, created_by) VALUES ('DEFAULT', 'Default Public Space', 'Public', 1)")) {
                    ps2.executeUpdate();
                }
            }
        }

        // Check if admin user exists
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = 'admin'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        }

        // 1. Seed Departments
        String[] depts = {
                "Water Supply & Drainage", "WTR", "Handles public water pipelines, leaks, drainage and quality issues",
                "Electricity & Power", "ELEC", "Handles electrical grids, streetlights, power cuts and line safety",
                "Roads & Traffic", "RDS", "Handles road repairs, potholes, traffic signaling and transport obstacles",
                "Garbage & Sanitation", "SNT", "Handles waste management, garbage clearance and municipal hygiene",
                "Public Safety", "SEC", "Handles public hazards, lighting in dark areas, and municipal security"
        };
        for (int i = 0; i < depts.length; i += 3) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO departments (name, code, description) VALUES (?, ?, ?)")) {
                ps.setString(1, depts[i]);
                ps.setString(2, depts[i + 1]);
                ps.setString(3, depts[i + 2]);
                ps.executeUpdate();
            }
        }

        // 2. Seed Users & Officers
        // Admin
        insertUser(conn, "DEFAULT", "admin", "admin123", "System Administrator", "admin@portal.gov.in", "9876543210", Role.ADMIN, null);

        // Officers
        int off1 = insertUser(conn, "DEFAULT", "officer_water", "officer123", "Rajesh Sharma (Water Officer)", "water.officer@portal.gov.in", "9876543211", Role.OFFICER, 1);
        int off2 = insertUser(conn, "DEFAULT", "officer_elec", "officer123", "Priya Verma (Power Officer)", "elec.officer@portal.gov.in", "9876543212", Role.OFFICER, 2);
        int off3 = insertUser(conn, "DEFAULT", "officer_roads", "officer123", "Amit Patel (Roads Officer)", "roads.officer@portal.gov.in", "9876543213", Role.OFFICER, 3);
        int off4 = insertUser(conn, "DEFAULT", "officer_snt", "officer123", "Sunita Rao (Sanitation Officer)", "snt.officer@portal.gov.in", "9876543214", Role.OFFICER, 4);

        insertOfficerRecord(conn, off1, 1, "Senior Water Works Engineer", "EMP-WTR-101");
        insertOfficerRecord(conn, off2, 2, "Assistant Electrical Inspector", "EMP-ELC-102");
        insertOfficerRecord(conn, off3, 3, "Chief Municipal Road Surveyor", "EMP-RDS-103");
        insertOfficerRecord(conn, off4, 4, "Public Health & Sanitation Inspector", "EMP-SNT-104");

        // Citizen
        int citizenId = insertUser(conn, "DEFAULT", "citizen1", "citizen123", "Rahul Mehta", "rahul.mehta@gmail.com", "9812345678", Role.CITIZEN, null);
        int citizenId2 = insertUser(conn, "DEFAULT", "citizen2", "citizen123", "Ananya Sen", "ananya.sen@gmail.com", "9823456789", Role.CITIZEN, null);

        // 3. Seed Sample Complaints with diverse SLA states
        long now = System.currentTimeMillis();
        long hour = 3600 * 1000L;
        long day = 24 * hour;

        // Complaint 1: Emergency Water Burst - Due Soon
        seedComplaint(conn, "CMP20260001", citizenId, "Severe Pipeline Burst in Sector 4",
                "Main water line cracked near Central Market; gallons of drinking water flooding road.",
                ComplaintCategory.WATER, "Pipe Leakage/Burst", "Sector 4 Market Circle, North Ward",
                ComplaintPriority.EMERGENCY, ComplaintStatus.IN_PROGRESS, 1, off1,
                new Timestamp(now - 18 * hour), new Timestamp(now + 6 * hour), null, SLAStatus.DUE_SOON);

        // Complaint 2: High Priority Road Hazard - SLA Breached (Unresolved past 48h)
        seedComplaint(conn, "CMP20260002", citizenId, "Huge Pothole Causing Accidents on Ring Road",
                "Deep 2-foot pothole right after the bridge turn. Two motorcyclists slipped yesterday.",
                ComplaintCategory.ROADS, "Potholes & Broken Road", "Ring Road Flyover exit, West Wing",
                ComplaintPriority.HIGH, ComplaintStatus.ASSIGNED, 3, off3,
                new Timestamp(now - 60 * hour), new Timestamp(now - 12 * hour), null, SLAStatus.SLA_BREACHED);

        // Complaint 3: Medium Priority Garbage - Within SLA
        seedComplaint(conn, "CMP20260003", citizenId2, "Community Dumpster Overflowing for 3 Days",
                "Garbage container overflowing onto the pedestrian pathway. Foul smell and stray animal gathering.",
                ComplaintCategory.GARBAGE, "Garbage Not Collected", "Block C, Green Park Residency",
                ComplaintPriority.MEDIUM, ComplaintStatus.UNDER_REVIEW, 4, off4,
                new Timestamp(now - 1 * day), new Timestamp(now + 4 * day), null, SLAStatus.WITHIN_SLA);

        // Complaint 4: Low Priority Street Light - Resolved on time
        seedComplaint(conn, "CMP20260004", citizenId, "Street Light Flickering Constantly",
                "Street lamp pole #42 on 5th Avenue blinking intermittently causing visibility hazard.",
                ComplaintCategory.ELECTRICITY, "Street Light Not Working", "5th Avenue, Lane 3",
                ComplaintPriority.LOW, ComplaintStatus.RESOLVED, 2, off2,
                new Timestamp(now - 5 * day), new Timestamp(now + 2 * day), new Timestamp(now - 2 * day), SLAStatus.RESOLVED);

        // Complaint 5: Public Safety - Submitted Fresh
        seedComplaint(conn, "CMP20260005", citizenId2, "Open Manhole Without Warning Sign",
                "Uncovered manhole in front of Sunshine Elementary School. Extreme danger for children.",
                ComplaintCategory.PUBLIC_SAFETY, "Open Manhole/Hazard", "Opposite Gate 2, Sunshine School",
                ComplaintPriority.EMERGENCY, ComplaintStatus.SUBMITTED, 5, null,
                new Timestamp(now - 2 * hour), new Timestamp(now + 22 * hour), null, SLAStatus.WITHIN_SLA);
    }

    private static int insertUser(Connection conn, String tenantId, String user, String pass, String name, String email, String phone, Role role, Integer deptId) throws SQLException {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(pass, salt);

        String sql = "INSERT INTO users (tenant_id, username, password_hash, salt, full_name, email, phone, role, department_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tenantId);
            ps.setString(2, user);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, name);
            ps.setString(6, email);
            ps.setString(7, phone);
            ps.setString(8, role.name());
            if (deptId != null) ps.setInt(9, deptId); else ps.setNull(9, Types.INTEGER);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    private static void insertOfficerRecord(Connection conn, int userId, int deptId, String desig, String empCode) throws SQLException {
        String sql = "INSERT INTO officers (user_id, department_id, designation, employee_code) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, deptId);
            ps.setString(3, desig);
            ps.setString(4, empCode);
            ps.executeUpdate();
        }
    }

    private static void seedComplaint(Connection conn, String id, int userId, String title, String desc,
                                     ComplaintCategory cat, String subCat, String loc,
                                     ComplaintPriority prio, ComplaintStatus status,
                                     int deptId, Integer offUserId,
                                     Timestamp created, Timestamp deadline, Timestamp resolved, SLAStatus slaStatus) throws SQLException {
        String sql = "INSERT INTO complaints (id, tenant_id, user_id, title, description, category, sub_category, location, " +
                "priority, status, assigned_department_id, assigned_officer_id, is_anonymous, created_at, sla_deadline, resolved_at, sla_status) " +
                "VALUES (?, 'DEFAULT', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setInt(2, userId);
            ps.setString(3, title);
            ps.setString(4, desc);
            ps.setString(5, cat.name());
            ps.setString(6, subCat);
            ps.setString(7, loc);
            ps.setString(8, prio.name());
            ps.setString(9, status.name());
            ps.setInt(10, deptId);
            if (offUserId != null) ps.setInt(11, offUserId); else ps.setNull(11, Types.INTEGER);
            ps.setTimestamp(12, created);
            ps.setTimestamp(13, deadline);
            ps.setTimestamp(14, resolved);
            ps.setString(15, slaStatus.name());
            ps.executeUpdate();
        }

        // Add initial log
        String logSql = "INSERT INTO complaint_logs (tenant_id, complaint_id, from_status, to_status, action_remarks, action_by_name, action_by_role, created_at) " +
                "VALUES ('DEFAULT', ?, null, ?, ?, 'System Auto-Dispatcher', 'ADMIN', ?)";
        try (PreparedStatement ps = conn.prepareStatement(logSql)) {
            ps.setString(1, id);
            ps.setString(2, status.name());
            ps.setString(3, "Initial complaint logged & registered with SLA priority: " + prio.getDisplayName());
            ps.setTimestamp(4, created);
            ps.executeUpdate();
        }
    }

    public static String getDbType() { return dbType; }
    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDbName() { return dbName; }
    public static String getUsername() { return username; }
    public static String getPassword() { return password; }
}
