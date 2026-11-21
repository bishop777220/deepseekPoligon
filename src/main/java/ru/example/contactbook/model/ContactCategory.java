package ru.example.contactbook.model;

import java.util.Locale;

/**
 * Contact groups used for filtering and colour coding in the UI.
 */
public enum ContactCategory {

    WORK("category.work", "info"),
    FAMILY("category.family", "danger"),
    FRIEND("category.friend", "success"),
    OTHER("category.other", "secondary");

    private final String labelKey;
    private final String severity;

    ContactCategory(String labelKey, String severity) {
        this.labelKey = labelKey;
        this.severity = severity;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getSeverity() {
        return severity;
    }

    public String getCssClass() {
        return "badge-" + name().toLowerCase(Locale.ROOT);
    }
}
