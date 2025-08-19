package com.studentmanagement.service;

import com.studentmanagement.dao.SubjectDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.dao.ExamResultDAO;
import com.studentmanagement.exception.ValidationException;
import com.studentmanagement.model.Subject;
import com.studentmanagement.model.User;
import com.studentmanagement.model.ExamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service for student-related operations
 */
public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    
    private final UserDAO userDAO;
    private final SubjectDAO subjectDAO;
    private final ExamResultDAO examResultDAO;
    private final ExecutorService executorService;

    public StudentService() {
        this.userDAO = new UserDAO();
        this.subjectDAO = new SubjectDAO();
        this.examResultDAO = new ExamResultDAO();
        this.executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * Enroll student in subjects with validation
     */
    public CompletableFuture<Boolean> enrollStudentInSubjects(int studentId, List<Integer> subjectIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get current enrolled subjects
                List<Subject> currentSubjects = subjectDAO.findStudentSubjects(studentId);
                
                // Get subjects to enroll
                List<Subject> newSubjects = subjectIds.stream()
                    .map(id -> {
                        try {
                            return subjectDAO.findById(id).orElse(null);
                        } catch (SQLException e) {
                            logger.error("Error finding subject by ID: {}", id, e);
                            return null;
                        }
                    })
                    .filter(subject -> subject != null)
                    .collect(Collectors.toList());

                // Validate subject combination
                validateSubjectCombination(currentSubjects, newSubjects);
                
                // Enroll in new subjects
                boolean allEnrolled = true;
                for (Integer subjectId : subjectIds) {
                    boolean enrolled = subjectDAO.enrollStudentInSubject(studentId, subjectId);
                    if (!enrolled) {
                        allEnrolled = false;
                        logger.warn("Failed to enroll student {} in subject {}", studentId, subjectId);
                    }
                }
                
                // Auto-enroll in compulsory subjects if not already enrolled
                subjectDAO.enrollStudentInCompulsorySubjects(studentId);
                
                logger.info("Student {} enrollment completed. Success: {}", studentId, allEnrolled);
                return allEnrolled;
                
            } catch (SQLException e) {
                logger.error("Database error during subject enrollment", e);
                throw new RuntimeException("Subject enrollment failed", e);
            } catch (ValidationException e) {
                logger.warn("Validation error during subject enrollment: {}", e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Get student's enrolled subjects
     */
    public CompletableFuture<List<Subject>> getStudentSubjects(int studentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return subjectDAO.findStudentSubjects(studentId);
            } catch (SQLException e) {
                logger.error("Error retrieving student subjects for ID: {}", studentId, e);
                throw new RuntimeException("Failed to retrieve student subjects", e);
            }
        }, executorService);
    }

    /**
     * Get student's exam results
     */
    public CompletableFuture<List<ExamResult>> getStudentExamResults(int studentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return examResultDAO.findByStudentId(studentId);
            } catch (SQLException e) {
                logger.error("Error retrieving exam results for student ID: {}", studentId, e);
                throw new RuntimeException("Failed to retrieve exam results", e);
            }
        }, executorService);
    }

    /**
     * Get student's overall performance summary
     */
    public CompletableFuture<StudentPerformanceSummary> getStudentPerformanceSummary(int studentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ExamResult> results = examResultDAO.findByStudentId(studentId);
                BigDecimal averageMarks = examResultDAO.getStudentAverageMarks(studentId);
                
                return new StudentPerformanceSummary(studentId, results, averageMarks);
                
            } catch (SQLException e) {
                logger.error("Error generating performance summary for student ID: {}", studentId, e);
                throw new RuntimeException("Failed to generate performance summary", e);
            }
        }, executorService);
    }

    /**
     * Validate subject combination according to rules
     */
    private void validateSubjectCombination(List<Subject> currentSubjects, List<Subject> newSubjects) 
            throws ValidationException {
        
        // Count subjects by category (including current + new)
        Map<Subject.Category, Long> currentCounts = currentSubjects.stream()
            .collect(Collectors.groupingBy(Subject::getCategory, Collectors.counting()));
        
        Map<Subject.Category, Long> newCounts = newSubjects.stream()
            .collect(Collectors.groupingBy(Subject::getCategory, Collectors.counting()));
        
        // Calculate total counts
        long stemCount = currentCounts.getOrDefault(Subject.Category.STEM_SCIENCE, 0L) + 
                        newCounts.getOrDefault(Subject.Category.STEM_SCIENCE, 0L);
        long humanitiesCount = currentCounts.getOrDefault(Subject.Category.HUMANITIES, 0L) + 
                              newCounts.getOrDefault(Subject.Category.HUMANITIES, 0L);
        long technicalCount = currentCounts.getOrDefault(Subject.Category.TECHNICAL, 0L) + 
                             newCounts.getOrDefault(Subject.Category.TECHNICAL, 0L);
        
        // Validate category limits
        if (stemCount > 2) {
            throw new ValidationException("Cannot enroll in more than 2 STEM science subjects");
        }
        
        if (humanitiesCount > 2) {
            throw new ValidationException("Cannot enroll in more than 2 humanities subjects");
        }
        
        if (technicalCount > 2) {
            throw new ValidationException("Cannot enroll in more than 2 technical subjects");
        }
        
        // Calculate total subjects (compulsory are always 3)
        long totalSubjects = 3 + stemCount + humanitiesCount + technicalCount;
        
        if (totalSubjects < 7) {
            throw new ValidationException("Must enroll in at least 7 subjects total (minimum 4 electives)");
        }
        
        if (totalSubjects > 8) {
            throw new ValidationException("Cannot enroll in more than 8 subjects total (maximum 5 electives)");
        }
    }

    /**
     * Get available subjects for a student based on current enrollment
     */
    public CompletableFuture<List<Subject>> getAvailableSubjectsForStudent(int studentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Subject> allSubjects = subjectDAO.findAll();
                List<Subject> enrolledSubjects = subjectDAO.findStudentSubjects(studentId);
                
                // Remove already enrolled subjects
                List<Integer> enrolledIds = enrolledSubjects.stream()
                    .map(Subject::getId)
                    .collect(Collectors.toList());
                
                return allSubjects.stream()
                    .filter(subject -> !enrolledIds.contains(subject.getId()))
                    .filter(subject -> canEnrollInSubject(studentId, subject, enrolledSubjects))
                    .collect(Collectors.toList());
                
            } catch (SQLException e) {
                logger.error("Error retrieving available subjects for student ID: {}", studentId, e);
                throw new RuntimeException("Failed to retrieve available subjects", e);
            }
        }, executorService);
    }

    private boolean canEnrollInSubject(int studentId, Subject subject, List<Subject> enrolledSubjects) {
        try {
            if (subject.isCompulsory()) {
                return false; // Compulsory subjects are auto-enrolled
            }
            
            int currentTotal = subjectDAO.getTotalStudentSubjectCount(studentId);
            if (currentTotal >= 8) {
                return false;
            }
            
            int categoryCount = subjectDAO.getStudentSubjectCount(studentId, subject.getCategory());
            
            if (subject.isStemScience() && categoryCount >= 2) {
                return false;
            }
            if (subject.isHumanities() && categoryCount >= 2) {
                return false;
            }
            if (subject.isTechnical() && categoryCount >= 2) {
                return false;
            }
            
            return true;
            
        } catch (SQLException e) {
            logger.error("Error checking enrollment eligibility", e);
            return false;
        }
    }

    public void shutdown() {
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

    /**
     * Inner class for student performance summary
     */
    public static class StudentPerformanceSummary {
        private final int studentId;
        private final List<ExamResult> examResults;
        private final BigDecimal averageMarks;
        private final int totalExams;
        private final long passingExams;

        public StudentPerformanceSummary(int studentId, List<ExamResult> examResults, BigDecimal averageMarks) {
            this.studentId = studentId;
            this.examResults = examResults;
            this.averageMarks = averageMarks != null ? averageMarks : BigDecimal.ZERO;
            this.totalExams = examResults.size();
            this.passingExams = examResults.stream()
                .mapToLong(result -> result.isPassing() ? 1 : 0)
                .sum();
        }

        // Getters
        public int getStudentId() { return studentId; }
        public List<ExamResult> getExamResults() { return examResults; }
        public BigDecimal getAverageMarks() { return averageMarks; }
        public int getTotalExams() { return totalExams; }
        public long getPassingExams() { return passingExams; }
        public double getPassingRate() { 
            return totalExams > 0 ? (double) passingExams / totalExams * 100 : 0.0; 
        }
        public String getOverallGrade() { 
            return ExamResult.calculateGrade(averageMarks); 
        }
    }
}