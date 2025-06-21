package it.unicam.cs.mpgc.jbudget120002.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Supplier;

/**
 * Abstract base service class providing common functionality for all service implementations
 * in the Family Budget App.
 * 
 * <p>This class manages shared logic for database transactions, entity management, and error
 * handling. It provides a consistent foundation for all service classes, ensuring proper
 * transaction management and resource cleanup.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Manage EntityManager and database transactions</li>
 *   <li>Provide utility methods for transaction handling</li>
 *   <li>Implement common error handling for services</li>
 *   <li>Serve as a superclass for all concrete service classes</li>
 *   <li>Ensure consistent transaction lifecycle management</li>
 *   <li>Provide functional transaction execution methods</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Execute a simple action in transaction
 * executeInTransaction(() -> {
 *     // Database operations here
 *     repository.save(entity);
 * });
 * 
 * // Execute with return value
 * Entity result = executeInTransaction(() -> {
 *     return repository.findById(id);
 * });
 * 
 * // Manual transaction management
 * beginTransaction();
 * try {
 *     // Database operations
 *     commitTransaction();
 * } catch (Exception e) {
 *     rollbackTransaction();
 *     throw e;
 * }
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseService {
    
    /** The EntityManager instance for database operations */
    protected final EntityManager em;

    // ==================== CONSTRUCTORS ====================

    /**
     * Creates a new BaseService with the specified EntityManager.
     * 
     * @param entityManager the EntityManager to use for database operations
     * @throws IllegalArgumentException if entityManager is null
     */
    public BaseService(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null");
        }
        this.em = entityManager;
    }

    // ==================== TRANSACTION MANAGEMENT METHODS ====================

    /**
     * Executes a runnable action within a database transaction.
     * The transaction is automatically committed on success or rolled back on failure.
     * 
     * @param action the action to execute
     * @throws RuntimeException if the action fails or transaction management fails
     */
    protected void executeInTransaction(Runnable action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        EntityTransaction transaction = em.getTransaction();
        boolean wasActive = transaction.isActive();
        
        if (!wasActive) {
            transaction.begin();
        }
        
        try {
            action.run();
            if (!wasActive && transaction.isActive()) {
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        }
    }

    /**
     * Executes a supplier function within a database transaction and returns the result.
     * The transaction is automatically committed on success or rolled back on failure.
     * 
     * @param <T> the type of the result
     * @param supplier the supplier function to execute
     * @return the result of the supplier function
     * @throws RuntimeException if the supplier fails or transaction management fails
     */
    protected <T> T executeInTransaction(Supplier<T> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        
        EntityTransaction transaction = em.getTransaction();
        boolean wasActive = transaction.isActive();
        
        if (!wasActive) {
            transaction.begin();
        }
        
        try {
            T result = supplier.get();
            if (!wasActive && transaction.isActive()) {
                transaction.commit();
            }
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        }
    }

    /**
     * Begins a new database transaction if one is not already active.
     * 
     * @throws IllegalStateException if transaction management fails
     */
    protected void beginTransaction() {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) {
            try {
                transaction.begin();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to begin transaction", e);
            }
        }
    }

    /**
     * Commits the current database transaction if one is active.
     * 
     * @throws IllegalStateException if transaction management fails
     */
    protected void commitTransaction() {
        EntityTransaction transaction = em.getTransaction();
        if (transaction.isActive()) {
            try {
                transaction.commit();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to commit transaction", e);
            }
        }
    }

    /**
     * Rolls back the current database transaction if one is active.
     * 
     * @throws IllegalStateException if transaction management fails
     */
    protected void rollbackTransaction() {
        EntityTransaction transaction = em.getTransaction();
        if (transaction.isActive()) {
            try {
                transaction.rollback();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to rollback transaction", e);
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Checks if a transaction is currently active.
     * 
     * @return true if a transaction is active, false otherwise
     */
    protected boolean isTransactionActive() {
        return em.getTransaction().isActive();
    }

    /**
     * Flushes the EntityManager, causing any pending changes to be written to the database.
     * 
     * @throws RuntimeException if the flush operation fails
     */
    protected void flush() {
        try {
            em.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to flush EntityManager", e);
        }
    }

    /**
     * Clears the EntityManager, removing all managed entities from the persistence context.
     * 
     * @throws RuntimeException if the clear operation fails
     */
    protected void clear() {
        try {
            em.clear();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear EntityManager", e);
        }
    }

    /**
     * Refreshes an entity from the database, updating its state.
     * 
     * @param entity the entity to refresh
     * @throws RuntimeException if the refresh operation fails
     */
    protected void refresh(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        try {
            em.refresh(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh entity", e);
        }
    }

    /**
     * Merges an entity into the persistence context.
     * 
     * @param <T> the type of the entity
     * @param entity the entity to merge
     * @return the merged entity
     * @throws RuntimeException if the merge operation fails
     */
    protected <T> T merge(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        try {
            return em.merge(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge entity", e);
        }
    }

    /**
     * Detaches an entity from the persistence context.
     * 
     * @param entity the entity to detach
     * @throws RuntimeException if the detach operation fails
     */
    protected void detach(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        try {
            em.detach(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to detach entity", e);
        }
    }

    /**
     * Checks if an entity is managed by the EntityManager.
     * 
     * @param entity the entity to check
     * @return true if the entity is managed, false otherwise
     */
    protected boolean contains(Object entity) {
        if (entity == null) {
            return false;
        }
        
        try {
            return em.contains(entity);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the EntityManager instance.
     * 
     * @return the EntityManager
     */
    protected EntityManager getEntityManager() {
        return em;
    }
}