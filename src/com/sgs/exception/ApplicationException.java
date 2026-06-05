package com.sgs.exception;

/**
 * Application Base Exception - All business exceptions extend this.
 * Provides consistent error codes and HTTP status mapping.
 */
public class ApplicationException extends Exception {

    private final String errorCode;
    private final int httpStatus;

    public ApplicationException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ApplicationException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
