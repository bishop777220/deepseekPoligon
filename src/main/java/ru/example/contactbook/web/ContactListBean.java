package ru.example.contactbook.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.example.contactbook.model.Contact;
import ru.example.contactbook.model.ContactCategory;
import ru.example.contactbook.service.ContactService;

import java.io.Serializable;
import java.util.List;

/**
 * Backing bean for the searchable, paginated contact list.
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
    private int page = 1;
    private List<Contact> contacts = List.of();
    private long total;

    @PostConstruct
    void initialize() {
        load();
    }

    public void load() {
        if (page < 1) {
            page = 1;
        }
        long count = contactService.count(search, category);
        long pages = pageCount(count);
        if (page > pages) {
            page = (int) pages;
        }
        total = count;
        contacts = contactService.findPage(search, category, (page - 1) * PAGE_SIZE, PAGE_SIZE);
    }

    public void search() {
        page = 1;
        load();
    }

    public void reset() {
        search = "";
        category = null;
        page = 1;
        load();
    }

    public void delete(Contact contact) {
        if (contact == null || contact.getId() == null) {
            return;
        }
        if (contactService.delete(contact.getId())) {
            Messages.addFlashInfo("msg.deleted");
            if (contacts.size() == 1 && page > 1) {
                page--;
            }
        } else {
            Messages.addFlashError("msg.notFound");
        }
        load();
    }

    public void next() {
        if (isHasNext()) {
            page++;
            load();
        }
    }

    public void previous() {
        if (isHasPrevious()) {
            page--;
            load();
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

    public int getPage() {
        return page;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public long getTotal() {
        return total;
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public long getPageCount() {
        return pageCount(total);
    }

    public ContactCategory[] getCategories() {
        return ContactCategory.values();
    }

    public boolean isHasNext() {
        return page < getPageCount();
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public long getFrom() {
        return total == 0 ? 0 : (long) (page - 1) * PAGE_SIZE + 1;
    }

    public long getTo() {
        return Math.min((long) page * PAGE_SIZE, total);
    }

    private static long pageCount(long count) {
        return Math.max(1, (count + PAGE_SIZE - 1) / PAGE_SIZE);
    }
}
