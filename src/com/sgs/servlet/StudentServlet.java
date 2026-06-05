package com.sgs.servlet;

import com.sgs.exception.BusinessException;
import com.sgs.exception.ValidationException;
import com.sgs.model.Student;
import com.sgs.service.StudentService;
import com.sgs.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet for managing student records and dashboard analytics.
 * REST APIs:
 * - GET /students : Fetch all students
 * - POST /student : Add a student
 * - DELETE /student?id={id} : Delete student by id
 * - GET /dashboard : Fetch dashboard statistics
 */
public class StudentServlet extends HttpServlet {

    private final StudentService studentService = StudentService.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/students".equals(path)) {
            List<Student> students = studentService.getAllStudents();
            String json = students.stream()
                    .map(Student::toJson)
                    .collect(Collectors.joining(",", "[", "]"));
            response.getWriter().write(json);
        } else if ("/dashboard".equals(path)) {
            String dashboardJson = studentService.getDashboardJson();
            response.getWriter().write(dashboardJson);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(JsonUtil.error("Endpoint not found"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/student".equals(path)) {
            // Read request body
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();

            // Extract fields
            String name = JsonUtil.getJsonField(body, "name");
            String s1Str = JsonUtil.getJsonField(body, "subject1");
            String s2Str = JsonUtil.getJsonField(body, "subject2");
            String s3Str = JsonUtil.getJsonField(body, "subject3");
            String s4Str = JsonUtil.getJsonField(body, "subject4");

            // Server-side validation
            if (name == null || name.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error("Student Name cannot be empty."));
                return;
            }

            double s1, s2, s3, s4;
            try {
                if (s1Str == null || s2Str == null || s3Str == null || s4Str == null) {
                    throw new NumberFormatException("Marks cannot be empty.");
                }
                s1 = Double.parseDouble(s1Str);
                s2 = Double.parseDouble(s2Str);
                s3 = Double.parseDouble(s3Str);
                s4 = Double.parseDouble(s4Str);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error("All marks must be valid numbers."));
                return;
            }

            // Limit validation
            if (s1 < 0 || s1 > 100 || s2 < 0 || s2 > 100 || s3 < 0 || s3 > 100 || s4 < 0 || s4 > 100) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error("Marks must be between 0 and 100."));
                return;
            }

            try {
                Student newStudent = studentService.addStudent(name.trim(), s1, s2, s3, s4);
                response.getWriter().write(JsonUtil.success("Student added successfully", newStudent.toJson()));
            } catch (ValidationException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error(e.getMessage()));
            } catch (BusinessException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error(e.getMessage()));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(JsonUtil.error("Endpoint not found"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/student".equals(path)) {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error("Student ID is required for deletion."));
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error("Student ID must be numeric."));
                return;
            }

            try {
                boolean deleted = studentService.deleteStudent(id);
                if (deleted) {
                    response.getWriter().write(JsonUtil.success("Student deleted successfully"));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(JsonUtil.error("Student with ID " + id + " not found."));
                }
            } catch (BusinessException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.error(e.getMessage()));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(JsonUtil.error("Endpoint not found"));
        }
    }
}
