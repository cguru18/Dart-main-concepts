package com.studentmanagement.dao;

import com.studentmanagement.model.ExamResult;
import com.studentmanagement.model.Examination;
import com.studentmanagement.model.Subject;
import com.studentmanagement.model.User;
import com.studentmanagement.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for ExamResult operations
 */
public class ExamResultDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExamResultDAO.class);
    private final DatabaseConnection dbConnection;

    public ExamResultDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public ExamResult save(ExamResult examResult) throws SQLException {
        String sql = "INSERT INTO exam_results (student_id, examination_id, marks_obtained, grade, remarks) VALUES (?, ?, ?, ?, ?)";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, examResult.getStudentId());
                stmt.setInt(2, examResult.getExaminationId());
                stmt.setBigDecimal(3, examResult.getMarksObtained());
                stmt.setString(4, examResult.getGrade());
                stmt.setString(5, examResult.getRemarks());

                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            examResult.setId(generatedKeys.getInt(1));
                        }
                    }
                }
                
                logger.info("Exam result saved for student {} in exam {}", 
                           examResult.getStudentId(), examResult.getExaminationId());
                return examResult;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public List<ExamResult> findByStudentId(int studentId) throws SQLException {
        String sql = """
            SELECT er.*, e.exam_name, e.exam_type, e.max_marks, e.exam_date,
                   s.subject_name, s.subject_code, s.category,
                   u.full_name as student_name, u.admission_number
            FROM exam_results er
            INNER JOIN examinations e ON er.examination_id = e.id
            INNER JOIN subjects s ON e.subject_id = s.id
            INNER JOIN users u ON er.student_id = u.id
            WHERE er.student_id = ?
            ORDER BY e.exam_date DESC, s.subject_name
        """;
        
        List<ExamResult> results = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToExamResultWithDetails(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return results;
    }

    public List<ExamResult> findByExaminationId(int examinationId) throws SQLException {
        String sql = """
            SELECT er.*, u.full_name as student_name, u.admission_number,
                   e.exam_name, e.exam_type, e.max_marks, e.exam_date,
                   s.subject_name, s.subject_code
            FROM exam_results er
            INNER JOIN users u ON er.student_id = u.id
            INNER JOIN examinations e ON er.examination_id = e.id
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE er.examination_id = ?
            ORDER BY er.marks_obtained DESC
        """;
        
        List<ExamResult> results = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, examinationId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    int position = 1;
                    while (rs.next()) {
                        ExamResult result = mapResultSetToExamResultWithDetails(rs);
                        result.setPosition(position++);
                        results.add(result);
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return results;
    }

    public List<ExamResult> findBySubjectId(int subjectId) throws SQLException {
        String sql = """
            SELECT er.*, u.full_name as student_name, u.admission_number,
                   e.exam_name, e.exam_type, e.max_marks, e.exam_date,
                   s.subject_name, s.subject_code
            FROM exam_results er
            INNER JOIN users u ON er.student_id = u.id
            INNER JOIN examinations e ON er.examination_id = e.id
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE s.id = ?
            ORDER BY e.exam_date DESC, er.marks_obtained DESC
        """;
        
        List<ExamResult> results = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, subjectId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToExamResultWithDetails(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return results;
    }

    public Optional<ExamResult> findByStudentAndExam(int studentId, int examinationId) throws SQLException {
        String sql = """
            SELECT er.*, u.full_name as student_name, u.admission_number,
                   e.exam_name, e.exam_type, e.max_marks, e.exam_date,
                   s.subject_name, s.subject_code
            FROM exam_results er
            INNER JOIN users u ON er.student_id = u.id
            INNER JOIN examinations e ON er.examination_id = e.id
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE er.student_id = ? AND er.examination_id = ?
        """;
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, examinationId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToExamResultWithDetails(rs));
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return Optional.empty();
    }

    public ExamResult update(ExamResult examResult) throws SQLException {
        String sql = "UPDATE exam_results SET marks_obtained = ?, grade = ?, remarks = ? WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setBigDecimal(1, examResult.getMarksObtained());
                stmt.setString(2, examResult.getGrade());
                stmt.setString(3, examResult.getRemarks());
                stmt.setInt(4, examResult.getId());

                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info("Exam result updated successfully: ID {}", examResult.getId());
                } else {
                    throw new SQLException("No exam result found with ID: " + examResult.getId());
                }
                
                return examResult;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public boolean delete(int examResultId) throws SQLException {
        String sql = "DELETE FROM exam_results WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, examResultId);

                int rowsAffected = stmt.executeUpdate();
                boolean deleted = rowsAffected > 0;
                
                if (deleted) {
                    logger.info("Exam result deleted successfully: ID {}", examResultId);
                }
                
                return deleted;
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
    }

    public List<ExamResult> getStudentRankingByExam(int examinationId) throws SQLException {
        String sql = """
            SELECT er.*, u.full_name as student_name, u.admission_number,
                   e.exam_name, e.exam_type, e.max_marks, e.exam_date,
                   s.subject_name, s.subject_code,
                   RANK() OVER (ORDER BY er.marks_obtained DESC) as position
            FROM exam_results er
            INNER JOIN users u ON er.student_id = u.id
            INNER JOIN examinations e ON er.examination_id = e.id
            INNER JOIN subjects s ON e.subject_id = s.id
            WHERE er.examination_id = ?
            ORDER BY er.marks_obtained DESC
        """;
        
        List<ExamResult> results = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, examinationId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ExamResult result = mapResultSetToExamResultWithDetails(rs);
                        result.setPosition(rs.getInt("position"));
                        results.add(result);
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return results;
    }

    public BigDecimal getStudentAverageMarks(int studentId) throws SQLException {
        String sql = "SELECT AVG(marks_obtained) as avg_marks FROM exam_results WHERE student_id = ?";
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("avg_marks");
                    }
                }
            }
        } finally {
            dbConnection.returnConnection(connection);
        }
        
        return BigDecimal.ZERO;
    }

    private ExamResult mapResultSetToExamResultWithDetails(ResultSet rs) throws SQLException {
        ExamResult result = new ExamResult();
        result.setId(rs.getInt("id"));
        result.setStudentId(rs.getInt("student_id"));
        result.setExaminationId(rs.getInt("examination_id"));
        result.setMarksObtained(rs.getBigDecimal("marks_obtained"));
        result.setGrade(rs.getString("grade"));
        result.setRemarks(rs.getString("remarks"));
        
        Timestamp submittedAt = rs.getTimestamp("submitted_at");
        if (submittedAt != null) {
            result.setSubmittedAt(submittedAt.toLocalDateTime());
        }

        // Map examination details
        Examination exam = new Examination();
        exam.setId(rs.getInt("examination_id"));
        exam.setExamName(rs.getString("exam_name"));
        exam.setExamType(Examination.ExamType.valueOf(rs.getString("exam_type")));
        exam.setMaxMarks(rs.getInt("max_marks"));
        
        Date examDate = rs.getDate("exam_date");
        if (examDate != null) {
            exam.setExamDate(examDate.toLocalDate());
        }

        // Map subject details
        Subject subject = new Subject();
        subject.setSubjectName(rs.getString("subject_name"));
        subject.setSubjectCode(rs.getString("subject_code"));
        
        exam.setSubject(subject);
        result.setExamination(exam);

        // Map student details if available
        String studentName = rs.getString("student_name");
        String admissionNumber = rs.getString("admission_number");
        if (studentName != null && admissionNumber != null) {
            User student = new User();
            student.setId(rs.getInt("student_id"));
            student.setFullName(studentName);
            student.setAdmissionNumber(admissionNumber);
            result.setStudent(student);
        }
        
        return result;
    }
}