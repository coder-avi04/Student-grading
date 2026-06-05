package com.sgs.util;

/**
 * JSON response builder helper.
 */
public class JsonUtil {

    public static String success(String message) {
        return "{\"status\":\"success\",\"message\":\"" + escape(message) + "\"}";
    }

    public static String success(String message, String data) {
        return "{\"status\":\"success\",\"message\":\"" + escape(message) + "\",\"data\":" + data + "}";
    }

    public static String error(String message) {
        return "{\"status\":\"error\",\"message\":\"" + escape(message) + "\"}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
