package com.studentmanagement;

import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.dao.SubjectDAO;
import com.studentmanagement.model.User;
import com.studentmanagement.model.Subject;
import com.studentmanagement.service.AuthenticationService;
import com.studentmanagement.util.DatabaseConnection;
import com.studentmanagement.util.ValidationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System integration tests for the Student Management System
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SystemTest {
    
    private UserDAO userDAO;
    private SubjectDAO subjectDAO;
    private AuthenticationService authService;

    @BeforeAll
    void setUp() {
        userDAO = new UserDAO();
        subjectDAO = new SubjectDAO();
        authService = new AuthenticationService();
    }

    @Test
    void testDatabaseConnection() {
        assertTrue(DatabaseConnection.getInstance().testConnection(), 
                  "Database connection should be successful");
    }

    @Test
    void testSubjectsExist() throws SQLException {
        List<Subject> subjects = subjectDAO.findAll();
        assertFalse(subjects.isEmpty(), "Subjects should exist in database");
        
        // Verify compulsory subjects exist
        List<Subject> compulsory = subjectDAO.findByCategory(Subject.Category.COMPULSORY);
        assertEquals(3, compulsory.size(), "Should have 3 compulsory subjects");
        
        // Verify STEM subjects exist
        List<Subject> stem = subjectDAO.findByCategory(Subject.Category.STEM_SCIENCE);
        assertEquals(4, stem.size(), "Should have 4 STEM science subjects");
    }

    @Test
    void testUserRegistration() {
        CompletableFuture<User> future = authService.registerStudent(
            "TEST001", 
            "test@example.com", 
            "Test Student", 
            "password123"
        );
        
        assertDoesNotThrow(() -> {
            User user = future.get();
            assertNotNull(user);
            assertEquals("TEST001", user.getAdmissionNumber());
            assertEquals(User.Role.STUDENT, user.getRole());
        });
    }

    @Test
    void testUserAuthentication() throws SQLException {
        // Create test user first
        User testUser = new User("AUTH001", "auth@test.com", "Auth Test", 
                                org.mindrot.jbcrypt.BCrypt.hashpw("testpass123", org.mindrot.jbcrypt.BCrypt.gensalt()));
        userDAO.save(testUser);
        
        CompletableFuture<User> authFuture = authService.authenticateUser("AUTH001", "testpass123");
        
        assertDoesNotThrow(() -> {
            User authenticatedUser = authFuture.get();
            assertNotNull(authenticatedUser);
            assertEquals("AUTH001", authenticatedUser.getAdmissionNumber());
        });
    }

    @Test
    void testValidationUtils() {
        // Test email validation
        assertTrue(ValidationUtils.isValidEmail("student@example.com"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        
        // Test admission number validation
        assertTrue(ValidationUtils.isValidAdmissionNumber("STD001234"));
        assertFalse(ValidationUtils.isValidAdmissionNumber("123"));
        
        // Test password validation
        assertTrue(ValidationUtils.isValidPassword("password123"));
        assertFalse(ValidationUtils.isValidPassword("weak"));
        
        // Test name validation
        assertTrue(ValidationUtils.isValidName("John Doe"));
        assertFalse(ValidationUtils.isValidName("J"));
    }

    @Test
    void testSubjectEnrollment() throws SQLException {
        // Find a test student
        Optional<User> studentOpt = userDAO.findByAdmissionNumber("TEST001");
        if (studentOpt.isPresent()) {
            User student = studentOpt.get();
            
            // Test subject enrollment
            List<Subject> subjects = subjectDAO.findAll();
            if (!subjects.isEmpty()) {
                Subject firstSubject = subjects.get(0);
                boolean enrolled = subjectDAO.enrollStudentInSubject(student.getId(), firstSubject.getId());
                assertTrue(enrolled, "Student should be able to enroll in subject");
                
                // Verify enrollment
                List<Subject> studentSubjects = subjectDAO.findStudentSubjects(student.getId());
                assertTrue(studentSubjects.stream()
                          .anyMatch(s -> s.getId() == firstSubject.getId()),
                          "Subject should appear in student's enrolled subjects");
            }
        }
    }

    @Test
    void testPasswordHashing() {
        String password = "testpassword123";
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        
        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw(password, hash));
        assertFalse(org.mindrot.jbcrypt.BCrypt.checkpw("wrongpassword", hash));
    }

    @Test
    void testConnectionPooling() {
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        
        // Test multiple connections
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                try (var connection = dbConn.getConnection()) {
                    assertTrue(connection.isValid(5));
                }
            }
        });
    }
}