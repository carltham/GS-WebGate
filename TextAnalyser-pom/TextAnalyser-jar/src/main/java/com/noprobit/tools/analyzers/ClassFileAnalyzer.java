package com.noprobit.tools.analyzers;

import com.noprobit.tools.linting.JavaMethodOrderLinter;
import com.noprobit.tools.encoding.AdvancedEncodingEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassFileAnalyzer {

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "public\\s+(?:abstract\\s+)?(?:final\\s+)?class\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
    private static final Pattern EXTENDS_CLASS_PATTERN = Pattern.compile(
            "public\\s+class\\s+[A-Za-z_$][A-Za-z0-9_$]*\\s+extends\\s+([A-Za-z_$.]+)");
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile(
            "^package\\s+([a-zA-Z_$][a-zA-Z0-9_$.]*);");
    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile(
            "(?:public|private|protected)?\\s*(?:static\\s+)?(?:final\\s+)?(?:synchronized\\s+)?\\w+\\s+([a-z_$][a-zA-Z0-9_$]*)\\s*\\(");
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
            "^import\\s+(?:static\\s+)?[a-zA-Z_$][a-zA-Z0-9_$.]*(?:\\*)?;\\s*$", Pattern.MULTILINE);

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

    public List<String> extractMethodNames(String content) {
        List<String> methods = new ArrayList<>();
        Matcher matcher = METHOD_NAME_PATTERN.matcher(content);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (!methods.contains(methodName)) {
                methods.add(methodName);
            }
        }
        return methods;
    }

    public List<String> extractImports(String content) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            imports.add(matcher.group().trim());
        }
        return imports;
    }

    public String readFileWithEncodingDetection(Path filePath) throws IOException {
        return AdvancedEncodingEngine.readFileWithEncodingDetection(filePath);
    }

    public List<JavaMethodOrderLinter.MethodMetadata> extractMethodMetadata(String className, String content) {
        List<JavaMethodOrderLinter.MethodMetadata> methods = new ArrayList<>();
        Pattern methodPattern = Pattern.compile(
            "(?:public|private|protected)?\\s*(?:static\\s+)?(?:final\\s+)?(?:synchronized\\s+)?(?:\\w+\\s+)?([a-z_$][a-zA-Z0-9_$]*)\\s*\\("
        );

        Matcher matcher = methodPattern.matcher(content);
        while (matcher.find()) {
            String methodName = matcher.group(1);

            int startPos = matcher.start();
            String signature = content.substring(Math.max(0, startPos - 100), matcher.end());

            JavaMethodOrderLinter.Visibility visibility = JavaMethodOrderLinter.Visibility.PACKAGE_PRIVATE;
            if (signature.contains("public")) {
                visibility = JavaMethodOrderLinter.Visibility.PUBLIC;
            } else if (signature.contains("protected")) {
                visibility = JavaMethodOrderLinter.Visibility.PROTECTED;
            } else if (signature.contains("private")) {
                visibility = JavaMethodOrderLinter.Visibility.PRIVATE;
            }

            boolean isConstructor = methodName.equals(className);
            boolean isGetterOrSetter = methodName.startsWith("get") || methodName.startsWith("set") ||
                                      methodName.startsWith("is") || methodName.startsWith("has");

            JavaMethodOrderLinter.MethodMetadata metadata = new JavaMethodOrderLinter.MethodMetadata(
                methodName, visibility, isConstructor, isGetterOrSetter
            );
            methods.add(metadata);
        }

        return methods;
    }
}
