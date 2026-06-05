package com.sgs.validation;

import com.sgs.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Input Validator - Centralized validation following OWASP standards.
 * Provides methods for common validation scenarios.
 */
public class InputValidator {

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final int MAX_STRING_LENGTH = 255;

    private InputValidator() {}

    /**
     * Validates that a value is not null or empty.
     */
    public static void validateRequired(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("VAL_001", fieldName + " is required");
        }
    }

    /**
     * Validates string length boundaries.
     */
    public static void validateLength(String value, int minLength, int maxLength, String fieldName) 
            throws ValidationException {
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ValidationException("VAL_002", 
                    fieldName + " must be between " + minLength + " and " + maxLength + " characters");
        }
    }

    /**
     * Validates that a string contains only alphanumeric and space characters.
     */
    public static void validateAlphanumeric(String value, String fieldName) throws ValidationException {
        if (!ALPHANUMERIC_PATTERN.matcher(value).matches()) {
            throw new ValidationException("VAL_003", 
                    fieldName + " contains invalid characters");
        }
    }

    /**
     * Validates numeric range.
     */
    public static void validateRange(double value, double min, double max, String fieldName) 
            throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException("VAL_004", 
                    fieldName + " must be between " + min + " and " + max);
        }
    }

    /**
     * Validates email format.
     */
    public static void validateEmail(String value, String fieldName) throws ValidationException {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new ValidationException("VAL_005", fieldName + " has invalid email format");
        }
    }

    /**
     * Validates username format.
     */
    public static void validateUsername(String value, String fieldName) throws ValidationException {
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new ValidationException("VAL_006", 
                    fieldName + " must be 3-20 characters (alphanumeric, dash, underscore only)");
        }
    }

    /**
     * Sanitizes string input to prevent injection attacks.
     */
    public static String sanitize(String value) {
        if (value == null) return null;
        return value.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Validates and sanitizes student name.
     */
    public static String validateStudentName(String name) throws ValidationException {
        validateRequired(name, "Student name");
        String sanitized = sanitize(name);
        validateLength(sanitized, 2, MAX_STRING_LENGTH, "Student name");
        validateAlphanumeric(sanitized, "Student name");
        return sanitized;
    }

    /**
     * Validates credentials structure.
     */
    public static void validateCredentials(String username, String password) throws ValidationException {
        validateRequired(username, "Username");
        validateRequired(password, "Password");
        validateUsername(username, "Username");
        validateLength(password, 6, MAX_STRING_LENGTH, "Password");
    }
}
