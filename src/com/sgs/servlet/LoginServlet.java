package com.sgs.servlet;

import com.sgs.exception.AuthenticationException;
import com.sgs.exception.ValidationException;
import com.sgs.logging.AppLogger;
import com.sgs.util.JsonUtil;
import com.sgs.validation.InputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * LoginServlet - Enterprise Authentication Handler
 * Manages session-based authentication with comprehensive logging and validation.
 * 
 * REST APIs:
 * - POST /login : Process login with credentials
 * - GET /login?action=logout : Invalidate session
 * - GET /login : Check active session status
 * 
 * @author IBM Developer
 * @version 2.0
 */
public class LoginServlet extends HttpServlet {

    private static final AppLogger LOGGER = AppLogger.getInstance();
    private static final String SERVLET_NAME = "LoginServlet";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final int SESSION_TIMEOUT = 30 * 60; // 30 minutes

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.debug(SERVLET_NAME, "Processing login request from IP: %s", request.getRemoteAddr());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read and parse request body
            String body = readRequestBody(request);
            String username = JsonUtil.getJsonField(body, "username");
            String password = JsonUtil.getJsonField(body, "password");

            // Validate input
            InputValidator.validateCredentials(username, password);
            
            // Authenticate user
            if (!authenticateUser(username, password)) {
                LOGGER.warn(SERVLET_NAME, "Authentication failed for username: %s", username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(JsonUtil.error("ERR_AUTH_001", "Invalid username or password"));
                return;
            }

            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", username);
            session.setMaxInactiveInterval(SESSION_TIMEOUT);
            
            LOGGER.info(SERVLET_NAME, "User authenticated successfully - Username: %s, SessionID: %s", 
                    username, session.getId());
            response.getWriter().write(JsonUtil.success("Login successful"));

        } catch (ValidationException ve) {
            LOGGER.warn(SERVLET_NAME, "Validation error during login: %s", ve.getMessage());
            response.setStatus(ve.getHttpStatus());
            response.getWriter().write(JsonUtil.error(ve.getErrorCode(), ve.getMessage()));
        } catch (Exception e) {
            LOGGER.error(SERVLET_NAME, "Unexpected error in login processing", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.error("ERR_SYS_001", "Login processing failed"));
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Handle Logout request
            String action = request.getParameter("action");
            if ("logout".equals(action)) {
                handleLogout(request, response);
                return;
            }
            
            // Session validation status check
            checkSessionStatus(request, response);
        } catch (Exception e) {
            LOGGER.error(SERVLET_NAME, "Error in GET handler", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.error("ERR_SYS_002", "Session check failed"));
        }
    }

    /**
     * Handles user logout.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("loggedInUser");
            session.invalidate();
            LOGGER.info(SERVLET_NAME, "User logged out successfully - Username: %s", username);
        }
        response.getWriter().write(JsonUtil.success("Logged out successfully"));
    }

    /**
     * Checks if session is active and valid.
     */
    private void checkSessionStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            String username = (String) session.getAttribute("loggedInUser");
            LOGGER.debug(SERVLET_NAME, "Session check - Active session for user: %s", username);
            response.getWriter().write("{\"status\":\"success\",\"loggedIn\":true,\"user\":\"" + 
                    InputValidator.sanitize(username) + "\"}");
        } else {
            LOGGER.debug(SERVLET_NAME, "Session check - No active session");
            response.getWriter().write("{\"status\":\"success\",\"loggedIn\":false}");
        }
    }

    /**
     * Reads complete request body.
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Authenticates user credentials.
     */
    private boolean authenticateUser(String username, String password) {
        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }
}
