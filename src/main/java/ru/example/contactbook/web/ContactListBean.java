package ru.example.contactbook.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import ru.example.contactbook.model.Contact;
import ru.example.contactbook.model.ContactCategory;
import ru.example.contactbook.service.ContactService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for the searchable contact list.
 *
 * <p>The table is backed by a PrimeFaces {@link LazyDataModel}: only the rows
 * of the current page are read from the database.</p>
 */
@Named("contactListBean")
@ViewScoped
public class ContactListBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;

    @Inject
    private ContactService contactService;

    private String search = "";
    private ContactCategory category;
    private int first;
    private long total;
    private LazyDataModel<Contact> lazyContacts;

    @PostConstruct
    void initialize() {
        lazyContacts = new LazyDataModel<>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return Math.toIntExact(contactService.count(search, category));
            }

            @Override
            public List<Contact> load(int first, int pageSize,
                                      Map<String, SortMeta> sortBy,
                                      Map<String, FilterMeta> filterBy) {
                setRowCount(count(filterBy));
                return contactService.findPage(search, category, first, pageSize);
            }

            @Override
            public String getRowKey(Contact contact) {
                return contact == null || contact.getId() == null ? null : contact.getId().toString();
            }
        };
        refreshTotal();
    }

    public void search() {
        first = 0;
        resetTablePage();
        refreshTotal();
    }

    public void reset() {
        search = "";
        category = null;
        first = 0;
        resetTablePage();
        refreshTotal();
    }

    public void delete(Contact contact) {
        if (contact == null || contact.getId() == null) {
            return;
        }
        if (contactService.delete(contact.getId())) {
            Messages.addInfo("msg.deleted");
            refreshTotal();
            if (first >= total && first > 0) {
                first = Math.max(0, first - PAGE_SIZE);
            }
        } else {
            Messages.addError("msg.notFound");
        }
    }

    private void refreshTotal() {
        total = contactService.count(search, category);
    }

    private void resetTablePage() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }
        UIComponent component = context.getViewRoot().findComponent("contactsForm:contactsTable");
        if (component instanceof DataTable dataTable) {
            dataTable.setFirst(0);
        }
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public ContactCategory getCategory() {
        return category;
    }

    public void setCategory(ContactCategory category) {
        this.category = category;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = Math.max(0, first);
    }

    public long getTotal() {
        return total;
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public ContactCategory[] getCategories() {
        return ContactCategory.values();
    }

    public int getCategoryCount() {
        return ContactCategory.values().length;
    }

    public LazyDataModel<Contact> getLazyContacts() {
        return lazyContacts;
    }
}
