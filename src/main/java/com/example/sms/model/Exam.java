package com.example.sms.model;

import java.time.Instant;

public class Exam {
    private long id;
    private String name;
    private int term;
    private int year;
    private Instant createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTerm() { return term; }
    public void setTerm(int term) { this.term = term; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
