package ru.example.contactbook.config;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Resolves database settings from JVM system properties or environment variables.
 *
 * <p>Precedence (highest first):</p>
 * <ol>
 *   <li>{@code -Dcontactbook.db.url=...} / {@code -Dcontactbook.db.user=...} / {@code -Dcontactbook.db.password=...}</li>
 *   <li>{@code CONTACTBOOK_DB_URL} / {@code CONTACTBOOK_DB_USER} / {@code CONTACTBOOK_DB_PASSWORD}</li>
 *   <li>Defaults declared in {@code META-INF/persistence.xml}</li>
 * </ol>
 */
public final class DatabaseConfig {

    public static final String PROP_URL = "contactbook.db.url";
    public static final String PROP_USER = "contactbook.db.user";
    public static final String PROP_PASSWORD = "contactbook.db.password";

    public static final String ENV_URL = "CONTACTBOOK_DB_URL";
    public static final String ENV_USER = "CONTACTBOOK_DB_USER";
    public static final String ENV_PASSWORD = "CONTACTBOOK_DB_PASSWORD";

    private static final Logger LOG = Logger.getLogger(DatabaseConfig.class.getName());

    private DatabaseConfig() {
    }

    /**
     * Builds JPA property overrides for the {@code contactBookPU} persistence unit.
     */
    public static Map<String, String> overrides() {
        Map<String, String> properties = new HashMap<>();
        apply(properties, "jakarta.persistence.jdbc.url", PROP_URL, ENV_URL, false);
        apply(properties, "jakarta.persistence.jdbc.user", PROP_USER, ENV_USER, true);
        apply(properties, "jakarta.persistence.jdbc.password", PROP_PASSWORD, ENV_PASSWORD, false);
        return properties;
    }

    private static void apply(Map<String, String> target, String jpaProperty,
                              String systemProperty, String environmentVariable,
                              boolean logValue) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        if (value != null && !value.isBlank()) {
            target.put(jpaProperty, value);
            if (logValue) {
                LOG.info("Using " + jpaProperty + "=" + truncate(value));
            }
        }
    }

    private static String truncate(String value) {
        return value.length() <= 3 ? "***" : "<configured>";
    }
}
