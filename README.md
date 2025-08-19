# Student Management Learning System

A comprehensive Java-based student management system with examination tracking, subject enrollment, and performance reporting capabilities.

## Features

### 🎓 Student Features
- **Secure Authentication**: Login/signup with admission number and password
- **Subject Selection**: Choose from compulsory, STEM, humanities, and technical subjects
- **Exam Results**: View detailed examination results with rankings
- **Performance Reports**: Generate PDF reports of academic performance
- **Responsive UI**: Works on both desktop and mobile devices

### 👨‍🏫 Teacher Features
- **Exam Management**: Create and manage examinations
- **Student Monitoring**: View student performance across subjects
- **Class Reports**: Generate class-wide performance reports
- **Results Analysis**: Detailed analytics and rankings

### 👨‍💼 Admin Features
- **User Management**: Create and manage student/teacher accounts
- **System Overview**: Monitor system-wide statistics
- **Comprehensive Reports**: Generate system-wide reports
- **Role-based Access**: Secure access control

## Technical Specifications

### Technology Stack
- **Frontend**: JavaFX 17 with FXML
- **Backend**: Java 17 with Maven
- **Database**: MySQL (XAMPP compatible)
- **Security**: BCrypt password hashing
- **Reports**: iText PDF generation
- **Architecture**: MVC with DAO pattern

### Color Scheme
- **Primary Colors**: Grey, Black, White, Lilac, Burgundy
- **Gradients**: Applied throughout the UI for modern appearance
- **Responsive Design**: Adapts to different screen sizes

## Setup Instructions

### Prerequisites
1. **Java 17** or higher
2. **Apache Maven 3.6+**
3. **XAMPP** with MySQL running
4. **JavaFX Runtime** (included in dependencies)

### Database Setup
1. Start XAMPP and ensure MySQL is running
2. Open phpMyAdmin or MySQL command line
3. Execute the SQL script: `database_setup.sql`
4. Verify tables are created in `student_management_db` database

### Application Setup
1. Clone or download the project
2. Navigate to project directory
3. Install dependencies:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn javafx:run
   ```

### NetBeans Setup
1. Open NetBeans IDE
2. Import as Maven project
3. Set main class: `com.studentmanagement.StudentManagementApp`
4. Ensure JavaFX runtime is configured
5. Run the project

## Subject Selection Rules

### Compulsory Subjects (3 subjects - auto-enrolled)
- Mathematics
- English
- Kiswahili

### STEM Sciences (Choose up to 2)
- Physics
- Chemistry
- Biology
- Computer Studies

### Humanities (Choose up to 2)
- History and Government
- Geography
- Business Studies

### Technical Subjects (Choose up to 2)
- Drawing and Design
- Art and Design
- Music

**Total**: Students must enroll in 7-8 subjects total (3 compulsory + 4-5 electives)

## Database Schema

### Key Tables
- `users`: Student, teacher, and admin accounts
- `subjects`: Academic subjects with categories
- `student_subjects`: Subject enrollment tracking
- `examinations`: Exam definitions
- `exam_results`: Student performance data
- `student_profiles`: Additional student information

## Security Features

### Authentication
- BCrypt password hashing with salt
- Session management
- Role-based access control
- Input validation and sanitization

### Data Protection
- SQL injection prevention
- XSS protection
- Secure password policies
- Connection pooling with timeout

## Performance Features

### Multi-threading
- Asynchronous database operations
- Background report generation
- Non-blocking UI operations
- Connection pooling

### Exception Handling
- Comprehensive error handling
- Graceful degradation
- User-friendly error messages
- Detailed logging

## Default Credentials

### Admin Account
- **Admission Number**: ADMIN001
- **Password**: admin123 (change immediately)

## File Structure

```
src/
├── main/
│   ├── java/com/studentmanagement/
│   │   ├── controller/          # UI Controllers
│   │   ├── dao/                 # Data Access Objects
│   │   ├── exception/           # Custom Exceptions
│   │   ├── model/               # Data Models
│   │   ├── service/             # Business Logic
│   │   ├── util/                # Utility Classes
│   │   └── StudentManagementApp.java
│   └── resources/
│       ├── css/                 # Stylesheets
│       ├── fxml/                # UI Layouts
│       └── images/              # Application Images
└── test/                        # Unit Tests
```

## API Documentation

### Key Services
- `AuthenticationService`: User authentication and registration
- `StudentService`: Student-specific operations
- `ReportService`: PDF report generation
- `DatabaseConnection`: Connection pool management

### Key Models
- `User`: Student/teacher/admin accounts
- `Subject`: Academic subjects
- `Examination`: Exam definitions
- `ExamResult`: Student performance data

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure XAMPP MySQL is running
   - Check database credentials in `DatabaseConnection.java`
   - Verify `student_management_db` database exists

2. **JavaFX Runtime Issues**
   - Ensure JavaFX is properly installed
   - Check Maven JavaFX plugin configuration
   - Verify Java 17 compatibility

3. **UI Not Loading**
   - Check FXML file paths
   - Verify CSS file references
   - Check console for JavaFX errors

4. **Report Generation Failed**
   - Ensure Downloads folder is accessible
   - Check iText PDF dependencies
   - Verify file permissions

### Performance Tuning
- Adjust connection pool size in `DatabaseConnection.java`
- Monitor memory usage for large datasets
- Optimize database queries for better performance

## Contributing

1. Follow Java coding conventions
2. Add comprehensive logging
3. Include unit tests for new features
4. Update documentation for changes
5. Test on both desktop and mobile layouts

## License

This project is developed for educational purposes. All rights reserved.

## Support

For technical support or feature requests, please contact the development team.

---

**Student Management Learning System v1.0.0**  
*Secure • Reliable • User-Friendly*