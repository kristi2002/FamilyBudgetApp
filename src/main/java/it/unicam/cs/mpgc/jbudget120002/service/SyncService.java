package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.SyncStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for handling data synchronization across devices.
 * This interface defines the contract for synchronizing data between different instances
 * of the application, enabling multi-device support.
 */
public interface SyncService {
    /**
     * Synchronize data with remote server
     * @param lastSyncTime Last successful synchronization time
     * @return Map of changes made during sync
     */
    Map<String, List<?>> syncWithServer(LocalDateTime lastSyncTime);
    
    /**
     * Resolve conflicts between local and remote changes
     * @param localChanges Local changes
     * @param remoteChanges Remote changes
     * @return Resolved changes
     */
    Map<String, List<?>> resolveConflicts(Map<String, List<?>> localChanges, 
                                         Map<String, List<?>> remoteChanges);
    
    /**
     * Get changes since last sync
     * @param lastSyncTime Last successful synchronization time
     * @return Map of changes
     */
    Map<String, List<?>> getChangesSince(LocalDateTime lastSyncTime);
    
    /**
     * Apply changes from remote server
     * @param changes Changes to apply
     */
    void applyChanges(Map<String, List<?>> changes);
    
    /**
     * Get sync status
     * @return Sync status information
     */
    SyncStatus getSyncStatus();
    
    /**
     * Enable/disable automatic sync
     * @param enabled Whether automatic sync should be enabled
     */
    void setAutoSync(boolean enabled);
    
    /**
     * Get last successful sync time
     * @return Last sync time
     */
    LocalDateTime getLastSyncTime();
    
    /**
     * Force immediate sync
     */
    void forceSync();
} 