package com.studentmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Examination model representing different types of exams
 */
public class Examination {
    public enum ExamType {
        CAT("Continuous Assessment Test"),
        MID_TERM("Mid Term Examination"),
        END_TERM("End Term Examination"),
        MOCK("Mock Examination"),
        FINAL("Final Examination");

        private final String displayName;

        ExamType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private int id;
    private String examName;
    private ExamType examType;
    private int subjectId;
    private Subject subject; // For joined queries
    private int maxMarks;
    private LocalDate examDate;
    private int createdBy;
    private LocalDateTime createdAt;

    // Constructors
    public Examination() {
        this.maxMarks = 100;
        this.createdAt = LocalDateTime.now();
    }

    public Examination(String examName, ExamType examType, int subjectId, LocalDate examDate, int createdBy) {
        this();
        this.examName = examName;
        this.examType = examType;
        this.subjectId = subjectId;
        this.examDate = examDate;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Utility methods
    public String getFullExamName() {
        return examType.getDisplayName() + " - " + examName;
    }

    public boolean isUpcoming() {
        return examDate.isAfter(LocalDate.now());
    }

    public boolean isPast() {
        return examDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Examination that = (Examination) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Examination{" +
                "id=" + id +
                ", examName='" + examName + '\'' +
                ", examType=" + examType +
                ", subjectId=" + subjectId +
                ", examDate=" + examDate +
                '}';
    }
}