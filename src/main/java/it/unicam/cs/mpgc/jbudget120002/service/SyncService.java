package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.ConflictResolutionStrategy;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service interface for handling data synchronization across devices.
 * This interface defines the contract for synchronizing data between different instances
 * of the application, enabling multi-device support.
 */
public interface SyncService {
    /**
     * Exports the current database state to a synchronization format.
     * @return Map containing the sync data and metadata
     */
    Map<String, Object> exportData();

    /**
     * Imports data from another device or backup.
     * @param syncData The data to import
     * @param strategy The conflict resolution strategy to use
     * @return true if import was successful
     */
    boolean importData(Map<String, Object> syncData, ConflictResolutionStrategy strategy);

    /**
     * Gets the last synchronization timestamp.
     * @return The timestamp of the last successful sync
     */
    LocalDateTime getLastSyncTime();

    /**
     * Checks if there are pending changes to sync.
     * @return true if there are local changes that haven't been synced
     */
    boolean hasPendingChanges();
} 