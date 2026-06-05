# Build Configuration - Enterprise Edition

## Project Structure
- Source: `src/com/sgs/**/*.java`
- Output: `WEB-INF/classes/com/sgs/**/*.class`
- Web Root: Root directory (index.html, login.html)
- Dependencies: None (uses Jakarta EE APIs)

## Compilation Configuration

### Compiler Settings
- **Target Version:** Java 11 (Jakarta EE 10 compatible)
- **Encoding:** UTF-8
- **Warnings:** All
- **Debug Info:** Included

### Required JARs
- `jakarta.servlet-api-6.0.0.jar` (provided by Tomcat)
- `jakarta.logging-api-6.0.0.jar` (provided by Tomcat)

## Build Properties

```properties
# Application Metadata
app.name=Student Grading System
app.version=2.0.0
app.environment=production

# Java Compilation
java.version=11
source.encoding=UTF-8

# Logging
log.level=INFO
log.dir=logs
log.file=sgs-application.log
log.size.max=5242880
log.backups=3

# Security
session.timeout=1800
session.secure=true
session.httponly=true
```

## Deployment

### WAR Package Contents
```
student-grading-system.war
├── WEB-INF/
│   ├── web.xml
│   ├── classes/com/sgs/**/*.class
│   └── lib/ (if external JARs needed)
├── index.html
├── login.html
├── css/
│   └── style.css
├── js/
│   └── app.js
└── error*.jsp
```

### Installation Steps
1. Copy `student-grading-system.war` to `$CATALINA_HOME/webapps/`
2. Start Tomcat: `catalina.sh run`
3. Access at: `http://localhost:8080/student-grading-system/`

## Environment Variables

```bash
# Unix/Linux
export JAVA_OPTS="-Xmx512m -Xms256m -Dapp.environment=production"
export CATALINA_OPTS="-Djava.util.logging.config.file=logging.properties"

# Windows
set JAVA_OPTS=-Xmx512m -Xms256m -Dapp.environment=production
set CATALINA_OPTS=-Djava.util.logging.config.file=logging.properties
```

## Continuous Integration

### CI/CD Pipeline
1. **Compile** - `javac src/**/*.java -d WEB-INF/classes/`
2. **Unit Tests** - Run JUnit tests
3. **Code Analysis** - SonarQube scan
4. **Security Scan** - OWASP dependency check
5. **Build WAR** - Create deployment package
6. **Deploy** - Copy to test/prod environment

### Quality Gates
- Code coverage: > 80%
- Critical issues: 0
- Security vulnerabilities: 0

## Monitoring

### Health Checks
- Application initialization: Check `logs/sgs-application.log` for "APPLICATION READY"
- Endpoint availability: `GET /login` should return 200
- Database connectivity: Service initialization completes

### Performance Metrics
- Response time: < 500ms
- Throughput: > 100 req/sec
- Memory usage: < 512MB

