package it.unicam.cs.mpgc.jbudget120002.service;

import jakarta.persistence.EntityManager;

public abstract class BaseService {
    protected final EntityManager entityManager;

    protected BaseService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected void beginTransaction() {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
    }

    protected void commitTransaction() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().commit();
        }
    }

    protected void rollbackTransaction() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }
} 