package com.matisense.exception;

/**
 * Custom exception for Matisense Community Report System
 * Extends Exception to provide specific error handling for the application
 */
public class MatisenseException extends Exception {
    
    private String errorCode;
    
    /**
     * Constructor with message
     * @param message Error message
     */
    public MatisenseException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message Error message
     * @param cause Original exception
     */
    public MatisenseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor with message and error code
     * @param message Error message
     * @param errorCode Error code for identification
     */
    public MatisenseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructor with message, cause, and error code
     * @param message Error message
     * @param cause Original exception
     * @param errorCode Error code for identification
     */
    public MatisenseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Get error code
     * @return Error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Set error code
     * @param errorCode Error code to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
