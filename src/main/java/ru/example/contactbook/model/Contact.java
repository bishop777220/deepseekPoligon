package ru.example.contactbook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single address-book entry.
 */
@Entity
@Table(name = "contacts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_contacts_email", columnNames = "email")
})
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.firstName.required}")
    @Size(max = 80, message = "{validation.firstName.size}")
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @NotBlank(message = "{validation.lastName.required}")
    @Size(max = 80, message = "{validation.lastName.size}")
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 160, message = "{validation.email.size}")
    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Size(max = 40, message = "{validation.phone.size}")
    @Column(name = "phone", length = 40)
    private String phone;

    @Size(max = 120, message = "{validation.organization.size}")
    @Column(name = "organization", length = 120)
    private String organization;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "category", nullable = false, length = 20)
    private ContactCategory category = ContactCategory.OTHER;

    @Size(max = 1000, message = "{validation.notes.size}")
    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (category == null) {
            category = ContactCategory.OTHER;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (category == null) {
            category = ContactCategory.OTHER;
        }
    }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? null : firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName == null ? null : lastName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization == null ? null : organization.trim();
    }

    public ContactCategory getCategory() {
        return category;
    }

    public void setCategory(ContactCategory category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? null : notes.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Contact contact)) {
            return false;
        }
        return id != null && Objects.equals(id, contact.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "Contact{id=" + id + ", email='" + email + "'}";
    }
}
