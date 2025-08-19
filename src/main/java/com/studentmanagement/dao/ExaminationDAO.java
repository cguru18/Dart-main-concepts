package com.studentmanagement.dao;

import com.studentmanagement.model.Examination;
import com.studentmanagement.model.Subject;
import com.studentmanagement.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Examination operations
 */
public class ExaminationDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExaminationDAO.class);
    private final DatabaseConnection dbConnection;

    public ExaminationDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public Examination save(Examination examination) throws SQLException {
        String sql = "INSERT INTO examinations (exam_name, exam_type, subject_id, max_marks, exam_date, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, examination.getExamName());
                stmt.setString(2, examination.getExamType().name());
                stmt.setInt(3, examination.getSubjectId());
                stmt.setInt(4, examination.getMaxMarks());
                stmt.setDate(5, Date.valueOf(examination.getExamDate()));
                stmt.setInt(6, examination.getCreatedBy());

                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            examination.setId(generatedKeys.getInt(1));
                        }
                    }
                }
                
                logger.info("Examination saved successfully: {}", examination.getExamName());
                return examination;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public Optional<Examination> findById(int id) throws SQLException {
        String sql = """
            SELECT e.*, s.subject_name, s.subject_code, s.category
            FROM examinations e
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE e.id = ?
        """;
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToExaminationWithSubject(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return Optional.empty();
    }

    public List<Examination> findBySubjectId(int subjectId) throws SQLException {
        String sql = """
            SELECT e.*, s.subject_name, s.subject_code, s.category
            FROM examinations e
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE e.subject_id = ?
            ORDER BY e.exam_date DESC
        """;
        
        List<Examination> examinations = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, subjectId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        examinations.add(mapResultSetToExaminationWithSubject(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return examinations;
    }

    public List<Examination> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = """
            SELECT e.*, s.subject_name, s.subject_code, s.category
            FROM examinations e
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE e.exam_date BETWEEN ? AND ?
            ORDER BY e.exam_date DESC
        """;
        
        List<Examination> examinations = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        examinations.add(mapResultSetToExaminationWithSubject(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return examinations;
    }

    public List<Examination> findUpcomingExams() throws SQLException {
        String sql = """
            SELECT e.*, s.subject_name, s.subject_code, s.category
            FROM examinations e
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE e.exam_date >= CURDATE()
            ORDER BY e.exam_date ASC
        """;
        
        List<Examination> examinations = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    examinations.add(mapResultSetToExaminationWithSubject(rs));
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return examinations;
    }

    public List<Examination> findAll() throws SQLException {
        String sql = """
            SELECT e.*, s.subject_name, s.subject_code, s.category
            FROM examinations e
            INNER JOIN subjects s ON e.subject_id = s.id
            ORDER BY e.exam_date DESC
        """;
        
        List<Examination> examinations = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    examinations.add(mapResultSetToExaminationWithSubject(rs));
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return examinations;
    }

    public Examination update(Examination examination) throws SQLException {
        String sql = "UPDATE examinations SET exam_name = ?, exam_type = ?, subject_id = ?, max_marks = ?, exam_date = ? WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, examination.getExamName());
                stmt.setString(2, examination.getExamType().name());
                stmt.setInt(3, examination.getSubjectId());
                stmt.setInt(4, examination.getMaxMarks());
                stmt.setDate(5, Date.valueOf(examination.getExamDate()));
                stmt.setInt(6, examination.getId());

                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info("Examination updated successfully: ID {}", examination.getId());
                } else {
                    throw new SQLException("No examination found with ID: " + examination.getId());
                }
                
                return examination;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public boolean delete(int examinationId) throws SQLException {
        String sql = "DELETE FROM examinations WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, examinationId);

                int rowsAffected = stmt.executeUpdate();
                boolean deleted = rowsAffected > 0;
                
                if (deleted) {
                    logger.info("Examination deleted successfully: ID {}", examinationId);
                }
                
                return deleted;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public int getStudentCountForExam(int examinationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exam_results WHERE examination_id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, examinationId);
                
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

    private Examination mapResultSetToExaminationWithSubject(ResultSet rs) throws SQLException {
        Examination examination = new Examination();
        examination.setId(rs.getInt("id"));
        examination.setExamName(rs.getString("exam_name"));
        examination.setExamType(Examination.ExamType.valueOf(rs.getString("exam_type")));
        examination.setSubjectId(rs.getInt("subject_id"));
        examination.setMaxMarks(rs.getInt("max_marks"));
        examination.setCreatedBy(rs.getInt("created_by"));
        
        Date examDate = rs.getDate("exam_date");
        if (examDate != null) {
            examination.setExamDate(examDate.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            examination.setCreatedAt(createdAt.toLocalDateTime());
        }

        // Map subject details
        Subject subject = new Subject();
        subject.setId(examination.getSubjectId());
        subject.setSubjectName(rs.getString("subject_name"));
        subject.setSubjectCode(rs.getString("subject_code"));
        subject.setCategory(Subject.Category.valueOf(rs.getString("category")));
        
        examination.setSubject(subject);
        
        return examination;
    }
}