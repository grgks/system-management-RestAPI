# Security Policy

## About This Project

This is a portfolio/educational project built as part of my capstone at Coding Factory AUEB. It is provided "as-is" for demonstration purposes only. **No official support or security updates are provided.**

## Threat Modeling & Risk Analysis

This document outlines identified assets, potential threats, associated risks, and mitigation strategies for this application.

---

### 1. Asset Inventory
### 1. Asset Inventory

| ID | Asset Name | Asset Type | Description | Owner | Criticality |
|----|------------|------------|-------------|--------|-------------|
| A1 | User Credentials Database | Data | Usernames, BCrypt password hashes (11 rounds), emails, roles | Application | HIGH |
| A2 | Client Personal Information | Data | Names, phones, VAT numbers, addresses, date of birth | Application | HIGH |
| A3 | Appointment Records | Data | Client appointments, scheduling data, notes | Application | MEDIUM |
| A4 | JWT Secret Key | Configuration | Secret key for token signing (`jwt.secret` environment variable) | DevOps | HIGH |
| A5 | REST API Endpoints | System | Authentication, user management, appointment CRUD operations | Application | HIGH |
| A6 | Swagger API Documentation | System | Complete API schema, endpoints, parameters | Application | MEDIUM |
| A7 | MySQL Database Server | System | Database instance with all application data | Infrastructure | HIGH |
| A8 | Spring Boot Application | System | Backend server handling business logic | Infrastructure | HIGH |                            |                                                       |                                                         |             |                               |

---

### 2. Threat Register

| ID | Related Asset (ID) | Threat Name                        | Threat Description                                                      | Attack Vector                     | Likelihood (Low/Medium/High) | Impact (Low/Medium/High) | Risk Level | Existing Mitigation                                                             | Additional Actions Required                                                                                |
|----|--------------------|------------------------------------|-------------------------------------------------------------------------|-----------------------------------|------------------------------|--------------------------|------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| T1 | A1 ,  A5           | SQL Injection in Authentication    | Attacker bypasses login using SQL injection in username/password fields | API/ authenticate endpoint        | Low                          | High                     | MEDIUM     | VERIFIED: <br/>Hibernate ORM with parameterized queries(EFFECTIVE)              | PASSED - No additional actions required                                                                    |
| T2 | A4 ,  A5           | JWT Manipulation                   | Attacker modifies JWT claims (role escalation CLIENT<br/>-> ADMIN)      | Token tampering, "none" algorithm | Low                          | High                     | MEDIUM     | VERIFIED: <br/>JWT library (jjwt) rejects unsigned tokens (EFFECTIVE)           | PASSED - No additional actions required                                                                    |
| T3 | A5                 | Authorization Bypass               | Attacker accesses admin endpoints with CLIENT role token                | API authorization checks          | Low                          | High                     | MEDIUM     | VERIFIED:<br/>Spring Security context checks + ownership validation (EFFECTIVE) | PASSED - No additional actions required                                                                    |
| T4 | A1                 | Brute Force Attack                 | Attacker attempts multiple login combinations to guess credentials      | API `/authenticate` endpoint      | High                         | Medium                   | HIgh       | BCrypt 11 rounds<br/> (slow hashing), Security audit logging (DETECTIVE only)   | FAILED - Implement rate limiting (Bucket4j: 5 attempts/min per IP) + BadCredentialsException error handler |
| T5 | A6                 | Information Disclosure via Swagger | Attacker learns complete API structure                                  | Public Swagger UI                 | High                         | Low                      | MEDIUM     | None - <br/>publicly accessible                                                 | ACCEPTED RISK - Appropriate for portfolio/demo project. Restrict to admins in production                   |

---

### 3. Risk Evaluation Criteria

Risk Level is determined based on:

- **Likelihood**: Probability of occurrence
- **Impact**: Severity of consequences if exploited

General Guidance:

- Low: Limited impact or unlikely occurrence
- Medium: Moderate impact or realistic possibility
- High: Severe impact or high probability

---

### 4. Scope & Assumptions

- This analysis focuses on application-level risks.
- Infrastructure and cloud-level risks may not be fully covered.
- This is an educational portfolio project and not production-hardened.

---

### 5. Review Log

| Date | Reviewed By | Notes |
|------|-------------|-------|
|      |             |       |



## Security Measures Implemented

This project demonstrates:
- JWT-based authentication
- Role-based authorization
- Automated security scanning (Trivy) in CI/CD pipeline
- Environment variable management for sensitive data
- HTTPS for production deployment

## Reporting Vulnerabilities

While this is an educational project without active maintenance, responsible disclosure is appreciated:

1. **Do NOT** create public GitHub issues for security concerns
2. Send me a direct email 
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact

**Note:** As this is a portfolio project, I may not be able to address all reported issues promptly.

## Disclaimer

This project is for educational and demonstration purposes only. It should not be used in production environments without thorough security review and hardening.
