package it.unicam.cs.mpgc.jbudget120002.service;

import jakarta.persistence.EntityManager;

/**
 * Abstract base service class providing common functionality for all service implementations
 * in the Family Budget App. This class manages shared logic for database transactions,
 * entity management, and error handling.
 *
 * Responsibilities:
 * - Manage EntityManager and database transactions
 * - Provide utility methods for transaction handling
 * - Implement common error handling for services
 * - Serve as a superclass for all concrete service classes
 *
 * Usage:
 * Extended by all service implementation classes to inherit common functionality
 * and ensure consistent transaction management and error handling.
 */
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