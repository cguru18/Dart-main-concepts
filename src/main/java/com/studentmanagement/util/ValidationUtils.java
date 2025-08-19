package com.studentmanagement.util;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 */
public class ValidationUtils {
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Admission number pattern (6-20 alphanumeric characters)
    private static final Pattern ADMISSION_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z0-9]{6,20}$"
    );
    
    // Phone number pattern (Kenyan format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+254|254|0)?[17]\\d{8}$"
    );
    
    // Name pattern (letters, spaces, hyphens, apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s'-]{2,50}$"
    );

    /**
     * Validate email address format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate admission number format
     */
    public static boolean isValidAdmissionNumber(String admissionNumber) {
        return admissionNumber != null && 
               ADMISSION_NUMBER_PATTERN.matcher(admissionNumber.trim().toUpperCase()).matches();
    }

    /**
     * Validate phone number format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    /**
     * Validate name format
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }

    /**
     * Get password strength description
     */
    public static String getPasswordStrengthDescription(String password) {
        if (password == null || password.isEmpty()) {
            return "Password required";
        }
        
        if (password.length() < 8) {
            return "Password too short (minimum 8 characters)";
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        
        if (!hasLetter) {
            return "Password must contain letters";
        }
        
        if (!hasDigit) {
            return "Password must contain numbers";
        }
        
        int strength = 0;
        if (hasUpper) strength++;
        if (hasLower) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        if (password.length() >= 12) strength++;
        
        switch (strength) {
            case 0:
            case 1:
            case 2:
                return "Weak password";
            case 3:
                return "Medium password";
            case 4:
                return "Strong password";
            case 5:
                return "Very strong password";
            default:
                return "Valid password";
        }
    }

    /**
     * Sanitize string input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        return input.trim()
                   .replaceAll("[<>\"'&]", "") // Remove potential XSS characters
                   .replaceAll("\\s+", " ");   // Normalize whitespace
    }

    /**
     * Validate marks/score input
     */
    public static boolean isValidMarks(String marksText, int maxMarks) {
        try {
            double marks = Double.parseDouble(marksText.trim());
            return marks >= 0 && marks <= maxMarks;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Format admission number consistently
     */
    public static String formatAdmissionNumber(String admissionNumber) {
        if (admissionNumber == null) {
            return "";
        }
        return admissionNumber.trim().toUpperCase();
    }

    /**
     * Format email consistently
     */
    public static String formatEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    /**
     * Format name consistently
     */
    public static String formatName(String name) {
        if (name == null) {
            return "";
        }
        
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        
        // Capitalize first letter of each word
        String[] words = trimmed.toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            
            if (!words[i].isEmpty()) {
                formatted.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    formatted.append(words[i].substring(1));
                }
            }
        }
        
        return formatted.toString();
    }

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}