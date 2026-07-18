package com.noprobit.tools.linting;

import java.util.ArrayList;
import java.util.List;

public class JavaMethodOrderLinter {

    public enum Severity { ERROR, WARNING }
    public enum Visibility { PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE }

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

    public static class MethodMetadata {
        public final String name;
        public final Visibility visibility;
        public final boolean isConstructor;
        public final boolean isGetterOrSetter;

        public MethodMetadata(String name, Visibility visibility, boolean isConstructor, boolean isGetterOrSetter) {
            this.name = name;
            this.visibility = visibility;
            this.isConstructor = isConstructor;
            this.isGetterOrSetter = isGetterOrSetter;
        }
    }

    public interface OrderRule {
        List<LintIssue> check(List<MethodMetadata> methods);
    }

    private final List<OrderRule> activeRules = new ArrayList<>();

    public JavaMethodOrderLinter() {
        activeRules.add(new ConstructorPositionRule());
        activeRules.add(new VisibilityStepDownRule());
        activeRules.add(new AccessorPositionRule());
    }

    public List<LintIssue> analyze(List<MethodMetadata> methods) {
        List<LintIssue> issues = new ArrayList<>();
        if (methods == null || methods.size() <= 1) {
            return issues;
        }

        for (OrderRule rule : activeRules) {
            issues.addAll(rule.check(methods));
        }
        return issues;
    }

    private static class ConstructorPositionRule implements OrderRule {
        @Override
        public List<LintIssue> check(List<MethodMetadata> methods) {
            List<LintIssue> issues = new ArrayList<>();
            boolean encounteredStandardMethod = false;

            for (MethodMetadata method : methods) {
                if (method.isConstructor) {
                    if (encounteredStandardMethod) {
                        issues.add(new LintIssue("CONSTRUCTOR_ORDER_VIOLATION", Severity.WARNING,
                            String.format("Constructor '%s' should be placed at the very top of the class.", method.name)));
                    }
                } else {
                    encounteredStandardMethod = true;
                }
            }
            return issues;
        }
    }

    private static class VisibilityStepDownRule implements OrderRule {
        @Override
        public List<LintIssue> check(List<MethodMetadata> methods) {
            List<LintIssue> issues = new ArrayList<>();
            int highestEncounteredOrdinal = 0;

            for (MethodMetadata method : methods) {
                if (method.isGetterOrSetter || method.isConstructor) {
                    continue;
                }

                int currentOrdinal = method.visibility.ordinal();

                if (currentOrdinal < highestEncounteredOrdinal) {
                    issues.add(new LintIssue("VISIBILITY_ORDER_VIOLATION", Severity.WARNING,
                        String.format("Method '%s' (%s) appears after a lower-visibility method. Follow the public -> private sequence.",
                            method.name, method.visibility)));
                    break;
                }
                highestEncounteredOrdinal = currentOrdinal;
            }
            return issues;
        }
    }

    private static class AccessorPositionRule implements OrderRule {
        @Override
        public List<LintIssue> check(List<MethodMetadata> methods) {
            List<LintIssue> issues = new ArrayList<>();
            boolean encounteredStandardMethodAfterAccessor = false;

            for (int i = 0; i < methods.size(); i++) {
                MethodMetadata method = methods.get(i);
                if (method.isGetterOrSetter) {
                    for (int j = i + 1; j < methods.size(); j++) {
                        MethodMetadata remaining = methods.get(j);
                        if (!remaining.isGetterOrSetter && !remaining.isConstructor) {
                            encounteredStandardMethodAfterAccessor = true;
                            break;
                        }
                    }
                }
            }

            if (encounteredStandardMethodAfterAccessor) {
                issues.add(new LintIssue("ACCESSOR_ORDER_VIOLATION", Severity.WARNING,
                    "Boilerplate getters and setters should be grouped cleanly at the bottom of the class block."));
            }
            return issues;
        }
    }
}
