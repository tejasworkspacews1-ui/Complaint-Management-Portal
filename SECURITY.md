# Security Policy

## Scope
This repository is an educational/open-source Java desktop application. Do not use it as a production civic platform without a production security review, hardened database configuration, access controls, secret management, logging policy, backup strategy, and deployment controls.

## Reporting a Vulnerability
Please use GitHub Security Advisories when available. Avoid posting credentials, tokens, or secrets in public issues.

## Local Credentials
Database passwords are stored only in local application configuration and are excluded from Git via `.gitignore`. Never commit `db.properties`, database files, API keys, tokens, or real user credentials.
