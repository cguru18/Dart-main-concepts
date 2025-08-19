package com.studentmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Student profile model with additional student information
 */
public class StudentProfile {
    private int id;
    private int studentId;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
    private String parentGuardianName;
    private String parentGuardianPhone;
    private String parentGuardianEmail;
    private String classLevel;
    private String stream;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for subject management
    private List<Subject> enrolledSubjects;

    // Constructors
    public StudentProfile() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.enrolledSubjects = new ArrayList<>();
    }

    public StudentProfile(int studentId) {
        this();
        this.studentId = studentId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getParentGuardianName() { return parentGuardianName; }
    public void setParentGuardianName(String parentGuardianName) { this.parentGuardianName = parentGuardianName; }

    public String getParentGuardianPhone() { return parentGuardianPhone; }
    public void setParentGuardianPhone(String parentGuardianPhone) { this.parentGuardianPhone = parentGuardianPhone; }

    public String getParentGuardianEmail() { return parentGuardianEmail; }
    public void setParentGuardianEmail(String parentGuardianEmail) { this.parentGuardianEmail = parentGuardianEmail; }

    public String getClassLevel() { return classLevel; }
    public void setClassLevel(String classLevel) { this.classLevel = classLevel; }

    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Subject> getEnrolledSubjects() { return enrolledSubjects; }
    public void setEnrolledSubjects(List<Subject> enrolledSubjects) { this.enrolledSubjects = enrolledSubjects; }

    // Utility methods for subject validation
    public boolean canEnrollInSubject(Subject subject) {
        if (subject.isCompulsory()) {
            return true; // Compulsory subjects are automatically enrolled
        }

        long stemCount = enrolledSubjects.stream()
                .filter(Subject::isStemScience)
                .count();
        long humanitiesCount = enrolledSubjects.stream()
                .filter(Subject::isHumanities)
                .count();
        long technicalCount = enrolledSubjects.stream()
                .filter(Subject::isTechnical)
                .count();

        if (subject.isStemScience() && stemCount >= 2) {
            return false;
        }
        if (subject.isHumanities() && humanitiesCount >= 2) {
            return false;
        }
        if (subject.isTechnical() && technicalCount >= 2) {
            return false;
        }

        return getTotalSubjectCount() < 8;
    }

    public int getTotalSubjectCount() {
        return enrolledSubjects.size();
    }

    public boolean hasValidSubjectCombination() {
        int total = getTotalSubjectCount();
        return total >= 7 && total <= 8;
    }

    @Override
    public String toString() {
        return "StudentProfile{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", classLevel='" + classLevel + '\'' +
                ", stream='" + stream + '\'' +
                ", totalSubjects=" + getTotalSubjectCount() +
                '}';
    }
}