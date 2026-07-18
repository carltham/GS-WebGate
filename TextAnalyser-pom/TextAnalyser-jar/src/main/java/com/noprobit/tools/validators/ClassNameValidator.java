package com.noprobit.tools.validators;

import java.util.regex.Pattern;

public class ClassNameValidator {

    private static final Pattern PASCAL_CASE = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");

    public boolean isPascalCase(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        return PASCAL_CASE.matcher(className).matches();
    }

    public boolean isValid(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }

        if (!Character.isUpperCase(className.charAt(0))) {
            return false;
        }

        if (className.contains("_")) {
            return false;
        }

        for (char c : className.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }

        return true;
    }

    public ValidationResult validate(String className) {
        ValidationResult result = new ValidationResult();
        result.className = className;

        if (className == null || className.isEmpty()) {
            result.valid = false;
            result.reason = "Class name is null or empty";
            return result;
        }

        if (!Character.isUpperCase(className.charAt(0))) {
            result.valid = false;
            result.reason = "Class name must start with uppercase letter (PascalCase)";
            return result;
        }

        if (className.contains("_")) {
            result.valid = false;
            result.reason = "Class name contains underscore - use PascalCase instead";
            return result;
        }

        for (char c : className.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                result.valid = false;
                result.reason = "Class name contains invalid character: " + c;
                return result;
            }
        }

        result.valid = true;
        result.reason = "Valid PascalCase name";
        return result;
    }

    public static class ValidationResult {
        public String className;
        public boolean valid;
        public String reason;

        @Override
        public String toString() {
            return className + " - " + (valid ? "✓" : "❌") + " " + reason;
        }
    }
}
