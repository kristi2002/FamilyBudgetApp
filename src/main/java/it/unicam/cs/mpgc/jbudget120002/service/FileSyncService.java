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
 * This implementation uses JSON files to store and share data between devices.
 */
public class FileSyncService implements SyncService {
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final String deviceId;
    private LocalDateTime lastSyncTime;
    private Path syncDirectory;

    public FileSyncService(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.objectMapper = configureObjectMapper();
        this.deviceId = generateDeviceId();
        this.lastSyncTime = LocalDateTime.now();
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
    public Map<String, Object> exportData() {
        Map<String, Object> syncData = new HashMap<>();
        
        // Add metadata
        syncData.put("deviceId", deviceId);
        syncData.put("timestamp", LocalDateTime.now());
        
        // Export all entities
        syncData.put("transactions", exportEntities("FROM Transaction"));
        syncData.put("tags", exportEntities("FROM Tag"));
        syncData.put("budgets", exportEntities("FROM Budget"));
        syncData.put("scheduledTransactions", exportEntities("FROM ScheduledTransaction"));
        
        // Save to file
        if (syncDirectory != null) {
            try {
                Path syncFile = syncDirectory.resolve("sync_" + deviceId + ".json");
                objectMapper.writeValue(syncFile.toFile(), syncData);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save sync data", e);
            }
        }
        
        return syncData;
    }

    @Override
    public boolean importData(Map<String, Object> syncData, ConflictResolutionStrategy strategy) {
        if (!validateSyncData(syncData)) {
            return false;
        }

        entityManager.getTransaction().begin();
        try {
            // Import each entity type
            importEntities("transactions", Transaction.class, syncData, strategy);
            importEntities("tags", Tag.class, syncData, strategy);
            importEntities("budgets", Budget.class, syncData, strategy);
            importEntities("scheduledTransactions", ScheduledTransaction.class, syncData, strategy);

            entityManager.getTransaction().commit();
            lastSyncTime = LocalDateTime.now();
            return true;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            return false;
        }
    }

    public boolean importFromFile(String filename, ConflictResolutionStrategy strategy) {
        if (syncDirectory == null) {
            throw new IllegalStateException("Sync directory not set");
        }

        try {
            Path syncFile = syncDirectory.resolve(filename);
            if (!Files.exists(syncFile)) {
                return false;
            }

            Map<String, Object> syncData = objectMapper.readValue(
                syncFile.toFile(), 
                objectMapper.getTypeFactory().constructMapType(
                    Map.class, 
                    String.class, 
                    Object.class
                )
            );

            return importData(syncData, strategy);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sync data", e);
        }
    }

    public List<String> listSyncFiles() {
        if (syncDirectory == null) {
            return Collections.emptyList();
        }

        try {
            return Files.list(syncDirectory)
                .filter(p -> p.toString().endsWith(".json"))
                .map(p -> p.getFileName().toString())
                .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list sync files", e);
        }
    }

    @Override
    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    @Override
    public boolean hasPendingChanges() {
        // Check if any entities were modified after the last sync
        String query = "SELECT COUNT(m) FROM SyncMetadata m WHERE m.lastModifiedTime > :lastSync";
        Long count = entityManager.createQuery(query, Long.class)
            .setParameter("lastSync", lastSyncTime)
            .getSingleResult();
        return count > 0;
    }

    private List<Object> exportEntities(String queryString) {
        return entityManager.createQuery(queryString).getResultList();
    }

    private <T> void importEntities(String key, Class<T> entityClass, 
            Map<String, Object> syncData, ConflictResolutionStrategy strategy) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawEntities = (List<Map<String, Object>>) syncData.get(key);
        if (rawEntities == null) return;

        for (Map<String, Object> rawEntity : rawEntities) {
            try {
                T entity = objectMapper.convertValue(rawEntity, entityClass);
                handleEntityImport(entity, strategy);
            } catch (Exception e) {
                // Log the error but continue with other entities
                System.err.println("Failed to import entity: " + e.getMessage());
            }
        }
    }

    private <T> void handleEntityImport(T entity, ConflictResolutionStrategy strategy) {
        // Implementation depends on the specific entity type and strategy
        switch (strategy) {
            case KEEP_LOCAL:
                // Do nothing, keep local version
                break;
            case KEEP_REMOTE:
                entityManager.merge(entity);
                break;
            case KEEP_NEWEST:
                // Compare timestamps and keep newest
                if (isNewer(entity)) {
                    entityManager.merge(entity);
                }
                break;
            case KEEP_BOTH:
                // Create a new copy
                entityManager.persist(entity);
                break;
        }
    }

    private <T> boolean isNewer(T entity) {
        // Implementation would compare timestamps
        // This is a simplified version
        return true;
    }

    private boolean validateSyncData(Map<String, Object> syncData) {
        return syncData != null && 
               syncData.containsKey("deviceId") &&
               syncData.containsKey("timestamp");
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
} 