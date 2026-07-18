package com.noprobit.tools.analyzers;

import com.noprobit.tools.reporters.ClassNameSuggester;
import com.noprobit.tools.validators.ClassNameValidator;

public class PurposeAnalyser {

    private static final String PANEL_LOWER = "panel";
    private static final String DIALOG_LOWER = "dialog";
    private static final String EDITOR_LOWER = "editor";
    private static final String SCREEN_LOWER = "screen";

    private final ClassNameValidator validator = new ClassNameValidator();
    private final ClassNameSuggester suggester = new ClassNameSuggester();

    public String analyzePurpose(String className, String extendsClass) {
        String lower = className.toLowerCase();

        if (extendsClass != null) {
            String extendsLower = extendsClass.toLowerCase();

            if (extendsLower.contains("editorpanel"))
                return "Management panel for " + extractEntity(className) + " with editor";
            if (extendsLower.contains("editorrecord"))
                return "Editor for " + extractEntity(className) + " records";
            if (extendsLower.contains(PANEL_LOWER))
                return "UI panel/view component";
            if (extendsLower.contains(DIALOG_LOWER))
                return "Modal dialog window";
            if (extendsLower.contains(EDITOR_LOWER))
                return "Editor component";
            if (extendsLower.contains(SCREEN_LOWER))
                return "Screen/display component";
        }

        if (lower.contains(PANEL_LOWER)) return "Panel/View component";
        if (lower.contains(DIALOG_LOWER)) return "Dialog window";
        if (lower.contains(EDITOR_LOWER)) return "Editor component";
        if (lower.contains(SCREEN_LOWER)) return "Screen/Display";
        return "Component/Helper";
    }

    private String extractEntity(String className) {
        String lower = className.toLowerCase();

        if (lower.contains("category")) return "Category";
        if (lower.contains("product")) return "Product";
        if (lower.contains("cashier")) return "Cashier";
        if (lower.contains("resource")) return "Resource";
        if (lower.contains("people")) return "Person";
        if (lower.contains("supplier")) return "Supplier";
        if (lower.contains("tax")) return "Tax";
        if (lower.contains("printer")) return "Printer";
        if (lower.contains("layer")) return "Layer Handling";
        if (lower.contains("register")) return "Registration";

        return className;
    }

    public String suggestName(String currentName, String extendsClass) {
        return suggester.suggest(currentName, extendsClass).suggestedName;
    }

    public boolean isPascalCase(String className) {
        return validator.isPascalCase(className);
    }
}
