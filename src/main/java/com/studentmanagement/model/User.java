package com.studentmanagement.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User model representing students, teachers, and administrators
 */
public class User {
    public enum Role {
        STUDENT, TEACHER, ADMIN
    }

    private int id;
    private String admissionNumber;
    private String email;
    private String fullName;
    private String passwordHash;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;

    // Constructors
    public User() {
        this.role = Role.STUDENT;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String admissionNumber, String email, String fullName, String passwordHash) {
        this();
        this.admissionNumber = admissionNumber;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
    }

    public User(String admissionNumber, String email, String fullName, String passwordHash, Role role) {
        this(admissionNumber, email, fullName, passwordHash);
        this.role = role;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Utility methods
    public boolean isStudent() { return role == Role.STUDENT; }
    public boolean isTeacher() { return role == Role.TEACHER; }
    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(admissionNumber, user.admissionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, admissionNumber);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", admissionNumber='" + admissionNumber + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}