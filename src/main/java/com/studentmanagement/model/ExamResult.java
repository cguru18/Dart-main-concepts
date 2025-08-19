package com.studentmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Exam result model representing student performance in examinations
 */
public class ExamResult {
    private int id;
    private int studentId;
    private int examinationId;
    private BigDecimal marksObtained;
    private String grade;
    private String remarks;
    private LocalDateTime submittedAt;
    
    // For joined queries
    private User student;
    private Examination examination;
    private int position; // Ranking position in the subject

    // Constructors
    public ExamResult() {
        this.submittedAt = LocalDateTime.now();
    }

    public ExamResult(int studentId, int examinationId, BigDecimal marksObtained) {
        this();
        this.studentId = studentId;
        this.examinationId = examinationId;
        this.marksObtained = marksObtained;
        this.grade = calculateGrade(marksObtained);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getExaminationId() { return examinationId; }
    public void setExaminationId(int examinationId) { this.examinationId = examinationId; }

    public BigDecimal getMarksObtained() { return marksObtained; }
    public void setMarksObtained(BigDecimal marksObtained) { 
        this.marksObtained = marksObtained;
        this.grade = calculateGrade(marksObtained);
    }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Examination getExamination() { return examination; }
    public void setExamination(Examination examination) { this.examination = examination; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    // Utility methods
    public static String calculateGrade(BigDecimal marks) {
        if (marks == null) return "F";
        
        double score = marks.doubleValue();
        if (score >= 80) return "A";
        else if (score >= 75) return "A-";
        else if (score >= 70) return "B+";
        else if (score >= 65) return "B";
        else if (score >= 60) return "B-";
        else if (score >= 55) return "C+";
        else if (score >= 50) return "C";
        else if (score >= 45) return "C-";
        else if (score >= 40) return "D+";
        else if (score >= 35) return "D";
        else if (score >= 30) return "D-";
        else return "E";
    }

    public double getPercentage() {
        if (marksObtained == null || examination == null) return 0.0;
        return (marksObtained.doubleValue() / examination.getMaxMarks()) * 100;
    }

    public boolean isPassing() {
        return marksObtained != null && marksObtained.doubleValue() >= 40;
    }

    public String getPerformanceLevel() {
        double percentage = getPercentage();
        if (percentage >= 80) return "Excellent";
        else if (percentage >= 70) return "Very Good";
        else if (percentage >= 60) return "Good";
        else if (percentage >= 50) return "Average";
        else if (percentage >= 40) return "Below Average";
        else return "Poor";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamResult that = (ExamResult) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ExamResult{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", examinationId=" + examinationId +
                ", marksObtained=" + marksObtained +
                ", grade='" + grade + '\'' +
                ", position=" + position +
                '}';
    }
}