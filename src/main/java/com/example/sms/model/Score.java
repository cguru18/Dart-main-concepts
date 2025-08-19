package com.example.sms.model;

public class Score {
    private long id;
    private long examId;
    private long userId;
    private Subject subject;
    private int marks; // 0-100

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getExamId() { return examId; }
    public void setExamId(long examId) { this.examId = examId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }
}
