package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import it.unicam.cs.mpgc.jbudget120002.model.ConflictResolutionStrategy;
import it.unicam.cs.mpgc.jbudget120002.model.SyncStatus;
import it.unicam.cs.mpgc.jbudget120002.service.SyncService;
import it.unicam.cs.mpgc.jbudget120002.service.FileSyncService;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsService;
import it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Controller class managing user settings and preferences in the Family Budget App.
 * This class handles the configuration of application-wide settings such as
 * currency preferences, date formats, and user-specific options.
 *
 * Responsibilities:
 * - Manage user preferences and settings
 * - Handle currency configuration
 * - Configure date and number formats
 * - Manage application appearance settings
 * - Coordinate with UserSettingsService for persistence
 *
 * Usage:
 * Used by MainController to manage the settings tab and provide
 * configuration options to users.
 */
public class SettingsController extends BaseController {

    @FXML private ComboBox<String> cbCurrency;
    @FXML private ComboBox<String> cbLocale;
    @FXML private ComboBox<String> cbTheme;
    @FXML private Button btnSave;
    @FXML private TextField tfDatabasePath;
    @FXML private CheckBox cbAutoBackup;
    @FXML private TextField tfBackupPath;
    
    // Sync controls
    @FXML private TextField tfSyncPath;
    @FXML private ComboBox<ConflictResolutionStrategy> cbConflictStrategy;
    @FXML private Label lblLastSync;
    @FXML private Label lblSyncStatus;

    private UserSettingsService settingsService;
    private SyncService syncService;

    @Override
    protected void initializeServices() {
        settingsService = serviceFactory.getUserSettingsService();
        syncService = serviceFactory.getSyncService();
    }

    @Override
    protected void setupUI() {
        // Initialize currency options
        cbCurrency.getItems().addAll("EUR", "USD", "GBP", "JPY", "CHF");
        
        // Initialize locale options (simplified for example)
        cbLocale.getItems().addAll("en-US", "it-IT", "de-DE", "fr-FR", "es-ES");
        
        // Initialize theme options
        cbTheme.getItems().addAll("Light", "Dark", "System");
        
        // Initialize conflict resolution strategy options
        cbConflictStrategy.getItems().addAll(ConflictResolutionStrategy.values());
        
        // Load current settings
        loadCurrentSettings();
    }

    private void loadCurrentSettings() {
        Optional<UserSettings> settings = settingsService.findFirst();
        settings.ifPresent(s -> {
            cbCurrency.setValue(s.getCurrency());
            cbLocale.setValue(s.getLocale());
            cbTheme.setValue(s.getTheme());
            tfDatabasePath.setText(s.getDatabasePath());
            cbAutoBackup.setSelected(s.isAutoBackup());
            tfBackupPath.setText(s.getBackupPath());
            tfSyncPath.setText(s.getSyncPath());
            cbConflictStrategy.setValue(s.getConflictStrategy());
        });
        
        updateSyncStatus();
    }

    @Override
    protected void loadData() {
        loadCurrentSettings();
    }

    @FXML
    private void handleSave() {
        try {
            UserSettings settings = settingsService.findFirst().orElse(new UserSettings());
            settings.setCurrency(cbCurrency.getValue());
            settings.setLocale(cbLocale.getValue());
            settings.setTheme(cbTheme.getValue());
            settings.setDatabasePath(tfDatabasePath.getText());
            settings.setAutoBackup(cbAutoBackup.isSelected());
            settings.setBackupPath(tfBackupPath.getText());
            settings.setSyncPath(tfSyncPath.getText());
            settings.setConflictStrategy(cbConflictStrategy.getValue());
            
            if (settings.getId() == null) {
                settingsService.create(settings);
            } else {
                settingsService.update(settings);
            }
            
            showInfo("Success", "Settings saved successfully");
        } catch (Exception e) {
            showError("Error", "Failed to save settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleChooseBackupPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Backup Directory");
        File file = chooser.showDialog(tfBackupPath.getScene().getWindow());
        if (file != null) {
            tfBackupPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleChooseSyncPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Sync Directory");
        File file = chooser.showDialog(tfSyncPath.getScene().getWindow());
        if (file != null) {
            tfSyncPath.setText(file.getAbsolutePath());
        }
    }

    private void updateSyncStatus() {
        if (syncService != null) {
            LocalDateTime lastSync = syncService.getLastSyncTime();
            lblLastSync.setText("Last sync: " + 
                (lastSync != null ? DateTimeUtils.formatDateTime(lastSync) : "Never"));
            
            SyncStatus status = syncService.getSyncStatus();
            lblSyncStatus.setText(status.hasChanges() ? "Changes pending" : "Up to date");
        }
    }

    @FXML
    private void handleSync() {
        try {
            LocalDateTime lastSync = syncService.getLastSyncTime();
            if (lastSync != null) {
                syncService.syncWithServer(lastSync);
            } else {
                syncService.forceSync();
            }
            showInfo("Success", "Sync completed successfully");
            updateSyncStatus();
        } catch (Exception e) {
            showError("Error", "Failed to sync: " + e.getMessage());
        }
    }
}
