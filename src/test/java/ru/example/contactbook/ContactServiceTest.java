package ru.example.contactbook;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.example.contactbook.model.Contact;
import ru.example.contactbook.model.ContactCategory;
import ru.example.contactbook.service.ContactService;
import ru.example.contactbook.service.DuplicateEmailException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactServiceTest {

    private static EntityManagerFactory entityManagerFactory;
    private static ContactService service;

    @BeforeAll
    static void startPersistence() {
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("jakarta.persistence.jdbc.url",
                "jdbc:h2:mem:contactbook;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "false");
        entityManagerFactory = Persistence.createEntityManagerFactory("contactBookPU", properties);
        service = new ContactService(entityManagerFactory);
    }

    @AfterAll
    static void stopPersistence() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void cleanDatabase() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("delete from Contact").executeUpdate();
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    @Test
    void createsAndReadsContact() {
        Contact contact = newContact("Анна", "Смирнова", "anna@example.com", ContactCategory.WORK);

        Contact saved = service.save(contact);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        Contact loaded = service.findById(saved.getId()).orElseThrow();
        assertEquals("Анна", loaded.getFirstName());
        assertEquals(ContactCategory.WORK, loaded.getCategory());
    }

    @Test
    void updatesExistingContact() {
        Contact saved = service.save(newContact("Анна", "Смирнова", "anna@example.com", ContactCategory.WORK));
        saved.setLastName("Иванова");
        saved.setCategory(ContactCategory.FAMILY);

        Contact updated = service.save(saved);

        assertEquals("Иванова", service.findById(updated.getId()).orElseThrow().getLastName());
        assertEquals(ContactCategory.FAMILY, service.findById(updated.getId()).orElseThrow().getCategory());
        assertEquals(1, service.count(null, null));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        service.save(newContact("Анна", "Смирнова", "Anna@Example.com", ContactCategory.WORK));

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
                () -> service.save(newContact("Другой", "Человек", "anna@example.com", ContactCategory.OTHER)));

        assertEquals("anna@example.com", exception.getEmail());
        assertEquals(1, service.count(null, null));
    }

    @Test
    void searchesByNameEmailAndCategory() {
        service.save(newContact("Анна", "Смирнова", "anna@example.com", ContactCategory.WORK));
        service.save(newContact("Дмитрий", "Козлов", "dmitry@example.com", ContactCategory.WORK));
        service.save(newContact("Мария", "Иванова", "maria@example.com", ContactCategory.FAMILY));

        assertEquals(1, service.count("смирнова", null));
        assertEquals(2, service.count(null, ContactCategory.WORK));
        assertEquals(1, service.count("maria", ContactCategory.FAMILY));
        assertEquals(0, service.count("никого", null));

        List<Contact> page = service.findPage(null, null, 0, 2);
        assertEquals(2, page.size());
        assertEquals("Иванова", page.get(0).getLastName());
    }

    @Test
    void deletesContact() {
        Contact saved = service.save(newContact("Анна", "Смирнова", "anna@example.com", ContactCategory.WORK));

        assertTrue(service.delete(saved.getId()));
        assertFalse(service.delete(saved.getId()));
        assertTrue(service.findById(saved.getId()).isEmpty());
        assertEquals(0, service.count(null, null));
    }

    private static Contact newContact(String firstName, String lastName, String email, ContactCategory category) {
        Contact contact = new Contact();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setEmail(email);
        contact.setCategory(category);
        return contact;
    }
}
