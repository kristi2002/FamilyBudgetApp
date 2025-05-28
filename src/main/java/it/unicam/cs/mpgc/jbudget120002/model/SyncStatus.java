package it.unicam.cs.mpgc.jbudget120002.model;

import java.time.LocalDateTime;

/**
 * Represents the current status of data synchronization.
 */
public class SyncStatus {
    private final boolean isSyncing;
    private final LocalDateTime lastSyncTime;
    private final boolean hasChanges;
    private final int pendingChangesCount;
    private final String lastError;
    private final boolean autoSyncEnabled;

    public SyncStatus(boolean isSyncing, LocalDateTime lastSyncTime, boolean hasChanges,
                     int pendingChangesCount, String lastError, boolean autoSyncEnabled) {
        this.isSyncing = isSyncing;
        this.lastSyncTime = lastSyncTime;
        this.hasChanges = hasChanges;
        this.pendingChangesCount = pendingChangesCount;
        this.lastError = lastError;
        this.autoSyncEnabled = autoSyncEnabled;
    }

    public boolean isSyncing() {
        return isSyncing;
    }

    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public boolean hasChanges() {
        return hasChanges;
    }

    public int getPendingChangesCount() {
        return pendingChangesCount;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean isAutoSyncEnabled() {
        return autoSyncEnabled;
    }
} 