package it.unicam.cs.mpgc.jbudget120002.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.unicam.cs.mpgc.jbudget120002.model.*;
import jakarta.persistence.EntityManager;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * File-based implementation of the SyncService interface.
 * This implementation uses serialized files to store and share data between devices.
 */
public class FileSyncService implements SyncService {
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final String deviceId;
    private LocalDateTime lastSyncTime;
    private Path syncDirectory;
    private final TransactionService transactionService;
    private final ScheduledTransactionService scheduledTransactionService;
    private final String syncFilePath;
    private boolean autoSyncEnabled = true;
    private boolean isSyncing = false;
    private String lastSyncError;

    public FileSyncService(EntityManager entityManager, 
                          TransactionService transactionService,
                          ScheduledTransactionService scheduledTransactionService,
                          String syncFilePath) {
        this.entityManager = entityManager;
        this.objectMapper = configureObjectMapper();
        this.deviceId = generateDeviceId();
        this.lastSyncTime = LocalDateTime.now();
        this.transactionService = transactionService;
        this.scheduledTransactionService = scheduledTransactionService;
        this.syncFilePath = syncFilePath;
    }

    private ObjectMapper configureObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Add Java 8 date/time support
        mapper.registerModule(new JavaTimeModule());
        
        // Configure entity handling
        mapper.addMixIn(Transaction.class, JpaEntityMixin.class);
        mapper.addMixIn(Tag.class, JpaEntityMixin.class);
        mapper.addMixIn(Budget.class, JpaEntityMixin.class);
        mapper.addMixIn(ScheduledTransaction.class, JpaEntityMixin.class);
        
        // Ignore unknown properties during deserialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }

    public void setSyncDirectory(String path) {
        this.syncDirectory = Paths.get(path);
        if (!Files.exists(syncDirectory)) {
            try {
                Files.createDirectories(syncDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create sync directory", e);
            }
        }
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
            
            // Read remote changes from file
            Map<String, List<?>> remoteChanges = readRemoteChanges();
            
            // Resolve conflicts
            Map<String, List<?>> resolvedChanges = resolveConflicts(localChanges, remoteChanges);
            
            // Apply changes
            applyChanges(resolvedChanges);
            
            // Write changes to file
            writeChangesToFile(resolvedChanges);
            
            // Update sync time
            this.lastSyncTime = LocalDateTime.now();
            
            return resolvedChanges;
        } catch (Exception e) {
            lastSyncError = e.getMessage();
            throw new RuntimeException("Sync failed: " + e.getMessage(), e);
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
            false, // hasChanges
            0, // pendingChangesCount
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

    private Map<String, List<?>> readRemoteChanges() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(syncFilePath))) {
            return (Map<String, List<?>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    private void writeChangesToFile(Map<String, List<?>> changes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(syncFilePath))) {
            oos.writeObject(changes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write changes to file: " + e.getMessage(), e);
        }
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
} 