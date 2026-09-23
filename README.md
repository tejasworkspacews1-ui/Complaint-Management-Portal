# Complaint Management Portal

A professional Java Swing desktop application for registering, tracking, escalating, and resolving civic complaints with role-based workflows, SLA monitoring, audit trails, notifications, and administrative reporting.

> **Developer:** Tejas Kamble  
> **Data notice:** Project data shown/accessed is completely legal, free and publicly accessible data and is not proprietary data. Bundled demo records are synthetic test data for development/evaluation.

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/badge/Build-Maven-blue)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

## Features
- Role-based access: Citizen/User, Complaint Officer, Administrator
- Complaint lifecycle and SLA monitoring
- Visual complaint tracking, status badges and audit history
- Citizen, officer and administrator workflows
- SQLite zero-configuration local mode with MySQL support
- Salted SHA-256 password hashing and parameterized JDBC queries
- FlatLaf modern Swing interface

## Architecture
```
SmartComplaintPortal/
├── src/com/smartcomplaint/
│   ├── model/
│   ├── dao/
│   ├── service/
│   ├── util/
│   └── ui/
├── resources/
├── docs/gallery/
├── .github/workflows/
├── pom.xml
└── README.md
```

## Quick Start
Requirements: JDK 17+ and Maven 3.9+.

```bash
mvn clean package
java -jar target/smart-complaint-portal-1.0.0.jar
```

On Windows, use `build_and_run.bat`.

## Database
SQLite is the default. Runtime configuration is kept outside the repository. For MySQL, use the application's Database Connection Settings. Never commit `db.properties`, database files, API keys, tokens or real credentials.

## Demo Accounts
| Role | Username | Password |
|---|---|---|
| Administrator | `admin` | `admin123` |
| Water Officer | `officer_water` | `officer123` |
| Electricity Officer | `officer_elec` | `officer123` |
| Roads Officer | `officer_roads` | `officer123` |
| Sanitation Officer | `officer_snt` | `officer123` |
| Citizen Demo 1 | `citizen1` | `citizen123` |
| Citizen Demo 2 | `citizen2` | `citizen123` |

Demo credentials are for local evaluation only.

## UI Gallery
See `docs/GALLERY.md` and the screenshots under `docs/gallery/`.

## Developer & Credits
**Tejas Kamble**  
Email: `tejasksocials@gmail.com`  
Website: https://tejas-personal-portfolio-dev.vercel.app/  
LinkedIn: https://www.linkedin.com/in/tejas-kamble-5342443b1/  
GitHub: https://github.com/tejasworkspacews1-ui  
Instagram: `@tejask.co.in`

## License
MIT. See `LICENSE`. Third-party notices are documented in `THIRD-PARTY-NOTICES.md`.
