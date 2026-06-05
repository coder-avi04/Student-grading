package com.sgs.exception;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Validation Exception - For input validation errors.
 */
public class ValidationException extends ApplicationException {
    public ValidationException(String errorCode, String message) {
        super(errorCode, message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
