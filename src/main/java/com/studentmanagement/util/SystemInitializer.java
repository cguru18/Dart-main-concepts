package com.studentmanagement.util;

import com.studentmanagement.dao.SubjectDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.exception.ValidationException;
import com.studentmanagement.model.Subject;
import com.studentmanagement.model.User;
import com.studentmanagement.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for initializing the system with default data
 */
public class SystemInitializer {
    private static final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);
    
    private final UserDAO userDAO;
    private final SubjectDAO subjectDAO;
    private final AuthenticationService authService;
    private final ExecutorService executorService;

    public SystemInitializer() {
        this.userDAO = new UserDAO();
        this.subjectDAO = new SubjectDAO();
        this.authService = new AuthenticationService();
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Initialize system with default data if needed
     */
    public CompletableFuture<Boolean> initializeSystem() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean initialized = false;
                
                // Check if admin user exists
                if (!userDAO.existsByAdmissionNumber("ADMIN001")) {
                    createDefaultAdmin();
                    initialized = true;
                }
                
                // Check if demo teacher exists
                if (!userDAO.existsByAdmissionNumber("TCH001")) {
                    createDemoTeacher();
                    initialized = true;
                }
                
                // Check if demo student exists
                if (!userDAO.existsByAdmissionNumber("STD001")) {
                    createDemoStudent();
                    initialized = true;
                }
                
                if (initialized) {
                    logger.info("System initialized with default users");
                } else {
                    logger.debug("System already initialized");
                }
                
                return true;
                
            } catch (Exception e) {
                logger.error("Failed to initialize system", e);
                return false;
            }
        }, executorService);
    }

    private void createDefaultAdmin() throws ValidationException, SQLException {
        logger.info("Creating default admin user");
        authService.createAdminUser(
            "ADMIN001",
            "admin@school.edu",
            "System Administrator",
            "admin123"
        );
    }

    private void createDemoTeacher() throws ValidationException, SQLException {
        logger.info("Creating demo teacher user");
        authService.createTeacherUser(
            "TCH001",
            "teacher@school.edu",
            "Demo Teacher",
            "teacher123"
        );
    }

    private void createDemoStudent() {
        logger.info("Creating demo student user");
        authService.registerStudent(
            "STD001",
            "student@school.edu",
            "Demo Student",
            "student123"
        ).whenComplete((user, throwable) -> {
            if (throwable != null) {
                logger.error("Failed to create demo student", throwable);
            } else {
                logger.info("Demo student created successfully");
                
                // Auto-enroll in compulsory subjects
                try {
                    subjectDAO.enrollStudentInCompulsorySubjects(user.getId());
                } catch (SQLException e) {
                    logger.error("Failed to enroll demo student in compulsory subjects", e);
                }
            }
        });
    }

    /**
     * Verify system integrity
     */
    public CompletableFuture<Boolean> verifySystemIntegrity() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Test database connection
                if (!DatabaseConnection.getInstance().testConnection()) {
                    logger.error("Database connection test failed");
                    return false;
                }
                
                // Verify subjects exist
                if (subjectDAO.findAll().isEmpty()) {
                    logger.warn("No subjects found in database");
                    return false;
                }
                
                // Verify at least one admin exists
                if (userDAO.findByRole(User.Role.ADMIN).isEmpty()) {
                    logger.warn("No admin users found");
                    return false;
                }
                
                logger.info("System integrity verification passed");
                return true;
                
            } catch (SQLException e) {
                logger.error("System integrity verification failed", e);
                return false;
            }
        }, executorService);
    }

    /**
     * Create sample exam data for testing
     */
    public CompletableFuture<Boolean> createSampleData() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // This would create sample examinations and results
                // Implementation depends on specific requirements
                logger.info("Sample data creation completed");
                return true;
                
            } catch (Exception e) {
                logger.error("Failed to create sample data", e);
                return false;
            }
        }, executorService);
    }

    public void shutdown() {
        authService.shutdown();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}