package com.sgs.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ApiResponse - Standardized API Response Wrapper
 * Provides consistent response structure across all endpoints.
 * Implements enterprise-grade response formatting.
 * 
 * @author IBM Developer
 * @version 2.0
 */
public class ApiResponse {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String status;
    private String code;
    private String message;
    private Object data;
    private String timestamp;

    // ========== Constructors ==========

    public ApiResponse() {
        this.timestamp = LocalDateTime.now().format(DTF);
    }

    public ApiResponse(String status, String code, String message) {
        this();
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public ApiResponse(String status, String code, String message, Object data) {
        this(status, code, message);
        this.data = data;
    }

    // ========== Static Builders ==========

    /**
     * Creates a success response.
     */
    public static ApiResponse success(String message) {
        return new ApiResponse("success", "SCS_000", message);
    }

    /**
     * Creates a success response with data.
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse("success", "SCS_000", message, data);
    }

    /**
     * Creates an error response.
     */
    public static ApiResponse error(String code, String message) {
        return new ApiResponse("error", code, message);
    }

    /**
     * Creates a validation error response.
     */
    public static ApiResponse validationError(String message) {
        return new ApiResponse("error", "VAL_001", message);
    }

    /**
     * Creates an authentication error response.
     */
    public static ApiResponse authError(String message) {
        return new ApiResponse("error", "AUTH_001", message);
    }

    // ========== Getters & Setters ==========

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getTimestamp() { return timestamp; }

    // ========== Serialization ==========

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"status\":\"").append(escape(status)).append("\",");
        sb.append("\"code\":\"").append(escape(code)).append("\",");
        sb.append("\"message\":\"").append(escape(message)).append("\",");
        if (data != null) {
            sb.append("\"data\":").append(data).append(",");
        }
        sb.append("\"timestamp\":\"").append(timestamp).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
