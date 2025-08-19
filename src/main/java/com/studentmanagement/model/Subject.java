package com.studentmanagement.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Subject model representing different academic subjects
 */
public class Subject {
    public enum Category {
        COMPULSORY,      // Mathematics, English, Kiswahili
        STEM_SCIENCE,    // Physics, Chemistry, Biology, Computer Studies (max 2)
        HUMANITIES,      // History, Geography, Business Studies (max 2)
        TECHNICAL        // Drawing, Art, Music (max 2)
    }

    private int id;
    private String subjectName;
    private String subjectCode;
    private Category category;
    private String description;
    private LocalDateTime createdAt;

    // Constructors
    public Subject() {
        this.createdAt = LocalDateTime.now();
    }

    public Subject(String subjectName, String subjectCode, Category category) {
        this();
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.category = category;
    }

    public Subject(String subjectName, String subjectCode, Category category, String description) {
        this(subjectName, subjectCode, category);
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Utility methods
    public boolean isCompulsory() { return category == Category.COMPULSORY; }
    public boolean isStemScience() { return category == Category.STEM_SCIENCE; }
    public boolean isHumanities() { return category == Category.HUMANITIES; }
    public boolean isTechnical() { return category == Category.TECHNICAL; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id == subject.id && Objects.equals(subjectCode, subject.subjectCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subjectCode);
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", subjectName='" + subjectName + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", category=" + category +
                '}';
    }
}