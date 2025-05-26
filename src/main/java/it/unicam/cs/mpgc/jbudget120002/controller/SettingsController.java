package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsService;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsServiceImpl;
import it.unicam.cs.mpgc.jbudget120002.repository.UserSettingsRepositoryJpa;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class SettingsController extends BaseController {

    @FXML private ComboBox<String> cbCurrency;
    @FXML private ComboBox<String> cbLocale;
    @FXML private ComboBox<String> cbTheme;
    @FXML private Button         btnSave;
    @FXML private TextField      tfDatabasePath;
    @FXML private CheckBox       cbAutoBackup;
    @FXML private TextField      tfBackupPath;

    private final UserSettingsService settingsService =
            new UserSettingsServiceImpl(new UserSettingsRepositoryJpa());

    @Override
    protected void initializeServices() {
        // No services needed for settings
    }

    @Override
    protected void setupUI() {
        // Setup theme options
        cbTheme.getItems().addAll("Light", "Dark", "System");
        cbTheme.setValue("Light");

        // sample options
        cbCurrency.getItems().setAll("EUR","USD","GBP");
        cbLocale.getItems().setAll("it-IT","en-US","fr-FR");

        // load existing
        Optional<UserSettings> opt = settingsService.findFirst();
        if (opt.isPresent()) {
            UserSettings s = opt.get();
            cbCurrency.setValue(s.getCurrency());
            cbLocale.setValue(s.getLocale());
            cbTheme.setValue(s.getTheme());
        }

        btnSave.setOnAction(e -> saveSettings());
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        // Reload settings from configuration
        // This is a placeholder until we implement settings persistence
    }

    private void saveSettings() {
        UserSettings s = settingsService.findFirst().orElse(new UserSettings());
        s.setCurrency(cbCurrency.getValue());
        s.setLocale(cbLocale.getValue());
        s.setTheme(cbTheme.getValue());
        settingsService.create(s);
        // TODO: feedback to user
    }

    @Override
    public void cleanup() {
        // No cleanup needed for settings
        super.cleanup();
    }
}
