package com.sgs.exception;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Business Logic Exception - For service layer errors.
 */
public class BusinessException extends ApplicationException {
    public BusinessException(String errorCode, String message) {
        super(errorCode, message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
