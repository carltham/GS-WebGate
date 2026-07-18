package com.noprobit.tools.linting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JavaImportLinter {

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

    public interface ImportRule {
        List<LintIssue> check(List<String> imports);
    }

    private final List<ImportRule> activeRules = new ArrayList<>();

    public JavaImportLinter() {
        activeRules.add(new NoWildcardImportRule());
        activeRules.add(new DuplicateImportRule());
        activeRules.add(new ForbiddenPackageRule());
        activeRules.add(new ImportGroupingRule());
    }

    public List<LintIssue> analyze(List<String> imports) {
        List<LintIssue> issues = new ArrayList<>();
        if (imports == null || imports.isEmpty()) {
            return issues;
        }

        List<String> sanitizedImports = new ArrayList<>();
        for (String imp : imports) {
            String trimmed = imp.trim();
            if (!trimmed.isEmpty()) {
                sanitizedImports.add(trimmed);
            }
        }

        for (ImportRule rule : activeRules) {
            issues.addAll(rule.check(sanitizedImports));
        }
        return issues;
    }

    private static class NoWildcardImportRule implements ImportRule {
        @Override
        public List<LintIssue> check(List<String> imports) {
            List<LintIssue> issues = new ArrayList<>();
            for (String imp : imports) {
                if (imp.contains("*")) {
                    issues.add(new LintIssue("WILDCARD_IMPORT", Severity.WARNING,
                        String.format("Avoid wildcard imports: '%s'. Explicitly list class imports.", imp)));
                }
            }
            return issues;
        }
    }

    private static class DuplicateImportRule implements ImportRule {
        @Override
        public List<LintIssue> check(List<String> imports) {
            List<LintIssue> issues = new ArrayList<>();
            Set<String> uniqueImports = new HashSet<>();

            for (String imp : imports) {
                if (!uniqueImports.add(imp)) {
                    issues.add(new LintIssue("DUPLICATE_IMPORT", Severity.WARNING,
                        String.format("Duplicate import statement found: '%s'.", imp)));
                }
            }
            return issues;
        }
    }

    private static class ForbiddenPackageRule implements ImportRule {
        private static final Pattern FORBIDDEN_PATTERN = Pattern.compile("^import\\s+(sun\\.|com\\.sun\\.|com\\.internal\\.).*");

        @Override
        public List<LintIssue> check(List<String> imports) {
            List<LintIssue> issues = new ArrayList<>();
            for (String imp : imports) {
                if (FORBIDDEN_PATTERN.matcher(imp).matches()) {
                    issues.add(new LintIssue("FORBIDDEN_PACKAGE", Severity.ERROR,
                        String.format("Importing internal compiler packages is unsafe: '%s'.", imp)));
                }
            }
            return issues;
        }
    }

    private static class ImportGroupingRule implements ImportRule {
        @Override
        public List<LintIssue> check(List<String> imports) {
            List<LintIssue> issues = new ArrayList<>();
            boolean encounteredNonStatic = false;

            for (String imp : imports) {
                boolean isStatic = imp.startsWith("import static");

                if (!isStatic) {
                    encounteredNonStatic = true;
                } else if (encounteredNonStatic) {
                    issues.add(new LintIssue("BAD_IMPORT_ORDER", Severity.WARNING,
                        "Static imports should be grouped together at the very top of the import block."));
                    break;
                }
            }
            return issues;
        }
    }
}
