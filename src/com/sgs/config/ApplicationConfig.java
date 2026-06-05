package com.sgs.config;

/**
 * ApplicationConfig - Centralized Configuration Management
 * Implements IBM best practices for environment-aware configuration.
 * Single source of truth for all application constants and settings.
 * 
 * @author IBM Developer
 * @version 2.0
 */
public final class ApplicationConfig {

    // ========== Application Metadata ==========
    public static final String APP_NAME = "Student Grading System";
    public static final String APP_VERSION = "2.0.0";
    public static final String APP_ENVIRONMENT = getEnvironment();

    // ========== Security Configuration ==========
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCKOUT_DURATION_MINUTES = 15;

    // ========== Validation Constraints ==========
    public static final double SCORE_MIN = 0.0;
    public static final double SCORE_MAX = 100.0;
    public static final int STUDENT_NAME_MIN_LENGTH = 2;
    public static final int STUDENT_NAME_MAX_LENGTH = 255;
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 20;
    public static final int PASSWORD_MIN_LENGTH = 6;

    // ========== Grade Thresholds ==========
    public static final double GRADE_A_THRESHOLD = 90.0;
    public static final double GRADE_B_THRESHOLD = 75.0;
    public static final double GRADE_C_THRESHOLD = 60.0;
    public static final double GRADE_D_THRESHOLD = 50.0;

    // ========== Logging Configuration ==========
    public static final String LOG_DIRECTORY = "logs";
    public static final String LOG_FILE_NAME = "sgs-application.log";
    public static final long LOG_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    public static final int LOG_FILE_COUNT = 3;

    // ========== API Response Codes ==========
    public static final String SUCCESS_CODE = "SCS_000";
    public static final String ERROR_GENERIC_CODE = "ERR_000";
    public static final String ERROR_VALIDATION_CODE = "VAL_000";
    public static final String ERROR_AUTH_CODE = "AUTH_000";
    public static final String ERROR_BUSINESS_CODE = "BIZ_000";

    private ApplicationConfig() {
        // Prevent instantiation
    }

    /**
     * Gets the current environment (Development, Testing, Production).
     */
    private static String getEnvironment() {
        String env = System.getProperty("app.environment", "development");
        return env.toLowerCase();
    }

    /**
     * Checks if running in development mode.
     */
    public static boolean isDevelopment() {
        return "development".equals(APP_ENVIRONMENT);
    }

    /**
     * Checks if running in production mode.
     */
    public static boolean isProduction() {
        return "production".equals(APP_ENVIRONMENT);
    }

    /**
     * Gets a property with fallback to default.
     */
    public static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
