package com.noprobit.tools.linting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;

public class JavaMethodLinter {

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
        List<LintIssue> check(String methodName);
    }

    private final List<LintRule> activeRules = new ArrayList<>();

    public JavaMethodLinter() {
        activeRules.add(new CompilerRule());
        activeRules.add(new CamelCaseRule());
        activeRules.add(new SpecialCharacterRule());
        activeRules.add(new PrefixVerbRule());
    }

    public List<LintIssue> analyze(String methodName) {
        List<LintIssue> issues = new ArrayList<>();
        if (methodName == null || methodName.isBlank()) {
            issues.add(new LintIssue("EMPTY_NAME", Severity.ERROR, "Method name cannot be empty."));
            return issues;
        }

        for (LintRule rule : activeRules) {
            issues.addAll(rule.check(methodName));
        }
        return issues;
    }

    private static class CompilerRule implements LintRule {
        @Override
        public List<LintIssue> check(String methodName) {
            List<LintIssue> issues = new ArrayList<>();
            if (!SourceVersion.isIdentifier(methodName)) {
                issues.add(new LintIssue("INVALID_IDENTIFIER", Severity.ERROR,
                    "Method name is not a valid Java identifier."));
            }
            if (SourceVersion.isKeyword(methodName)) {
                issues.add(new LintIssue("RESERVED_KEYWORD", Severity.ERROR,
                    String.format("Method name cannot use the reserved keyword '%s'.", methodName)));
            }
            return issues;
        }
    }

    private static class CamelCaseRule implements LintRule {
        @Override
        public List<LintIssue> check(String methodName) {
            List<LintIssue> issues = new ArrayList<>();
            if (!Character.isLowerCase(methodName.charAt(0))) {
                issues.add(new LintIssue("CAMEL_CASE_VIOLATION", Severity.WARNING,
                    "Method name should start with a lowercase character (camelCase)."));
            }
            if (Pattern.compile("[A-Z]{3,}").matcher(methodName).find()) {
                issues.add(new LintIssue("ACRONYM_VIOLATION", Severity.WARNING,
                    "Avoid consecutive uppercase acronyms; use 'parseXml' instead of 'parseXML'."));
            }
            return issues;
        }
    }

    private static class SpecialCharacterRule implements LintRule {
        @Override
        public List<LintIssue> check(String methodName) {
            List<LintIssue> issues = new ArrayList<>();
            if (methodName.contains("_")) {
                issues.add(new LintIssue("SNAKE_CASE_DISCOURAGED", Severity.WARNING,
                    "Method name contains underscores. Use camelCase instead."));
            }
            if (methodName.contains("$")) {
                issues.add(new LintIssue("DOLLAR_SIGN_DISCOURAGED", Severity.WARNING,
                    "Method name contains dollar signs, which are discouraged in source code."));
            }
            return issues;
        }
    }

    private static class PrefixVerbRule implements LintRule {
        private static final Pattern VERB_PREFIX_PATTERN = Pattern.compile(
            "^(get|set|is|has|can|should|will|do|execute|process|parse|validate|create|delete|update|remove|add|find|read|write|open|close|to|as)[A-Z0-9].*$"
        );

        @Override
        public List<LintIssue> check(String methodName) {
            List<LintIssue> issues = new ArrayList<>();

            if (methodName.matches("^(run|stop|start|execute|print|clear|flush|close|open)$")) {
                return issues;
            }

            if (!VERB_PREFIX_PATTERN.matcher(methodName).matches()) {
                issues.add(new LintIssue("POOR_VERB_USAGE", Severity.WARNING,
                    "Method name does not appear to start with a standard verb or prefix (e.g., get, set, calculate, parse)."));
            }
            return issues;
        }
    }
}
