# Student Management Learning System - Deployment Guide

## Quick Start

### 1. Prerequisites Setup
- **Java 17+**: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or use OpenJDK
- **Apache Maven 3.6+**: Download from [Maven Website](https://maven.apache.org/download.cgi)
- **XAMPP**: Download from [Apache Friends](https://www.apachefriends.org/download.html)

### 2. Database Setup
1. **Start XAMPP**:
   - Open XAMPP Control Panel
   - Start **Apache** and **MySQL** services
   - Verify MySQL is running on port 3306

2. **Create Database**:
   - Open phpMyAdmin: `http://localhost/phpmyadmin`
   - Click "Import" tab
   - Select `database_setup.sql` file
   - Click "Go" to execute the script
   - Verify `student_management_db` database is created

### 3. Application Setup

#### Option A: Using Scripts (Recommended)
```bash
# Linux/Mac
./build-and-run.sh

# Windows
run.bat
```

#### Option B: Manual Setup
```bash
# Install dependencies
mvn clean install

# Run application
mvn javafx:run
```

#### Option C: NetBeans IDE
1. Open NetBeans IDE
2. File → Open Project
3. Select the project folder
4. Right-click project → Run

## Default Login Credentials

### Administrator
- **Admission Number**: `ADMIN001`
- **Password**: `admin123`

### Demo Teacher (if created)
- **Admission Number**: `TCH001`
- **Password**: `teacher123`

### Demo Student (if created)
- **Admission Number**: `STD001`
- **Password**: `student123`

## Features Overview

### 🎓 Student Portal
- **Secure Login**: Admission number + password authentication
- **Subject Selection**: Choose 7-8 subjects following school rules
- **Exam Results**: View performance with rankings
- **PDF Reports**: Generate comprehensive performance reports
- **Responsive Design**: Works on desktop and mobile

### 👨‍🏫 Teacher Portal
- **Exam Management**: Create and manage examinations
- **Student Monitoring**: Track student performance
- **Class Reports**: Generate class-wide analytics
- **Results Entry**: Input and manage exam results

### 👨‍💼 Admin Portal
- **User Management**: Create student/teacher accounts
- **System Overview**: Monitor system statistics
- **Role Management**: Assign and manage user roles
- **System Reports**: Generate comprehensive system reports

## Subject Selection Rules

### Compulsory (3 subjects - automatically enrolled)
- Mathematics
- English  
- Kiswahili

### STEM Sciences (choose up to 2)
- Physics
- Chemistry
- Biology
- Computer Studies

### Humanities (choose up to 2)
- History and Government
- Geography
- Business Studies

### Technical (choose up to 2)
- Drawing and Design
- Art and Design
- Music

**Total**: 7-8 subjects (3 compulsory + 4-5 electives)

## Troubleshooting

### Database Connection Issues
```
Error: "Unable to connect to database"
Solution:
1. Start XAMPP MySQL service
2. Check if port 3306 is available
3. Verify database credentials in DatabaseConnection.java
4. Run database_setup.sql script
```

### JavaFX Runtime Issues
```
Error: "JavaFX runtime components missing"
Solution:
1. Ensure Java 17+ is installed
2. Verify JavaFX dependencies in pom.xml
3. Use: mvn javafx:run instead of java -jar
```

### Memory Issues
```
Error: "OutOfMemoryError"
Solution:
1. Increase JVM heap size: -Xmx2g
2. Close other applications
3. Restart the application
```

### Port Conflicts
```
Error: "Port already in use"
Solution:
1. Stop other MySQL instances
2. Change MySQL port in XAMPP
3. Update database URL in configuration
```

## Performance Optimization

### Database
- Connection pooling (5-20 connections)
- Query optimization with indexes
- Batch operations for bulk data

### UI
- Asynchronous data loading
- Lazy loading for large datasets
- Responsive design principles

### Reports
- Background PDF generation
- Streaming for large reports
- Caching for frequently accessed data

## Security Features

### Authentication
- BCrypt password hashing (12 rounds)
- Session management
- Role-based access control

### Data Protection
- SQL injection prevention
- Input validation and sanitization
- Secure password policies

### Network Security
- Local database connections only
- No external API dependencies
- Secure file operations

## Backup and Maintenance

### Database Backup
```sql
-- Export database
mysqldump -u root student_management_db > backup.sql

-- Import database
mysql -u root student_management_db < backup.sql
```

### Log Management
- Logs stored in `logs/` directory
- Automatic rotation (10MB files, 30 days retention)
- Error logs separated for debugging

### Updates
1. Backup current database
2. Stop application
3. Replace application files
4. Run database migration scripts if needed
5. Restart application

## Production Deployment

### System Requirements
- **OS**: Windows 10+, macOS 10.14+, Ubuntu 18.04+
- **RAM**: Minimum 4GB, Recommended 8GB
- **Storage**: 500MB for application + database space
- **Network**: Local network access for database

### Configuration
1. Update database credentials in `DatabaseConnection.java`
2. Configure logging levels in `logback.xml`
3. Set appropriate file permissions
4. Configure firewall if needed

### Monitoring
- Check application logs regularly
- Monitor database performance
- Track user activity
- Generate periodic reports

## Support and Maintenance

### Log Files
- Application logs: `logs/student-management.log`
- Error logs: `logs/student-management-errors.log`
- Database logs: Check XAMPP logs

### Common Tasks
- **Add new user**: Use admin dashboard
- **Reset password**: Contact administrator
- **Backup data**: Export database regularly
- **Update subjects**: Modify database directly

### Contact Information
For technical support, please refer to the development team or check the project documentation.

---

**Student Management Learning System v1.0.0**  
*Secure • Reliable • User-Friendly*