package it.unicam.cs.mpgc.jbudget120002.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base repository class implementing common JPA operations for the Family Budget App.
 * This class provides a generic implementation of the Repository interface using JPA,
 * handling basic CRUD operations and transaction management.
 *
 * Responsibilities:
 * - Implement basic CRUD operations using JPA
 * - Manage entity persistence and retrieval
 * - Handle transaction boundaries
 * - Provide reflection-based entity ID management
 * - Support generic entity operations
 *
 * Usage:
 * Extended by specific repository implementations to provide
 * type-safe database operations for different entities.
 */
public abstract class JpaRepository<T, ID> implements Repository<T, ID> {
    protected final EntityManager em;
    private final Class<T> entityClass;

    protected JpaRepository(Class<T> entityClass, EntityManager entityManager) {
        this.entityClass = entityClass;
        this.em = entityManager;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                .getResultList();
    }

    @Override
    public void save(T entity) {
        // Don't manage transactions here - let the service layer handle it
        if (em.contains(entity) || getEntityId(entity) != null) {
            em.merge(entity);
        } else {
            em.persist(entity);
        }
    }

    // Helper method to get the ID of the entity using reflection
    private Object getEntityId(T entity) {
        try {
            return entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void delete(T entity) {
        // Don't manage transactions here - let the service layer handle it
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
}
