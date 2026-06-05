package com.sgs.service;

import com.sgs.model.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StudentService - Business logic layer.
 * Uses ArrayList for in-memory storage (no database).
 * Thread-safe using synchronized methods.
 */
public class StudentService {

    private static final StudentService INSTANCE = new StudentService();
    private final List<Student> students = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1000);

    // Seed sample data for first load
    private StudentService() {
        addStudent("Alice Johnson", 95, 88, 92, 90);
        addStudent("Bob Smith", 72, 65, 70, 68);
        addStudent("Charlie Brown", 55, 60, 58, 62);
        addStudent("Diana Prince", 45, 40, 38, 42);
        addStudent("Ethan Hunt", 82, 78, 85, 80);
    }

    public static StudentService getInstance() { return INSTANCE; }

    // -------- CRUD Operations --------

    public synchronized Student addStudent(String name, double s1, double s2, double s3, double s4) {
        int id = idGenerator.getAndIncrement();
        Student s = new Student(id, name, s1, s2, s3, s4);
        students.add(s);
        return s;
    }

    public synchronized List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public synchronized boolean deleteStudent(int id) {
        return students.removeIf(s -> s.getId() == id);
    }

    public synchronized Student findById(int id) {
        return students.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    public synchronized boolean existsById(int id) {
        return students.stream().anyMatch(s -> s.getId() == id);
    }

    // -------- Dashboard Analytics --------

    public synchronized double getClassAverage() {
        if (students.isEmpty()) return 0.0;
        double sum = 0;
        for (Student s : students) sum += s.getAverage();
        return Math.round((sum / students.size()) * 100.0) / 100.0;
    }

    public synchronized String getTopGrade() {
        if (students.isEmpty()) return "N/A";
        return students.stream()
                .map(Student::getGrade)
                .min((a, b) -> a.compareTo(b))  // A is "top"
                .orElse("N/A");
    }

    public synchronized int countByGrade(String grade) {
        return (int) students.stream().filter(s -> s.getGrade().equals(grade)).count();
    }

    public synchronized String getDashboardJson() {
        return "{"
            + "\"totalStudents\":" + students.size() + ","
            + "\"classAverage\":" + getClassAverage() + ","
            + "\"topGrade\":\"" + getTopGrade() + "\","
            + "\"gradeA\":" + countByGrade("A") + ","
            + "\"gradeB\":" + countByGrade("B") + ","
            + "\"gradeC\":" + countByGrade("C") + ","
            + "\"gradeD\":" + countByGrade("D") + ","
            + "\"gradeF\":" + countByGrade("F")
            + "}";
    }
}
