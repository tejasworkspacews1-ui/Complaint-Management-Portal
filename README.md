# Smart Complaint Registration Portal 🏛️

A comprehensive, multi-tenant Java Desktop application for registering, tracking, and resolving municipal grievances. Built for scale, it enforces strict data isolation for different spaces, SLA tracking, and dynamic role-based access.

## Features ✨
*   **Multi-Tenancy (Spaces):** Independent organizations, spaces, or societies can use the same application without cross-data contamination.
*   **Role Hierarchy:** Strict access controls for Citizens, Officers, Space Moderators, and Super Administrators.
*   **Automated SLA Enforcement:** Automatically checks and tracks resolution deadlines and escalates overdue grievances.
*   **Real-time Dashboard:** Built-in analytics and status breakdowns (pending, resolved, breached).
*   **Modern UI Engine:** Powered by FlatLaf for a seamless, cross-platform sleek interface.

## Tech Stack 🛠️
*   **Language:** Java 8+
*   **UI Framework:** Java Swing + FlatLaf
*   **Database:** SQLite / MySQL (Multi-tenant)
*   **Architecture:** 2-Tier (Desktop to Database)

## Notice for Public Deployment 🚨
This application currently utilizes a **2-Tier Architecture** (Desktop directly connects to the remote Database). While this is excellent for internal municipal teams and trusted organizations, it is **not recommended** to distribute this executable to the general public, as database credentials must be bundled with the client application.

**Future Roadmap:** To support public citizen deployment, the architecture will be migrated to a **3-Tier REST API structure** (Java Spring Boot or Node.js Backend) to securely shield database configurations from end-users.

## Installation & Running 🚀

1.  Clone the repository:
    ```bash
    git clone <your-repository-url>
    ```
2.  Navigate to the directory and compile:
    ```bash
    .\compile.bat
    ```
3.  Run the application:
    ```bash
    .\run.bat
    ```

## Initial Setup 🔑
*   **Super Admin:** The first user to register on the platform must be configured via the database backend to gain global `ADMIN` privileges.
*   **Space Moderator:** Create a new Space from the Register screen to automatically be assigned Moderator privileges for that specific Space.

---
*Built with secure data isolation and modern Java UI design patterns.*