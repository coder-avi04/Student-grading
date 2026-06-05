# Student Grading System

A small local web app for managing student records, grades, and dashboard metrics.

## Run the app

### Option 1: Local fallback server
From the project root:

```powershell
node server.js
```

The app will start on:

- http://127.0.0.1:3000/

### Option 2: Tomcat runner
If you want to use the included Tomcat setup:

```powershell
./run.ps1
```

Then open:

- http://localhost:8080/

## Notes
- The fallback server uses port 3000 if the default port is unavailable.
- Login credentials:
  - Username: admin
  - Password: admin123
