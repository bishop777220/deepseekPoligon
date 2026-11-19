package ru.example.contactbook.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import ru.example.contactbook.config.DatabaseConfig;
import ru.example.contactbook.model.Contact;
import ru.example.contactbook.model.ContactCategory;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Application-scoped persistence facade. Keeps transactions explicit and short,
 * which is the simplest reliable model for a RESOURCE_LOCAL persistence unit on Tomcat.
 */
@ApplicationScoped
public class ContactService {

    private static final Logger LOG = Logger.getLogger(ContactService.class.getName());
    private static final String PU_NAME = "contactBookPU";
    private static final int MAX_PAGE_SIZE = 100;

    private EntityManagerFactory entityManagerFactory;

    public ContactService() {
        // CDI
    }

    /**
     * Test-friendly constructor: the caller owns the factory.
     */
    public ContactService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory, "entityManagerFactory");
    }

    @PostConstruct
    void initialize() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME, DatabaseConfig.overrides());
            LOG.info("Persistence unit '" + PU_NAME + "' initialized");
        }
    }

    @PreDestroy
    void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            LOG.info("Persistence unit '" + PU_NAME + "' closed");
        }
    }

    public List<Contact> findPage(String search, ContactCategory category, int firstResult, int maxResults) {
        int safeFirst = Math.max(0, firstResult);
        int safeMax = Math.min(Math.max(1, maxResults), MAX_PAGE_SIZE);
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Contact> query = em.createQuery(
                    "select c from Contact c" + whereClause(search, category)
                            + " order by c.lastName, c.firstName, c.id",
                    Contact.class);
            bindParameters(query, search, category);
            query.setFirstResult(safeFirst);
            query.setMaxResults(safeMax);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long count(String search, ContactCategory category) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "select count(c) from Contact c" + whereClause(search, category),
                    Long.class);
            bindParameters(query, search, category);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public Optional<Contact> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Contact.class, id));
        } finally {
            em.close();
        }
    }

    /**
     * Inserts or updates a contact and returns the detached, persisted state.
     */
    public Contact save(Contact contact) {
        Objects.requireNonNull(contact, "contact");
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ensureEmailAvailable(em, contact);
            Contact saved;
            if (contact.getId() == null) {
                em.persist(contact);
                saved = contact;
            } else {
                saved = em.merge(contact);
            }
            transaction.commit();
            return saved;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            em.close();
        }
    }

    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Contact contact = em.find(Contact.class, id);
            if (contact == null) {
                transaction.rollback();
                return false;
            }
            em.remove(contact);
            transaction.commit();
            return true;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            em.close();
        }
    }

    private void ensureEmailAvailable(EntityManager em, Contact contact) {
        String email = contact.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        StringBuilder jpql = new StringBuilder(
                "select count(c) from Contact c where lower(c.email) = :email");
        if (contact.getId() != null) {
            jpql.append(" and c.id <> :id");
        }
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        query.setParameter("email", email.toLowerCase(Locale.ROOT));
        if (contact.getId() != null) {
            query.setParameter("id", contact.getId());
        }
        if (query.getSingleResult() > 0) {
            throw new DuplicateEmailException(email);
        }
    }

    private static String whereClause(String search, ContactCategory category) {
        StringBuilder where = new StringBuilder(" where 1 = 1");
        if (normalize(search) != null) {
            where.append(" and (lower(c.firstName) like :search")
                    .append(" or lower(c.lastName) like :search")
                    .append(" or lower(c.email) like :search")
                    .append(" or lower(c.organization) like :search)");
        }
        if (category != null) {
            where.append(" and c.category = :category");
        }
        return where.toString();
    }

    private static void bindParameters(TypedQuery<?> query, String search, ContactCategory category) {
        String normalized = normalize(search);
        if (normalized != null) {
            query.setParameter("search", "%" + normalized.toLowerCase(Locale.ROOT) + "%");
        }
        if (category != null) {
            query.setParameter("category", category);
        }
    }

    private static String normalize(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
