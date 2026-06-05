package com.sgs.filter;

import com.sgs.logging.AppLogger;
import com.sgs.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthFilter - Enterprise Request Authentication Filter
 * Intercepts all incoming requests and enforces session-based authentication.
 * Permits access to login pages, APIs, and static assets.
 * All access attempts are logged for audit compliance.
 * 
 * @author IBM Developer
 * @version 2.0
 */
public class AuthFilter implements Filter {

    private static final AppLogger LOGGER = AppLogger.getInstance();
    private static final String FILTER_NAME = "AuthFilter";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info(FILTER_NAME, "Initializing authentication filter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());
        String remoteIP = httpRequest.getRemoteAddr();
        String method = httpRequest.getMethod();

        try {
            // Check if request is for public resources
            if (isPublicResource(path)) {
                LOGGER.debug(FILTER_NAME, "Public resource access - Path: %s, IP: %s", path, remoteIP);
                chain.doFilter(request, response);
                return;
            }

            // Validate active session
            HttpSession session = httpRequest.getSession(false);
            if (isAuthenticated(session)) {
                String user = (String) session.getAttribute("loggedInUser");
                LOGGER.debug(FILTER_NAME, "Authenticated access - User: %s, Path: %s, IP: %s", 
                        user, path, remoteIP);
                chain.doFilter(request, response);
                return;
            }

            // Handle unauthenticated access
            handleUnauthorized(httpRequest, httpResponse, path, remoteIP, method);

        } catch (Exception e) {
            LOGGER.error(FILTER_NAME, "Error in authentication filter", e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(JsonUtil.error("ERR_AUTH_002", "Authentication check failed"));
        }
    }

    /**
     * Checks if the requested path is a public resource.
     */
    private boolean isPublicResource(String path) {
        return path.equals("/login.html") 
                || path.equals("/login") 
                || path.equals("/") 
                || path.startsWith("/css/") 
                || path.startsWith("/js/")
                || path.startsWith("/img/");
    }

    /**
     * Checks if the session is authenticated.
     */
    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("loggedInUser") != null;
    }

    /**
     * Handles unauthorized access attempts.
     */
    private void handleUnauthorized(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                    String path, String remoteIP, String method) throws IOException {
        
        LOGGER.warn(FILTER_NAME, "Unauthorized access attempt - Path: %s, Method: %s, IP: %s", 
                path, method, remoteIP);

        boolean isApiRequest = isApiPath(path);
        
        if (isApiRequest) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(JsonUtil.error("ERR_AUTH_003", "Session expired. Please log in again."));
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
        }
    }

    /**
     * Determines if the path is an API endpoint.
     */
    private boolean isApiPath(String path) {
        return path.equals("/student") 
                || path.equals("/students") 
                || path.equals("/dashboard")
                || path.startsWith("/api/");
    }

    @Override
    public void destroy() {
        LOGGER.info(FILTER_NAME, "Destroying authentication filter");
    }
}
