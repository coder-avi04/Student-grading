package com.sgs.util;

import com.sgs.logging.AppLogger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonUtil - Enterprise JSON Response Builder
 * Provides standardized error/success response formats with proper escaping.
 * Implements secure parsing with logging.
 * 
 * @author IBM Developer
 * @version 2.0
 */
public class JsonUtil {

    private static final AppLogger LOGGER = AppLogger.getInstance();
    private static final String UTIL_NAME = "JsonUtil";

    private JsonUtil() {}

    /**
     * Builds standardized success response with message.
     */
    public static String success(String message) {
        return "{\"status\":\"success\",\"message\":\"" + escape(message) + "\"}";
    }

    /**
     * Builds success response with message and data payload.
     */
    public static String success(String message, String data) {
        return "{\"status\":\"success\",\"message\":\"" + escape(message) + "\",\"data\":" + data + "}";
    }

    /**
     * Builds standardized error response with message.
     */
    public static String error(String message) {
        return "{\"status\":\"error\",\"message\":\"" + escape(message) + "\"}";
    }

    /**
     * Builds error response with error code and message.
     */
    public static String error(String errorCode, String message) {
        return "{\"status\":\"error\",\"code\":\"" + escape(errorCode) + "\",\"message\":\"" + 
                escape(message) + "\"}";
    }

    /**
     * JSON escapes string to prevent injection attacks.
     */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Extracts a string field from a JSON string using regex.
     * Handles string values ("field": "value") and numerical/boolean/null values.
     */
    public static String getJsonField(String json, String field) {
        if (json == null || json.isEmpty()) {
            LOGGER.debug(UTIL_NAME, "Attempted to parse null or empty JSON for field: %s", field);
            return null;
        }
        
        try {
            // Match string: "field" : "value"
            String stringPattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
            Pattern pattern = Pattern.compile(stringPattern);
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                String value = matcher.group(1);
                LOGGER.debug(UTIL_NAME, "Extracted field %s from JSON", field);
                return value;
            }
            
            // Match number/boolean/null: "field" : value
            String rawPattern = "\"" + field + "\"\\s*:\\s*([^,}\\s]*)";
            pattern = Pattern.compile(rawPattern);
            matcher = pattern.matcher(json);
            if (matcher.find()) {
                String val = matcher.group(1).trim();
                if ("null".equals(val)) return null;
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                return val;
            }
            
            LOGGER.debug(UTIL_NAME, "Field not found in JSON: %s", field);
            return null;
        } catch (Exception e) {
            LOGGER.error(UTIL_NAME, "Error parsing JSON field %s", e, field);
            return null;
        }
    }
}
