package com.sgs.listener;

import com.sgs.logging.AppLogger;
import com.sgs.service.StudentService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * ApplicationInitializer - Servlet Context Lifecycle Listener
 * Initializes application resources on startup and cleanup on shutdown.
 * Implements enterprise-grade lifecycle management.
 * 
 * @author IBM Developer
 * @version 2.0
 */
public class ApplicationInitializer implements ServletContextListener {

    private static final AppLogger LOGGER = AppLogger.getInstance();
    private static final String LISTENER_NAME = "ApplicationInitializer";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            LOGGER.info(LISTENER_NAME, "=== APPLICATION STARTUP ===");
            LOGGER.info(LISTENER_NAME, "Application context initializing: %s", 
                    sce.getServletContext().getServletContextName());

            // Initialize singleton services
            initializeServices();

            LOGGER.info(LISTENER_NAME, "Application startup completed successfully");
            LOGGER.info(LISTENER_NAME, "=== APPLICATION READY ===");
        } catch (Exception e) {
            LOGGER.error(LISTENER_NAME, "FATAL: Application initialization failed", e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            LOGGER.info(LISTENER_NAME, "=== APPLICATION SHUTDOWN ===");
            LOGGER.info(LISTENER_NAME, "Application context destroying: %s", 
                    sce.getServletContext().getServletContextName());

            // Cleanup operations
            cleanupResources();

            LOGGER.info(LISTENER_NAME, "Application shutdown completed");
            LOGGER.info(LISTENER_NAME, "=== APPLICATION STOPPED ===");
        } catch (Exception e) {
            LOGGER.error(LISTENER_NAME, "Error during application shutdown", e);
        }
    }

    /**
     * Initializes application services.
     */
    private void initializeServices() {
        // Initialize StudentService singleton
        StudentService studentService = StudentService.getInstance();
        LOGGER.info(LISTENER_NAME, "StudentService initialized with %d records", 
                studentService.getAllStudents().size());

        // Additional initialization can be added here
        LOGGER.info(LISTENER_NAME, "All services initialized successfully");
    }

    /**
     * Cleans up resources during shutdown.
     */
    private void cleanupResources() {
        // Clear any cached resources
        LOGGER.info(LISTENER_NAME, "Resource cleanup completed");
    }
}
