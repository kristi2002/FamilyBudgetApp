package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to track synchronization metadata for entities.
 */
@Entity
@Table(name = "sync_metadata")
public class SyncMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModifiedTime;

    @Column(name = "sync_status", nullable = false)
    private String syncStatus;

    public SyncMetadata() {
        // Required by JPA
    }

    public SyncMetadata(String entityType, Long entityId, LocalDateTime lastModifiedTime) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.lastModifiedTime = lastModifiedTime;
        this.syncStatus = "PENDING";
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(LocalDateTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
} 