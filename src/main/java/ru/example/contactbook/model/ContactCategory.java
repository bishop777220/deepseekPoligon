package ru.example.contactbook.model;

import java.util.Locale;

/**
 * Contact groups used for filtering and colour coding in the UI.
 */
public enum ContactCategory {

    WORK("category.work"),
    FAMILY("category.family"),
    FRIEND("category.friend"),
    OTHER("category.other");

    private final String labelKey;

    ContactCategory(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getCssClass() {
        return "badge-" + name().toLowerCase(Locale.ROOT);
    }
}
