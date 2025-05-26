package it.unicam.cs.mpgc.jbudget120002.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MainController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML
    private BorderPane rootPane;

    private Node transactionsView;
    private Node budgetsView;
    private Node deadlinesView;
    private Node statisticsView;
    private Node tagsView;
    private Node scheduledView;
    private Node settingsView;

    private TransactionsController transactionsController;
    private BudgetsController budgetsController;
    private DeadlinesController deadlinesController;
    private StatisticsController statisticsController;
    private TagsController tagsController;
    private ScheduledController scheduledController;
    private SettingsController settingsController;

    @Override
    protected void initializeServices() {
        // No services needed in main controller
    }

    @Override
    protected void setupUI() {
        try {
            loadViews();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load application views", e);
            showError("Error", "Failed to load application views: " + e.getMessage());
        }
    }

    @Override
    protected void loadData() {
        showTransactions(); // Show transactions view by default
    }

    private void loadViews() throws IOException {
        // Load all views
        try {
            FXMLLoader transactionsLoader = new FXMLLoader(getClass().getResource("/fxml/TransactionsView.fxml"));
            transactionsView = transactionsLoader.load();
            transactionsController = transactionsLoader.getController();
            LOGGER.info("Successfully loaded TransactionsView");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load transactions view", e);
            showError("Error", "Failed to load transactions view: " + e.getMessage());
        }

        try {
            FXMLLoader budgetsLoader = new FXMLLoader(getClass().getResource("/fxml/BudgetsView.fxml"));
            budgetsView = budgetsLoader.load();
            budgetsController = budgetsLoader.getController();
            LOGGER.info("Successfully loaded BudgetsView");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load budgets view", e);
            showError("Error", "Failed to load budgets view: " + e.getMessage());
        }

        try {
            FXMLLoader deadlinesLoader = new FXMLLoader(getClass().getResource("/fxml/DeadlinesView.fxml"));
            deadlinesView = deadlinesLoader.load();
            deadlinesController = deadlinesLoader.getController();
            LOGGER.info("Successfully loaded DeadlinesView");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load deadlines view", e);
            showError("Error", "Failed to load deadlines view: " + e.getMessage());
        }

        try {
            FXMLLoader statisticsLoader = new FXMLLoader(getClass().getResource("/fxml/StatisticsView.fxml"));
            statisticsView = statisticsLoader.load();
            statisticsController = statisticsLoader.getController();
            LOGGER.info("Successfully loaded StatisticsView");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load statistics view", e);
            showError("Error", "Failed to load statistics view: " + e.getMessage());
        }

        try {
            FXMLLoader tagsLoader = new FXMLLoader(getClass().getResource("/fxml/TagsView.fxml"));
            tagsView = tagsLoader.load();
            tagsController = tagsLoader.getController();
            LOGGER.info("Successfully loaded TagsView");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load tags view", e);
            showError("Error", "Failed to load tags view: " + e.getMessage());
        }

        try {
            FXMLLoader scheduledLoader = new FXMLLoader(getClass().getResource("/fxml/ScheduledView.fxml"));
            scheduledView = scheduledLoader.load();
            scheduledController = scheduledLoader.getController();
            LOGGER.info("Successfully loaded ScheduledView");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load scheduled view", e);
            showError("Error", "Failed to load scheduled view: " + e.getMessage());
        }

        try {
            FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("/fxml/SettingsView.fxml"));
            settingsView = settingsLoader.load();
            settingsController = settingsLoader.getController();
            LOGGER.info("Successfully loaded SettingsView");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load settings view", e);
            showError("Error", "Failed to load settings view: " + e.getMessage());
        }
    }

    @FXML
    public void showTransactions() {
        if (transactionsView != null) {
            rootPane.setCenter(transactionsView);
            transactionsController.refreshData();
        }
    }

    @FXML
    public void showBudgets() {
        if (budgetsView != null) {
            rootPane.setCenter(budgetsView);
            budgetsController.refreshData();
        }
    }

    @FXML
    public void showDeadlines() {
        if (deadlinesView != null) {
            rootPane.setCenter(deadlinesView);
            deadlinesController.refreshData();
        }
    }

    @FXML
    public void showStatistics() {
        if (statisticsView != null) {
            rootPane.setCenter(statisticsView);
            statisticsController.refreshData();
        }
    }

    @FXML
    public void showTags() {
        if (tagsView != null) {
            rootPane.setCenter(tagsView);
            tagsController.refreshData();
        }
    }

    @FXML
    public void showScheduled() {
        if (scheduledView != null) {
            rootPane.setCenter(scheduledView);
            scheduledController.refreshData();
        }
    }

    @FXML
    public void showSettings() {
        if (settingsView != null) {
            rootPane.setCenter(settingsView);
            settingsController.refreshData();
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (transactionsController != null) transactionsController.cleanup();
        if (budgetsController != null) budgetsController.cleanup();
        if (deadlinesController != null) deadlinesController.cleanup();
        if (statisticsController != null) statisticsController.cleanup();
        if (tagsController != null) tagsController.cleanup();
        if (scheduledController != null) scheduledController.cleanup();
        if (settingsController != null) settingsController.cleanup();
    }
}
