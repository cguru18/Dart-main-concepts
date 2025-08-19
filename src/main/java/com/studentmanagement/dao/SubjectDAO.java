package com.studentmanagement.dao;

import com.studentmanagement.model.Subject;
import com.studentmanagement.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Subject operations
 */
public class SubjectDAO {
    private static final Logger logger = LoggerFactory.getLogger(SubjectDAO.class);
    private final DatabaseConnection dbConnection;

    public SubjectDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public List<Subject> findAll() throws SQLException {
        String sql = "SELECT * FROM subjects ORDER BY category, subject_name";
        List<Subject> subjects = new ArrayList<>();
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return subjects;
    }

    public List<Subject> findByCategory(Subject.Category category) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE category = ? ORDER BY subject_name";
        List<Subject> subjects = new ArrayList<>();
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, category.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        subjects.add(mapResultSetToSubject(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return subjects;
    }

    public Optional<Subject> findById(int id) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToSubject(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return Optional.empty();
    }

    public Optional<Subject> findByCode(String subjectCode) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE subject_code = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, subjectCode);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToSubject(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return Optional.empty();
    }

    public List<Subject> findStudentSubjects(int studentId) throws SQLException {
        String sql = """
            SELECT s.* FROM subjects s
            INNER JOIN student_subjects ss ON s.id = ss.subject_id
            WHERE ss.student_id = ? AND ss.is_active = TRUE
            ORDER BY s.category, s.subject_name
        """;
        
        List<Subject> subjects = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        subjects.add(mapResultSetToSubject(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return subjects;
    }

    public boolean enrollStudentInSubject(int studentId, int subjectId) throws SQLException {
        String sql = "INSERT INTO student_subjects (student_id, subject_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE is_active = TRUE, enrollment_date = CURRENT_TIMESTAMP";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, subjectId);
                
                int rowsAffected = stmt.executeUpdate();
                boolean enrolled = rowsAffected > 0;
                
                if (enrolled) {
                    logger.info("Student {} enrolled in subject {}", studentId, subjectId);
                }
                
                return enrolled;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public boolean unenrollStudentFromSubject(int studentId, int subjectId) throws SQLException {
        String sql = "UPDATE student_subjects SET is_active = FALSE WHERE student_id = ? AND subject_id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, subjectId);
                
                int rowsAffected = stmt.executeUpdate();
                boolean unenrolled = rowsAffected > 0;
                
                if (unenrolled) {
                    logger.info("Student {} unenrolled from subject {}", studentId, subjectId);
                }
                
                return unenrolled;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public void enrollStudentInCompulsorySubjects(int studentId) throws SQLException {
        List<Subject> compulsorySubjects = findByCategory(Subject.Category.COMPULSORY);
        
        for (Subject subject : compulsorySubjects) {
            try {
                enrollStudentInSubject(studentId, subject.getId());
            } catch (SQLException e) {
                logger.warn("Failed to enroll student {} in compulsory subject {}: {}", 
                           studentId, subject.getSubjectCode(), e.getMessage());
            }
        }
    }

    public int getStudentSubjectCount(int studentId, Subject.Category category) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM student_subjects ss
            INNER JOIN subjects s ON ss.subject_id = s.id
            WHERE ss.student_id = ? AND s.category = ? AND ss.is_active = TRUE
        """;
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setString(2, category.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return 0;
    }

    public int getTotalStudentSubjectCount(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM student_subjects WHERE student_id = ? AND is_active = TRUE";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return 0;
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setId(rs.getInt("id"));
        subject.setSubjectName(rs.getString("subject_name"));
        subject.setSubjectCode(rs.getString("subject_code"));
        subject.setCategory(Subject.Category.valueOf(rs.getString("category")));
        subject.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            subject.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return subject;
    }
}