package ru.example.contactbook.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.example.contactbook.model.Contact;
import ru.example.contactbook.model.ContactCategory;
import ru.example.contactbook.service.ContactService;
import ru.example.contactbook.service.DuplicateEmailException;

import java.io.Serializable;

/**
 * Backing bean for the create/edit form.
 */
@Named("contactEditBean")
@ViewScoped
public class ContactEditBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ContactService contactService;

    private Long id;
    private Contact contact;

    @PostConstruct
    void initialize() {
        // The view calls init() through <f:viewAction> once the view parameter is applied.
    }

    /**
     * Loads an existing contact or prepares a blank one. Invoked from {@code <f:viewAction>}.
     *
     * @return navigation outcome, or {@code null} to stay on the page
     */
    public String init() {
        if (FacesContext.getCurrentInstance().isValidationFailed()) {
            return "index?faces-redirect=true";
        }
        if (id == null) {
            contact = new Contact();
            return null;
        }
        contact = contactService.findById(id).orElse(null);
        if (contact == null) {
            Messages.addFlashError("msg.notFound");
            return "index?faces-redirect=true";
        }
        return null;
    }

    public String save() {
        if (contact == null) {
            return "index?faces-redirect=true";
        }
        boolean creating = contact.getId() == null;
        try {
            contact = contactService.save(contact);
        } catch (DuplicateEmailException exception) {
            Messages.addError("editForm:email", "validation.email.duplicate");
            return null;
        }
        Messages.addFlashInfo(creating ? "msg.created" : "msg.updated");
        return "index?faces-redirect=true";
    }

    public String cancel() {
        return "index?faces-redirect=true";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contact getContact() {
        return contact;
    }

    public ContactCategory[] getCategories() {
        return ContactCategory.values();
    }
}
