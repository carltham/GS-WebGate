package com.noprobit.analyzers.reporters;

public class ClassNameSuggester {

    private static final String PANEL = "Panel";
    private static final String DIALOG = "Dialog";
    private static final String EDITOR = "Editor";
    private static final String SCREEN = "Screen";
    private static final String PANEL_LOWER = "panel";
    private static final String DIALOG_LOWER = "dialog";
    private static final String EDITOR_LOWER = "editor";
    private static final String SCREEN_LOWER = "screen";

    public SuggestionResult suggest(String currentName, String extendsClass) {
        SuggestionResult result = new SuggestionResult();
        result.currentName = currentName;
        result.extendsClass = extendsClass;

        String entity = extractEntity(currentName);
        String componentType = determineComponentType(currentName, extendsClass);

        result.suggestedName = buildSuggestedName(entity, componentType);
        result.reason = generateReason(currentName, componentType, extendsClass);

        return result;
    }

    private String determineComponentType(String currentName, String extendsClass) {
        String lower = currentName.toLowerCase();

        if (extendsClass != null) {
            String extendsLower = extendsClass.toLowerCase();

            if (extendsLower.contains("editorpanel")) return PANEL;
            if (extendsLower.contains("editorrecord")) return EDITOR;
            if (extendsLower.contains(DIALOG_LOWER)) return DIALOG;
            if (extendsLower.contains(PANEL_LOWER)) return PANEL;
            if (extendsLower.contains(EDITOR_LOWER)) return EDITOR;
            if (extendsLower.contains(SCREEN_LOWER)) return SCREEN;
        }

        if (lower.contains(PANEL_LOWER)) return PANEL;
        if (lower.contains(EDITOR_LOWER)) return EDITOR;
        if (lower.contains(DIALOG_LOWER)) return DIALOG;
        if (lower.contains(SCREEN_LOWER)) return SCREEN;

        return "Component";
    }

    private String extractEntity(String className) {
        String lower = className.toLowerCase();

        if (lower.contains("category")) return "Category";
        if (lower.contains("product")) return "Product";
        if (lower.contains("cashier")) return "Cashier";
        if (lower.contains("resource")) return "Resource";
        if (lower.contains("people")) return "People";
        if (lower.contains("supplier")) return "Supplier";
        if (lower.contains("tax")) return "Tax";
        if (lower.contains("printer")) return "Printer";
        if (lower.contains("layer")) return "LayerHandling";
        if (lower.contains("register")) return "Register";
        if (lower.contains("adder")) return "Adder";
        if (lower.contains("notregistred")) return "NotRegistered";
        if (lower.contains("cash")) return "Cash";
        if (lower.contains("null")) return "Null";

        return className;
    }

    private String buildSuggestedName(String entity, String componentType) {
        if (entity == null || entity.isEmpty()) {
            return componentType;
        }

        if ("Component".equals(componentType)) {
            return normalizeName(entity);
        }

        return normalizeName(entity + componentType);
    }

    private String generateReason(String currentName, String componentType, String extendsClass) {
        String entity = extractEntity(currentName);
        String currentLower = currentName.toLowerCase();

        StringBuilder reason = new StringBuilder();
        reason.append("Rename '").append(currentName).append("' to follow PascalCase convention. ");

        if (extendsClass != null) {
            reason.append("Extends ").append(extendsClass).append(" - ");
        }

        reason.append("identified as ").append(componentType).append(" for ").append(entity);

        return reason.toString();
    }

    private String normalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String withSpaces = name.replaceAll("[_-]+", " ")
                                 .replaceAll("([a-z])([A-Z])", "$1 $2");
        String[] parts = withSpaces.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    public static class SuggestionResult {
        public String currentName;
        public String suggestedName;
        public String extendsClass;
        public String reason;

        @Override
        public String toString() {
            return String.format("%-30s → %-30s (%s)", currentName, suggestedName, reason);
        }
    }
}
