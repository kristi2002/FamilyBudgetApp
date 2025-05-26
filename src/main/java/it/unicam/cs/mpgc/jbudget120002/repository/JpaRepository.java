package it.unicam.cs.mpgc.jbudget120002.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;

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
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(entity);
        tx.commit();
    }

    @Override
    public void delete(T entity) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.remove(em.contains(entity) ? entity : em.merge(entity));
        tx.commit();
    }
}
