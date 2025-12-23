package com.matisense.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for input validation
 * Implements validation methods for various data types
 */
public class ValidationUtil {
    
    // Email validation pattern
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    // Phone number pattern (allows various formats)
    private static final String PHONE_PATTERN = 
        "^[+]?[0-9]{10,15}$";
    
    // Username pattern (alphanumeric and underscore, 3-20 chars)
    private static final String USERNAME_PATTERN = 
        "^[a-zA-Z0-9_]{3,20}$";
    
    /**
     * Validate email address
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
    /**
     * Validate phone number
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Remove spaces, dashes, parentheses
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(cleanPhone);
        return matcher.matches();
    }
    
    /**
     * Validate username
     * @param username Username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if valid (min 6 chars), false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }
    
    /**
     * Validate that a field is not empty
     * @param value Value to check
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate date format (YYYY-MM-DD)
     * @param date Date string to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }
    
    /**
     * Validate gender selection
     * @param gender Gender to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return false;
        }
        return gender.equals("Male") || gender.equals("Female") || gender.equals("Other");
    }
    
    /**
     * Validate report status
     * @param status Status to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidReportStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return status.equals("Pending") ||
               status.equals("In Review") ||
               status.equals("Approved") ||
               status.equals("In Progress") ||
               status.equals("Resolved") ||
               status.equals("Completed");
    }
    
    /**
     * Validate report type
     * @param type Report type to validate
     * @return true if not empty, false otherwise
     */
    public static boolean isValidReportType(String type) {
        return isNotEmpty(type);
    }
    
    /**
     * Validate text length
     * @param text Text to validate
     * @param maxLength Maximum allowed length
     * @return true if within limit, false otherwise
     */
    public static boolean isValidLength(String text, int maxLength) {
        if (text == null) {
            return false;
        }
        return text.length() <= maxLength;
    }
}
