package com.sgs.service;

import com.sgs.exception.BusinessException;
import com.sgs.exception.ValidationException;
import com.sgs.logging.AppLogger;
import com.sgs.model.Student;
import com.sgs.validation.InputValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StudentService - Enterprise Business Logic Layer
 * Implements CRUD operations with full validation, logging, and error handling.
 * Thread-safe singleton pattern with structured logging.
 * 
 * @author IBM Developer
 * @version 2.0
 */
public class StudentService {

    private static final StudentService INSTANCE = new StudentService();
    private static final AppLogger LOGGER = AppLogger.getInstance();
    private static final String SERVICE_NAME = "StudentService";

    private final List<Student> students = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1000);

    // Seed sample data for first load
    private StudentService() {
        try {
            LOGGER.info(SERVICE_NAME, "Initializing StudentService with seed data");
            addStudent("Alice Johnson", 95, 88, 92, 90);
            addStudent("Bob Smith", 72, 65, 70, 68);
            addStudent("Charlie Brown", 55, 60, 58, 62);
            addStudent("Diana Prince", 45, 40, 38, 42);
            addStudent("Ethan Hunt", 82, 78, 85, 80);
            LOGGER.info(SERVICE_NAME, "StudentService initialized successfully with %d students", students.size());
        } catch (Exception e) {
            LOGGER.error(SERVICE_NAME, "Failed to initialize StudentService: %s", e, e.getMessage());
        }
    }

    public static StudentService getInstance() {
        return INSTANCE;
    }

    // -------- CRUD Operations --------

    /**
     * Adds a new student with validation.
     * @throws ValidationException if validation fails
     */
    public synchronized Student addStudent(String name, double s1, double s2, double s3, double s4) 
            throws ValidationException, BusinessException {
        try {
            // Validate input
            String validatedName = InputValidator.validateStudentName(name);
            InputValidator.validateRange(s1, 0, 100, "Subject 1");
            InputValidator.validateRange(s2, 0, 100, "Subject 2");
            InputValidator.validateRange(s3, 0, 100, "Subject 3");
            InputValidator.validateRange(s4, 0, 100, "Subject 4");

            int id = idGenerator.getAndIncrement();
            Student student = new Student(id, validatedName, s1, s2, s3, s4);
            students.add(student);

            LOGGER.info(SERVICE_NAME, "Student added successfully - ID: %d, Name: %s, Grade: %s", 
                    id, validatedName, student.getGrade());
            return student;
        } catch (ValidationException ve) {
            LOGGER.warn(SERVICE_NAME, "Validation error while adding student: %s", ve.getMessage());
            throw ve;
        } catch (Exception e) {
            LOGGER.error(SERVICE_NAME, "Unexpected error while adding student", e);
            throw new BusinessException("SVC_001", "Failed to add student: " + e.getMessage());
        }
    }

    /**
     * Retrieves all students with defensive copying.
     */
    public synchronized List<Student> getAllStudents() {
        LOGGER.debug(SERVICE_NAME, "Retrieving all %d students", students.size());
        return new ArrayList<>(students);
    }

    /**
     * Deletes a student by ID.
     * @return true if deletion successful, false if student not found
     */
    public synchronized boolean deleteStudent(int id) throws BusinessException {
        try {
            boolean deleted = students.removeIf(s -> s.getId() == id);
            if (deleted) {
                LOGGER.info(SERVICE_NAME, "Student deleted successfully - ID: %d", id);
            } else {
                LOGGER.warn(SERVICE_NAME, "Student not found for deletion - ID: %d", id);
            }
            return deleted;
        } catch (Exception e) {
            LOGGER.error(SERVICE_NAME, "Error deleting student ID: %d", e, id);
            throw new BusinessException("SVC_002", "Failed to delete student");
        }
    }

    /**
     * Finds student by ID.
     * @return Student or null if not found
     */
    public synchronized Student findById(int id) throws BusinessException {
        try {
            return students.stream()
                    .filter(s -> s.getId() == id)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error(SERVICE_NAME, "Error finding student by ID: %d", e, id);
            throw new BusinessException("SVC_003", "Failed to find student");
        }
    }

    public synchronized boolean existsById(int id) {
        return students.stream().anyMatch(s -> s.getId() == id);
    }

    // -------- Dashboard Analytics --------

    /**
     * Calculates class average with rounding.
     */
    public synchronized double getClassAverage() {
        if (students.isEmpty()) {
            return 0.0;
        }
        double sum = students.stream().mapToDouble(Student::getAverage).sum();
        return Math.round((sum / students.size()) * 100.0) / 100.0;
    }

    /**
     * Gets the highest grade (top grade).
     */
    public synchronized String getTopGrade() {
        if (students.isEmpty()) return "N/A";
        return students.stream()
                .map(Student::getGrade)
                .min(String::compareTo)  // A is highest
                .orElse("N/A");
    }

    /**
     * Counts students by grade.
     */
    public synchronized int countByGrade(String grade) {
        return (int) students.stream()
                .filter(s -> s.getGrade().equals(grade))
                .count();
    }

    /**
     * Gets grade distribution statistics.
     */
    public synchronized GradeStatistics getGradeStatistics() {
        return new GradeStatistics(
                students.size(),
                getClassAverage(),
                getTopGrade(),
                countByGrade("A"),
                countByGrade("B"),
                countByGrade("C"),
                countByGrade("D"),
                countByGrade("F")
        );
    }

    /**
     * Generates dashboard JSON with analytics.
     */
    public synchronized String getDashboardJson() {
        GradeStatistics stats = getGradeStatistics();
        return "{"
            + "\"totalStudents\":" + stats.totalStudents + ","
            + "\"classAverage\":" + stats.classAverage + ","
            + "\"topGrade\":\"" + stats.topGrade + "\","
            + "\"gradeA\":" + stats.gradeA + ","
            + "\"gradeB\":" + stats.gradeB + ","
            + "\"gradeC\":" + stats.gradeC + ","
            + "\"gradeD\":" + stats.gradeD + ","
            + "\"gradeF\":" + stats.gradeF
            + "}";
    }

    /**
     * Inner class for grade statistics.
     */
    public static class GradeStatistics {
        public final int totalStudents;
        public final double classAverage;
        public final String topGrade;
        public final int gradeA, gradeB, gradeC, gradeD, gradeF;

        public GradeStatistics(int total, double avg, String top, int a, int b, int c, int d, int f) {
            this.totalStudents = total;
            this.classAverage = avg;
            this.topGrade = top;
            this.gradeA = a;
            this.gradeB = b;
            this.gradeC = c;
            this.gradeD = d;
            this.gradeF = f;
        }
    }
}
