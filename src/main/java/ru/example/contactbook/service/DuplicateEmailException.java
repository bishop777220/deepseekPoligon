package ru.example.contactbook.service;

/**
 * Raised when a contact e-mail is already used by another entry.
 */
public class DuplicateEmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String email;

    public DuplicateEmailException(String email) {
        super("A contact with e-mail '" + email + "' already exists");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
