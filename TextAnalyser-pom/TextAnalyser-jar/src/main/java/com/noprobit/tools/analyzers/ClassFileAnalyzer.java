package com.noprobit.tools.analyzers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassFileAnalyzer {

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "public\\s+(?:abstract\\s+)?(?:final\\s+)?class\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
    private static final Pattern EXTENDS_CLASS_PATTERN = Pattern.compile(
            "public\\s+class\\s+[A-Za-z_$][A-Za-z0-9_$]*\\s+extends\\s+([A-Za-z_$.]+)");
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile(
            "^package\\s+([a-zA-Z_$][a-zA-Z0-9_$.]*);");

    public String extractClassName(String content) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String extractExtendsClass(String content) {
        Matcher matcher = EXTENDS_CLASS_PATTERN.matcher(content);
        if (matcher.find()) {
            String full = matcher.group(1);
            return full.substring(full.lastIndexOf('.') + 1);
        }
        return null;
    }

    public String extractPackageName(String content) {
        Matcher matcher = PACKAGE_NAME_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String getFullyQualifiedName(String packageName, String className) {
        if (packageName != null && !packageName.isEmpty()) {
            return packageName + "." + className;
        }
        return className;
    }
}
