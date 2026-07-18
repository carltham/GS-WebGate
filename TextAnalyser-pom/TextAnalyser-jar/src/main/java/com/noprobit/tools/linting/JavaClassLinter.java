package com.noprobit.tools.linting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;

public class JavaClassLinter {

    public enum Severity { ERROR, WARNING }

    public static class LintIssue {
        public final String ruleId;
        public final Severity severity;
        public final String message;

        public LintIssue(String ruleId, Severity severity, String message) {
            this.ruleId = ruleId;
            this.severity = severity;
            this.message = message;
        }
    }

    public interface LintRule {
        List<LintIssue> check(String className);
    }

    private final List<LintRule> activeRules = new ArrayList<>();

    public JavaClassLinter() {
        activeRules.add(new CompilerRule());
        activeRules.add(new PascalCaseRule());
        activeRules.add(new SpecialCharacterRule());
        activeRules.add(new LengthRule());
    }

    public List<LintIssue> analyze(String className) {
        List<LintIssue> issues = new ArrayList<>();
        if (className == null || className.isBlank()) {
            issues.add(new LintIssue("EMPTY_NAME", Severity.ERROR, "Class name cannot be empty."));
            return issues;
        }

        for (LintRule rule : activeRules) {
            issues.addAll(rule.check(className));
        }
        return issues;
    }

    private static class CompilerRule implements LintRule {
        @Override
        public List<LintIssue> check(String className) {
            List<LintIssue> issues = new ArrayList<>();
            if (!SourceVersion.isIdentifier(className)) {
                issues.add(new LintIssue("INVALID_IDENTIFIER", Severity.ERROR,
                    "Class name is not a valid Java identifier (e.g., cannot start with a number)."));
            }
            if (SourceVersion.isKeyword(className)) {
                issues.add(new LintIssue("RESERVED_KEYWORD", Severity.ERROR,
                    String.format("Class name cannot use the reserved keyword '%s'.", className)));
            }
            return issues;
        }
    }

    private static class PascalCaseRule implements LintRule {
        @Override
        public List<LintIssue> check(String className) {
            List<LintIssue> issues = new ArrayList<>();
            if (!Character.isUpperCase(className.charAt(0))) {
                issues.add(new LintIssue("PASCAL_CASE_VIOLATION", Severity.WARNING,
                    "Class name should start with an uppercase character."));
            }
            if (Pattern.compile("[A-Z]{3,}").matcher(className).find()) {
                issues.add(new LintIssue("ACRONYM_VIOLATION", Severity.WARNING,
                    "Avoid consecutive uppercase acronyms; use 'XmlParser' instead of 'XMLParser'."));
            }
            return issues;
        }
    }

    private static class SpecialCharacterRule implements LintRule {
        @Override
        public List<LintIssue> check(String className) {
            List<LintIssue> issues = new ArrayList<>();
            if (className.contains("_")) {
                issues.add(new LintIssue("SNAKE_CASE_DISCOURAGED", Severity.WARNING,
                    "Class name contains underscores. Use PascalCase instead."));
            }
            if (className.contains("$")) {
                issues.add(new LintIssue("DOLLAR_SIGN_DISCOURAGED", Severity.WARNING,
                    "Dollar signs should only be used for compiler-generated or inner classes."));
            }
            return issues;
        }
    }

    private static class LengthRule implements LintRule {
        @Override
        public List<LintIssue> check(String className) {
            List<LintIssue> issues = new ArrayList<>();
            if (className.length() < 3) {
                issues.add(new LintIssue("NAME_TOO_SHORT", Severity.WARNING,
                    "Class name is short; ensure it is descriptive enough."));
            }
            if (className.length() > 50) {
                issues.add(new LintIssue("NAME_TOO_LONG", Severity.WARNING,
                    "Class name exceeds 50 characters. Consider refactoring to simplify design."));
            }
            return issues;
        }
    }
}
