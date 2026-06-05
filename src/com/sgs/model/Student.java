package com.sgs.model;

import com.sgs.exception.ValidationException;
import com.sgs.validation.InputValidator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Student Model Class - Enterprise Edition
 * Represents a student entity with validation, audit tracking, and grade calculation.
 * Implements encapsulation with immutable timestamps for audit compliance.
 * 
 * @author IBM Developer
 * @version 2.0
 */
public class Student {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 100.0;

    // -------- Core Fields --------
    private final int id;
    private String name;
    private double subject1;
    private double subject2;
    private double subject3;
    private double subject4;
    private double average;
    private String grade;
    private String performanceMessage;

    // -------- Audit Fields (Immutable) --------
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastModifiedBy;

    // -------- Constructors --------
    public Student() {
        this.id = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Student(int id, String name, double subject1, double subject2, double subject3, double subject4) 
            throws ValidationException {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        setName(name);
        setSubject1(subject1);
        setSubject2(subject2);
        setSubject3(subject3);
        setSubject4(subject4);
    }

    // -------- Business Logic --------
    private void calculateResults() {
        this.average = (subject1 + subject2 + subject3 + subject4) / 4.0;
        this.average = Math.round(this.average * 100.0) / 100.0;

        if (average >= 90) {
            this.grade = "A";
            this.performanceMessage = "Excellent";
        } else if (average >= 75) {
            this.grade = "B";
            this.performanceMessage = "Very Good";
        } else if (average >= 60) {
            this.grade = "C";
            this.performanceMessage = "Good";
        } else if (average >= 50) {
            this.grade = "D";
            this.performanceMessage = "Needs Improvement";
        } else {
            this.grade = "F";
            this.performanceMessage = "Failed";
        }
    }

    private void updateTimestamp(String modifiedBy) {
        this.updatedAt = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    // -------- Getters & Setters --------
    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) throws ValidationException { 
        this.name = InputValidator.validateStudentName(name);
        updateTimestamp("system");
    }

    public double getSubject1() { return subject1; }
    public void setSubject1(double subject1) throws ValidationException { 
        InputValidator.validateRange(subject1, MIN_SCORE, MAX_SCORE, "Subject 1 score");
        this.subject1 = subject1; 
        calculateResults();
        updateTimestamp("system");
    }

    public double getSubject2() { return subject2; }
    public void setSubject2(double subject2) throws ValidationException { 
        InputValidator.validateRange(subject2, MIN_SCORE, MAX_SCORE, "Subject 2 score");
        this.subject2 = subject2; 
        calculateResults();
        updateTimestamp("system");
    }

    public double getSubject3() { return subject3; }
    public void setSubject3(double subject3) throws ValidationException { 
        InputValidator.validateRange(subject3, MIN_SCORE, MAX_SCORE, "Subject 3 score");
        this.subject3 = subject3; 
        calculateResults();
        updateTimestamp("system");
    }

    public double getSubject4() { return subject4; }
    public void setSubject4(double subject4) throws ValidationException { 
        InputValidator.validateRange(subject4, MIN_SCORE, MAX_SCORE, "Subject 4 score");
        this.subject4 = subject4; 
        calculateResults();
        updateTimestamp("system");
    }

    public double getAverage() { return average; }
    public String getGrade() { return grade; }
    public String getPerformanceMessage() { return performanceMessage; }

    // -------- Audit Getters --------
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getLastModifiedBy() { return lastModifiedBy; }

    // -------- JSON Serialization --------
    public String toJson() {
        return "{"
            + "\"id\":" + id + ","
            + "\"name\":\"" + escape(name) + "\","
            + "\"subject1\":" + subject1 + ","
            + "\"subject2\":" + subject2 + ","
            + "\"subject3\":" + subject3 + ","
            + "\"subject4\":" + subject4 + ","
            + "\"average\":" + average + ","
            + "\"grade\":\"" + grade + "\","
            + "\"performanceMessage\":\"" + escape(performanceMessage) + "\","
            + "\"createdAt\":\"" + createdAt.format(DTF) + "\","
            + "\"updatedAt\":\"" + updatedAt.format(DTF) + "\""
            + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return "Student{" + "id=" + id + ", name='" + name + '\'' + ", grade='" + grade + '\'' + '}';
    }
}
