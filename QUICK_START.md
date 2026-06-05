# Quick Start Guide - Enterprise Edition v2.0

## 🚀 Getting Started in 5 Minutes

### Prerequisites
- **Java 11+** installed
- **Tomcat 10+** available
- **Windows PowerShell 5.0+** (for scripts)

### Step 1: Build the Application
```powershell
cd C:\Users\avish\OneDrive\New folder
.\build.ps1
```

**Expected Output:**
```
============================================================
  BUILD SUCCESSFUL - Enterprise Edition v2.0
============================================================
Generated 15 class files
Status: Ready for deployment
```

### Step 2: Start Tomcat Server
```powershell
.\run.ps1
```

**Expected Output:**
```
[INFO] === APPLICATION STARTUP ===
[INFO] StudentService initialized with 5 records
[INFO] === APPLICATION READY ===
```

### Step 3: Access the Application
```
URL: http://localhost:8080/
Login: admin
Password: admin123
```

---

## 📱 Key Features

### ✅ What's Included
- **Secure Authentication** - Session-based login
- **Student Management** - Add, view, delete students
- **Grade Analytics** - Class averages, grade distribution
- **Comprehensive Logging** - All events logged to `logs/sgs-application.log`
- **Error Handling** - Meaningful error messages with codes
- **Input Validation** - Complete field validation
- **Audit Trail** - Timestamps on all changes

### 🔒 Security Features
- Input sanitization (XSS prevention)
- Session timeout (30 minutes)
- HTTP-only cookies
- Secure authentication
- Authorization checks

---

## 📊 Default Data

| ID | Student Name | Grade | Average |
|----|---|---|---|
| 1000 | Alice Johnson | A | 91.25 |
| 1001 | Bob Smith | C | 68.75 |
| 1002 | Charlie Brown | D | 58.75 |
| 1003 | Diana Prince | D | 41.25 |
| 1004 | Ethan Hunt | B | 81.25 |

---

## 🔧 Configuration

### Session Timeout
**File:** `src/com/sgs/config/ApplicationConfig.java`
```java
public static final int SESSION_TIMEOUT_MINUTES = 30;
```

### Score Thresholds
```java
GRADE_A_THRESHOLD = 90.0  // A: 90-100
GRADE_B_THRESHOLD = 75.0  // B: 75-89
GRADE_C_THRESHOLD = 60.0  // C: 60-74
GRADE_D_THRESHOLD = 50.0  // D: 50-59
                          // F: 0-49
```

### Validation Rules
- **Student Name:** 2-255 characters, alphanumeric + spaces
- **Subject Score:** 0-100 (decimal allowed)
- **Username:** 3-20 characters, alphanumeric + dash/underscore

---

## 📝 API Endpoints

### Authentication
```
POST /login
{
  "username": "admin",
  "password": "admin123"
}

GET /login?action=logout
GET /login  (check session status)
```

### Student Operations
```
GET /students           → List all students (requires auth)
POST /student           → Create student
GET /student?id=1000    → Get student details
DELETE /student?id=1000 → Delete student
GET /dashboard          → Get analytics
```

### Response Format
```json
{
  "status": "success",
  "code": "SCS_000",
  "message": "Operation successful",
  "data": {...},
  "timestamp": "2024-01-01 10:30:45"
}
```

---

## 📊 Error Codes

| Code | Meaning | HTTP Status |
|------|---------|-------------|
| `SCS_000` | Success | 200 |
| `VAL_001` | Validation error | 400 |
| `AUTH_001` | Authentication failed | 401 |
| `BIZ_001` | Business logic error | 400 |
| `ERR_001` | System error | 500 |

---

## 📋 Logging

### Log File Location
```
logs/sgs-application.log
```

### Log Format
```
2024-01-01 10:30:45.123 [SEVERE] StudentGradingSystem - [ServiceName] Log message
```

### Log Levels
- `SEVERE` - Error conditions
- `WARNING` - Warning messages
- `INFO` - Informational messages
- `FINE` - Debug level

### Example Logs
```
[INFO] [StudentService] Student added successfully - ID: 1005, Name: John Doe, Grade: A
[WARN] [AuthFilter] Unauthorized access attempt - Path: /students, IP: 192.168.1.1
[ERROR] [StudentService] Failed to add student: Invalid score range
```

---

## 🛠️ Troubleshooting

### Build Fails
**Error:** `javac not found`
```
Solution: Add JDK bin directory to system PATH
  Windows: setx PATH "%PATH%;C:\Program Files\Java\jdk-11\bin"
```

### Cannot Access Application
**Error:** `Connection refused`
```
Solution: 
  1. Verify Tomcat is running: run.ps1 output
  2. Check port 8080 is not in use
  3. Verify firewall settings
```

### Login Fails
**Error:** `Invalid username or password`
```
Solution:
  1. Default credentials: admin / admin123
  2. Check log file: logs/sgs-application.log
  3. Verify session is active
```

### No Logs Generated
**Error:** `logs/ directory empty`
```
Solution:
  1. Run application: .\run.ps1
  2. Perform actions (login, view students)
  3. Logs generated on first request
```

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| `ENTERPRISE_README.md` | Complete architecture & features |
| `BUILD_CONFIG.md` | Build & deployment guide |
| `UPGRADE_SUMMARY.md` | Changes from v1.0 to v2.0 |
| `QUICK_START.md` | This file |

---

## 🎯 Common Tasks

### Add a New Student
```javascript
// POST /student
{
  "name": "Jane Smith",
  "subject1": 92,
  "subject2": 88,
  "subject3": 95,
  "subject4": 90
}
```

### View All Students
```
GET /students
```

### Get Analytics
```
GET /dashboard
```

### Delete Student
```
DELETE /student?id=1005
```

### Logout
```
GET /login?action=logout
```

---

## ⚙️ System Requirements

### Minimum
- JDK 11.0+
- Tomcat 10.0+
- 256 MB RAM
- 100 MB Disk Space

### Recommended
- JDK 17 or newer
- Tomcat 10.1+
- 512 MB+ RAM
- SSD storage

---

## 🔐 Security Tips

1. **Change default credentials** in production
2. **Enable HTTPS** for production deployments
3. **Review logs** regularly for security events
4. **Backup data** periodically
5. **Keep JDK updated** with latest patches
6. **Restrict file permissions** on logs directory
7. **Monitor session timeouts** and activity

---

## 📞 Support Resources

- **Logs:** `logs/sgs-application.log`
- **Configuration:** `src/com/sgs/config/ApplicationConfig.java`
- **Help:** Check error codes and logs for detailed information
- **Version:** 2.0.0 (Enterprise Edition)

---

## ✅ Verification Checklist

After startup, verify:
- [ ] Application starts without errors
- [ ] Log file created: `logs/sgs-application.log`
- [ ] Can login with admin/admin123
- [ ] Dashboard displays student list
- [ ] Default 5 students appear
- [ ] Can view student details
- [ ] Error handling works properly
- [ ] Session timeout works (30 minutes)
- [ ] Logout functionality works

---

## 🎉 You're Ready!

Your enterprise-grade Student Grading System v2.0 is now running.

**Next Steps:**
1. Explore the dashboard
2. Review `ENTERPRISE_README.md` for detailed information
3. Check logs for monitoring
4. Configure for production use
5. Deploy to your environment

**Happy Learning! 📚**

