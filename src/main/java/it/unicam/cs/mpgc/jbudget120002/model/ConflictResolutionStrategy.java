package it.unicam.cs.mpgc.jbudget120002.model;

/**
 * Conflict resolution strategies for data synchronization.
 */
public enum ConflictResolutionStrategy {
    KEEP_LOCAL,      // Keep local version in case of conflict
    KEEP_REMOTE,     // Use remote version in case of conflict
    KEEP_NEWEST,     // Keep the most recently modified version
    KEEP_BOTH       // Keep both versions (may result in duplicates)
} 