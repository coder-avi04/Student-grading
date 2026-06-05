package com.sgs.model;

/**
 * Student Model Class
 * Represents a student entity with marks, calculated average, grade and performance message.
 * Implements encapsulation with private fields and public getters/setters.
 */
public class Student {

    // -------- Fields --------
    private int id;
    private String name;
    private double subject1;
    private double subject2;
    private double subject3;
    private double subject4;
    private double average;
    private String grade;
    private String performanceMessage;

    // -------- Constructors --------
    public Student() { }

    public Student(int id, String name, double subject1, double subject2, double subject3, double subject4) {
        this.id = id;
        this.name = name;
        this.subject1 = subject1;
        this.subject2 = subject2;
        this.subject3 = subject3;
        this.subject4 = subject4;
        calculateResults();
    }

    // -------- Business Logic --------
    public void calculateResults() {
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

    // -------- Getters & Setters --------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSubject1() { return subject1; }
    public void setSubject1(double subject1) { this.subject1 = subject1; calculateResults(); }

    public double getSubject2() { return subject2; }
    public void setSubject2(double subject2) { this.subject2 = subject2; calculateResults(); }

    public double getSubject3() { return subject3; }
    public void setSubject3(double subject3) { this.subject3 = subject3; calculateResults(); }

    public double getSubject4() { return subject4; }
    public void setSubject4(double subject4) { this.subject4 = subject4; calculateResults(); }

    public double getAverage() { return average; }
    public String getGrade() { return grade; }
    public String getPerformanceMessage() { return performanceMessage; }

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
            + "\"performanceMessage\":\"" + escape(performanceMessage) + "\""
            + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
