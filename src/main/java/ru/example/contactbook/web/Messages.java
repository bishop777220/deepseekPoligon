package ru.example.contactbook.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Small helper for localized JSF messages and flash-message workflows.
 */
public final class Messages {

    private static final String BUNDLE = "messages";

    private Messages() {
    }

    public static void addFlashInfo(String key, Object... parameters) {
        addFlash(FacesMessage.SEVERITY_INFO, key, parameters);
    }

    public static void addFlashError(String key, Object... parameters) {
        addFlash(FacesMessage.SEVERITY_ERROR, key, parameters);
    }

    public static void addError(String clientId, String key, Object... parameters) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }
        context.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, text(key, parameters), null));
    }

    public static void addError(String key, Object... parameters) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, text(key, parameters), null));
    }

    public static String text(String key, Object... parameters) {
        FacesContext context = FacesContext.getCurrentInstance();
        Locale locale = context != null && context.getViewRoot() != null
                ? context.getViewRoot().getLocale()
                : Locale.getDefault();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);
            String value = bundle.getString(key);
            return parameters.length == 0 ? value : MessageFormat.format(value, parameters);
        } catch (MissingResourceException exception) {
            return key;
        }
    }

    private static void addFlash(FacesMessage.Severity severity, String key, Object... parameters) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, new FacesMessage(severity, text(key, parameters), null));
    }
}
