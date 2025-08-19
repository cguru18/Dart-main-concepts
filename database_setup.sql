-- Student Management System Database Setup
-- Run this script in XAMPP phpMyAdmin or MySQL command line

CREATE DATABASE IF NOT EXISTS student_management_db;
USE student_management_db;

-- Users table for authentication (students, teachers, admins)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admission_number VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Subjects table
CREATE TABLE IF NOT EXISTS subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    subject_code VARCHAR(20) UNIQUE NOT NULL,
    category ENUM('COMPULSORY', 'STEM_SCIENCE', 'HUMANITIES', 'TECHNICAL') NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student subjects enrollment
CREATE TABLE IF NOT EXISTS student_subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY unique_student_subject (student_id, subject_id)
);

-- Examinations table
CREATE TABLE IF NOT EXISTS examinations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_name VARCHAR(255) NOT NULL,
    exam_type ENUM('CAT', 'MID_TERM', 'END_TERM', 'MOCK', 'FINAL') NOT NULL,
    subject_id INT NOT NULL,
    max_marks INT DEFAULT 100,
    exam_date DATE NOT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Student examination results
CREATE TABLE IF NOT EXISTS exam_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    examination_id INT NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    grade CHAR(2),
    remarks TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (examination_id) REFERENCES examinations(id) ON DELETE CASCADE,
    UNIQUE KEY unique_student_exam (student_id, examination_id)
);

-- Student profiles additional info
CREATE TABLE IF NOT EXISTS student_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT UNIQUE NOT NULL,
    date_of_birth DATE,
    phone_number VARCHAR(20),
    address TEXT,
    parent_guardian_name VARCHAR(255),
    parent_guardian_phone VARCHAR(20),
    parent_guardian_email VARCHAR(255),
    class_level VARCHAR(20),
    stream VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert default subjects
INSERT INTO subjects (subject_name, subject_code, category, description) VALUES
-- Compulsory subjects
('Mathematics', 'MATH', 'COMPULSORY', 'Core mathematics curriculum'),
('English', 'ENG', 'COMPULSORY', 'English language and literature'),
('Kiswahili', 'KIS', 'COMPULSORY', 'Kiswahili language'),

-- STEM Sciences (max 2)
('Physics', 'PHY', 'STEM_SCIENCE', 'Physics science curriculum'),
('Chemistry', 'CHEM', 'STEM_SCIENCE', 'Chemistry science curriculum'),
('Biology', 'BIO', 'STEM_SCIENCE', 'Biology science curriculum'),
('Computer Studies', 'COMP', 'STEM_SCIENCE', 'Computer studies and programming'),

-- Humanities (max 2)
('History and Government', 'HIST', 'HUMANITIES', 'History and government studies'),
('Geography', 'GEO', 'HUMANITIES', 'Physical and human geography'),
('Business Studies', 'BUS', 'HUMANITIES', 'Business and entrepreneurship'),

-- Technical subjects (max 2)
('Drawing and Design', 'DRAW', 'TECHNICAL', 'Technical drawing and design'),
('Art and Design', 'ART', 'TECHNICAL', 'Creative arts and design'),
('Music', 'MUS', 'TECHNICAL', 'Music theory and practice');

-- Create default admin user
INSERT INTO users (admission_number, email, full_name, password_hash, role) VALUES
('ADMIN001', 'admin@school.edu', 'System Administrator', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN');

-- Create indexes for better performance
CREATE INDEX idx_users_admission ON users(admission_number);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_exam_results_student ON exam_results(student_id);
CREATE INDEX idx_exam_results_exam ON exam_results(examination_id);
CREATE INDEX idx_student_subjects_student ON student_subjects(student_id);
CREATE INDEX idx_examinations_subject ON examinations(subject_id);