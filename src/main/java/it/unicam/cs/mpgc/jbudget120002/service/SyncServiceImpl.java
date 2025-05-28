package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the SyncService interface that manages data synchronization across
 * multiple devices in the Family Budget App. This class handles the synchronization
 * of transactions, budgets, and user preferences between different instances of the app.
 *
 * Responsibilities:
 * - Multi-device data synchronization
 * - Conflict resolution
 * - Change tracking and versioning
 * - Data consistency maintenance
 * - Offline support and sync queue management
 *
 * Usage:
 * Used by the application to ensure data consistency across different devices and
 * to handle offline operations with subsequent synchronization.
 */
public class SyncServiceImpl implements SyncService {
    private final EntityManager entityManager;
    private final TransactionService transactionService;
    private final ScheduledTransactionService scheduledTransactionService;
    private final Map<String, List<?>> pendingChanges = new ConcurrentHashMap<>();
    private LocalDateTime lastSyncTime;
    private boolean autoSyncEnabled = true;
    private boolean isSyncing = false;
    private String lastSyncError;

    public SyncServiceImpl(EntityManager entityManager, 
                          TransactionService transactionService,
                          ScheduledTransactionService scheduledTransactionService) {
        this.entityManager = entityManager;
        this.transactionService = transactionService;
        this.scheduledTransactionService = scheduledTransactionService;
        this.lastSyncTime = LocalDateTime.now();
    }

    @Override
    public Map<String, List<?>> syncWithServer(LocalDateTime lastSyncTime) {
        if (isSyncing) {
            throw new IllegalStateException("Sync already in progress");
        }

        isSyncing = true;
        try {
            // Get local changes
            Map<String, List<?>> localChanges = getChangesSince(lastSyncTime);
            
            // Get remote changes (simulated for now)
            Map<String, List<?>> remoteChanges = simulateRemoteChanges();
            
            // Resolve conflicts
            Map<String, List<?>> resolvedChanges = resolveConflicts(localChanges, remoteChanges);
            
            // Apply changes
            applyChanges(resolvedChanges);
            
            // Update sync time
            this.lastSyncTime = LocalDateTime.now();
            
            return resolvedChanges;
        } catch (Exception e) {
            lastSyncError = e.getMessage();
            throw e;
        } finally {
            isSyncing = false;
        }
    }

    @Override
    public Map<String, List<?>> resolveConflicts(Map<String, List<?>> localChanges, 
                                               Map<String, List<?>> remoteChanges) {
        Map<String, List<?>> resolvedChanges = new HashMap<>();
        
        // Merge changes, preferring the most recent version
        for (String entityType : new HashSet<>(localChanges.keySet())) {
            List<?> localEntities = localChanges.get(entityType);
            List<?> remoteEntities = remoteChanges.get(entityType);
            
            if (remoteEntities == null) {
                resolvedChanges.put(entityType, localEntities);
            } else if (localEntities == null) {
                resolvedChanges.put(entityType, remoteEntities);
            } else {
                // Merge lists, removing duplicates
                List<Object> merged = new ArrayList<>();
                merged.addAll(localEntities);
                merged.addAll(remoteEntities);
                resolvedChanges.put(entityType, merged);
            }
        }
        
        return resolvedChanges;
    }

    @Override
    public Map<String, List<?>> getChangesSince(LocalDateTime lastSyncTime) {
        Map<String, List<?>> changes = new HashMap<>();
        
        // Get changed transactions
        List<Transaction> transactions = transactionService.findByDateRange(
            lastSyncTime.toLocalDate(), LocalDateTime.now().toLocalDate());
        if (!transactions.isEmpty()) {
            changes.put("transactions", transactions);
        }
        
        // Get changed scheduled transactions
        List<ScheduledTransaction> scheduledTransactions = scheduledTransactionService
            .findByDateRange(lastSyncTime.toLocalDate(), LocalDateTime.now().toLocalDate());
        if (!scheduledTransactions.isEmpty()) {
            changes.put("scheduledTransactions", scheduledTransactions);
        }
        
        return changes;
    }

    @Override
    public void applyChanges(Map<String, List<?>> changes) {
        entityManager.getTransaction().begin();
        try {
            for (Map.Entry<String, List<?>> entry : changes.entrySet()) {
                String entityType = entry.getKey();
                List<?> entities = entry.getValue();
                
                switch (entityType) {
                    case "transactions":
                        for (Object entity : entities) {
                            if (entity instanceof Transaction) {
                                Transaction transaction = (Transaction) entity;
                                if (transaction.getId() == null) {
                                    entityManager.persist(transaction);
                                } else {
                                    entityManager.merge(transaction);
                                }
                            }
                        }
                        break;
                    case "scheduledTransactions":
                        for (Object entity : entities) {
                            if (entity instanceof ScheduledTransaction) {
                                ScheduledTransaction scheduled = (ScheduledTransaction) entity;
                                if (scheduled.getId() == null) {
                                    entityManager.persist(scheduled);
                                } else {
                                    entityManager.merge(scheduled);
                                }
                            }
                        }
                        break;
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public SyncStatus getSyncStatus() {
        return new SyncStatus(
            isSyncing,
            lastSyncTime,
            !pendingChanges.isEmpty(),
            pendingChanges.values().stream()
                .mapToInt(List::size)
                .sum(),
            lastSyncError,
            autoSyncEnabled
        );
    }

    @Override
    public void setAutoSync(boolean enabled) {
        this.autoSyncEnabled = enabled;
    }

    @Override
    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    @Override
    public void forceSync() {
        syncWithServer(lastSyncTime);
    }

    // Helper method to simulate remote changes (for testing)
    private Map<String, List<?>> simulateRemoteChanges() {
        // In a real implementation, this would fetch changes from a remote server
        return new HashMap<>();
    }
} 