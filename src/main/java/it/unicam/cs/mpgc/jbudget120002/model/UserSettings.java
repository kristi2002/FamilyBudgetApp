package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;

/**
 * Entity class representing user settings and preferences in the Family Budget App.
 * This class stores application-wide configuration settings and user preferences
 * that affect the behavior and appearance of the application.
 *
 * Responsibilities:
 * - Store currency preferences
 * - Manage date and number formats
 * - Track user interface preferences
 * - Store notification settings
 * - Maintain application configuration
 *
 * Usage:
 * Used by UserSettingsService to manage application settings
 * and provide consistent configuration across the application.
 */
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String currency = "EUR";

    @Column(nullable = false)
    private String locale = "it-IT";

    @Column(nullable = false)
    private String theme = "Light";

    @Column(name = "database_path")
    private String databasePath;

    @Column(name = "auto_backup")
    private boolean autoBackup = false;

    @Column(name = "backup_path")
    private String backupPath;

    @Column(name = "sync_path")
    private String syncPath;

    @Column(name = "conflict_strategy")
    @Enumerated(EnumType.STRING)
    private ConflictResolutionStrategy conflictStrategy = ConflictResolutionStrategy.KEEP_NEWEST;

    public UserSettings() {}

    public UserSettings(String currency, String locale, String theme) {
        this.currency = currency;
        this.locale = locale;
        this.theme = theme;
    }

    public Long getId() { return id; }
    public String getCurrency() { return currency; }
    public String getLocale() { return locale; }
    public String getTheme() { return theme; }
    public String getDatabasePath() { return databasePath; }
    public boolean isAutoBackup() { return autoBackup; }
    public String getBackupPath() { return backupPath; }
    public String getSyncPath() { return syncPath; }
    public ConflictResolutionStrategy getConflictStrategy() { return conflictStrategy; }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }
    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }
    public void setAutoBackup(boolean autoBackup) {
        this.autoBackup = autoBackup;
    }
    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }
    public void setSyncPath(String syncPath) {
        this.syncPath = syncPath;
    }
    public void setConflictStrategy(ConflictResolutionStrategy strategy) {
        this.conflictStrategy = strategy;
    }
}
