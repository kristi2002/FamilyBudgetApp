package it.unicam.cs.mpgc.jbudget120002.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import java.util.logging.Logger;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML private BorderPane rootPane;
    @FXML private TabPane tabPane;
    
    // Tab references
    @FXML private Tab transactionsTab;
    @FXML private Tab statisticsTab;
    @FXML private Tab scheduledTab;
    @FXML private Tab budgetsTab;
    @FXML private Tab tagsTab;
    @FXML private Tab settingsTab;

    // View references - using Node to accept any layout type
    @FXML private Node transactionsView;
    @FXML private Node statisticsView;
    @FXML private Node scheduledView;
    @FXML private Node budgetsView;
    @FXML private Node tagsView;
    @FXML private Node settingsView;
    
    // Controller references - these will be automatically injected by FXMLLoader
    @FXML private TransactionsController transactionsViewController;
    @FXML private StatisticsController statisticsViewController;
    @FXML private ScheduledController scheduledViewController;
    @FXML private BudgetsController budgetsViewController;
    @FXML private TagsController tagsViewController;
    @FXML private SettingsController settingsViewController;

    @Override
    protected void initializeServices() {
        LOGGER.info("Initializing MainController services");
        // Set this controller as parent for all child controllers
        if (transactionsViewController != null) {
            LOGGER.info("Setting parent for TransactionsController");
            transactionsViewController.setParentController(this);
        } else {
            LOGGER.warning("TransactionsController is null");
        }
        if (scheduledViewController != null) {
            LOGGER.info("Setting parent for ScheduledController");
            scheduledViewController.setParentController(this);
        } else {
            LOGGER.warning("ScheduledController is null");
        }
        if (budgetsViewController != null) {
            LOGGER.info("Setting parent for BudgetsController");
            budgetsViewController.setParentController(this);
        } else {
            LOGGER.warning("BudgetsController is null");
        }
        if (tagsViewController != null) {
            LOGGER.info("Setting parent for TagsController");
            tagsViewController.setParentController(this);
        } else {
            LOGGER.warning("TagsController is null");
        }
        if (settingsViewController != null) {
            LOGGER.info("Setting parent for SettingsController");
            settingsViewController.setParentController(this);
        } else {
            LOGGER.warning("SettingsController is null");
        }
    }

    @Override
    protected void setupUI() {
        LOGGER.info("Setting up MainController UI");
        // Setup tab selection handlers
        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == transactionsTab) {
                    onTransactionsTabSelected();
                } else if (newTab == statisticsTab) {
                    onStatisticsTabSelected();
                } else if (newTab == scheduledTab) {
                    onScheduledTabSelected();
                } else if (newTab == budgetsTab) {
                    onBudgetsTabSelected();
                } else if (newTab == tagsTab) {
                    onTagsTabSelected();
                } else if (newTab == settingsTab) {
                    onSettingsTabSelected();
                }
            });
        } else {
            LOGGER.warning("TabPane is null");
        }
    }

    @Override
    protected void loadData() {
        LOGGER.info("Loading MainController data");
        // Show transactions view by default
        if (transactionsTab != null && tabPane != null) {
            tabPane.getSelectionModel().select(transactionsTab);
        } else {
            LOGGER.warning("TransactionsTab or TabPane is null");
        }
    }

    private void onTransactionsTabSelected() {
        if (transactionsViewController != null) {
            transactionsViewController.refreshData();
        }
    }

    private void onStatisticsTabSelected() {
        if (statisticsViewController != null) {
            statisticsViewController.refreshData();
        }
    }

    private void onScheduledTabSelected() {
        if (scheduledViewController != null) {
            scheduledViewController.refreshData();
        }
    }

    private void onBudgetsTabSelected() {
        if (budgetsViewController != null) {
            budgetsViewController.refreshData();
        }
    }

    private void onTagsTabSelected() {
        if (tagsViewController != null) {
            tagsViewController.refreshData();
        }
    }

    private void onSettingsTabSelected() {
        if (settingsViewController != null) {
            settingsViewController.refreshData();
        }
    }

    public void refreshAllViews() {
        LOGGER.info("Refreshing all views");
        if (transactionsViewController != null) transactionsViewController.refreshData();
        if (scheduledViewController != null) scheduledViewController.refreshData();
        if (budgetsViewController != null) budgetsViewController.refreshData();
        if (tagsViewController != null) tagsViewController.refreshData();
    }

    @Override
    public void cleanup() {
        LOGGER.info("Cleaning up MainController");
        super.cleanup();
        if (transactionsViewController != null) transactionsViewController.cleanup();
        if (scheduledViewController != null) scheduledViewController.cleanup();
        if (budgetsViewController != null) budgetsViewController.cleanup();
        if (tagsViewController != null) tagsViewController.cleanup();
        if (settingsViewController != null) settingsViewController.cleanup();
    }
}
