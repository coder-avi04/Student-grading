package com.sgs.exception;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Authentication Exception - For security-related errors.
 */
public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message, HttpServletResponse.SC_UNAUTHORIZED);
    }
}
