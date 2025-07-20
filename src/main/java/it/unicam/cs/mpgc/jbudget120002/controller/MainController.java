package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.view.UserSession;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import java.util.logging.Logger;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Main controller class for the Family Budget App's primary user interface.
 * This class manages the main application window and coordinates between different
 * views and controllers.
 *
 * Responsibilities:
 * - Main window initialization and management
 * - Navigation between different views
 * - UI state management
 * - Event handling for main menu actions
 * - Coordination of child controllers
 *
 * Usage:
 * Serves as the primary entry point for the application's UI, managing the main
 * window and coordinating user interactions across different sections of the app.
 */
public class MainController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    private User currentUser;
    private UserSession userSession;

    @FXML private BorderPane rootPane;
    @FXML private TabPane tabPane;
    
    // Tab references
    @FXML private Tab transactionsTab;
    @FXML private Tab statisticsTab;
    @FXML private Tab scheduledTab;
    @FXML private Tab budgetsTab;
    @FXML private Tab tagsTab;
    @FXML private Tab settingsTab;
    @FXML private Tab dashboardTab;
    @FXML private Tab loanAmortizationTab;
    @FXML private Tab deadlinesTab;
    @FXML private Tab userManagementTab;

    // Controller references
    @FXML private TransactionsController transactionsViewController;
    @FXML private StatisticsController statisticsViewController;
    @FXML private ScheduledController scheduledViewController;
    @FXML private BudgetsController budgetsViewController;
    @FXML private TagsController tagsViewController;
    @FXML private SettingsController settingsViewController;
    @FXML private DeadlinesController deadlinesViewController;
    @FXML private LoanAmortizationController loanAmortizationViewController;
    @FXML private DashboardController dashboardViewController;
    @FXML private UserManagementController userManagementViewController;

    public TransactionsController getTransactionsViewController() {
        return transactionsViewController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (transactionsViewController != null) transactionsViewController.setCurrentUser(user);
        if (statisticsViewController != null) statisticsViewController.setCurrentUser(user);
        if (scheduledViewController != null) scheduledViewController.setCurrentUser(user);
        if (budgetsViewController != null) budgetsViewController.setCurrentUser(user);
        if (tagsViewController != null) tagsViewController.setCurrentUser(user);
        if (settingsViewController != null) settingsViewController.setCurrentUser(user);
        if (deadlinesViewController != null) deadlinesViewController.setCurrentUser(user);
        if (loanAmortizationViewController != null) loanAmortizationViewController.setCurrentUser(user);
        if (dashboardViewController != null) dashboardViewController.setCurrentUser(user);
        if (userManagementViewController != null) userManagementViewController.setCurrentUser(user);
    }

    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
        // You can also update UI elements here with user info
    }

    public User getLoggedInUser() {
        return (userSession != null) ? userSession.getLoggedInUser() : null;
    }

    @Override
    protected void initializeServices() {
        LOGGER.info("Initializing MainController services");
        
        // Set main controller reference for all child controllers
        if (transactionsViewController != null) {
            transactionsViewController.setMainController(this);
            if (currentUser != null) transactionsViewController.setCurrentUser(currentUser);
            LOGGER.info("Setting parent for TransactionsController");
        } else {
            LOGGER.warning("TransactionsController is null!");
        }
        if (statisticsViewController != null) {
            statisticsViewController.setMainController(this);
            if (currentUser != null) statisticsViewController.setCurrentUser(currentUser);
            LOGGER.info("Setting parent for StatisticsController");
        }
        if (scheduledViewController != null) {
            scheduledViewController.setMainController(this);
            if (currentUser != null) scheduledViewController.setCurrentUser(currentUser);
            LOGGER.info("Setting parent for ScheduledController");
        }
        if (budgetsViewController != null) {
            budgetsViewController.setMainController(this);
            if (currentUser != null) budgetsViewController.setCurrentUser(currentUser);
            LOGGER.info("Setting parent for BudgetsController");
        }
        if (tagsViewController != null) {
            tagsViewController.setMainController(this);
            if (currentUser != null) tagsViewController.setCurrentUser(currentUser);
            LOGGER.info("Setting parent for TagsController");
        }
        if (settingsViewController != null) {
            settingsViewController.setMainController(this);
            if (currentUser != null) settingsViewController.setCurrentUser(currentUser);
            LOGGER.info("Setting parent for SettingsController");
        }
        if (deadlinesViewController != null) {
            deadlinesViewController.setMainController(this);
            if (currentUser != null) deadlinesViewController.setCurrentUser(currentUser);
        }
        if (loanAmortizationViewController != null) {
            loanAmortizationViewController.setMainController(this);
            if (currentUser != null) loanAmortizationViewController.setCurrentUser(currentUser);
        }
        if (dashboardViewController != null) {
            dashboardViewController.setMainController(this);
            if (currentUser != null) dashboardViewController.setCurrentUser(currentUser);
        }
        if (userManagementViewController != null) {
            userManagementViewController.setMainController(this);
            if (currentUser != null) userManagementViewController.setCurrentUser(currentUser);
        }
    }

    @Override
    protected void setupUI() {
        LOGGER.info("Setting up MainController UI");
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == transactionsTab) onTransactionsTabSelected();
            else if (newTab == statisticsTab) onStatisticsTabSelected();
            else if (newTab == scheduledTab) onScheduledTabSelected();
            else if (newTab == budgetsTab) onBudgetsTabSelected();
            else if (newTab == tagsTab) onTagsTabSelected();
            else if (newTab == settingsTab) onSettingsTabSelected();
            else if (newTab == deadlinesTab) onDeadlinesTabSelected();
            else if (newTab == loanAmortizationTab) onLoanAmortizationTabSelected();
            else if (newTab == userManagementTab) onUserManagementTabSelected();
        });
    }

    @Override
    protected void loadData() {
        LOGGER.info("Loading MainController data");
        tabPane.getSelectionModel().select(dashboardTab);
        if (transactionsViewController != null) transactionsViewController.loadData();
    }

    @FXML
    private void onTransactionsTabSelected() {
        LOGGER.info("Transactions tab selected");
        if (transactionsViewController != null) {
            LOGGER.info("TransactionsController found, refreshing data");
            transactionsViewController.setCurrentUser(currentUser);
            transactionsViewController.refreshData();
            transactionsViewController.refreshTags();
        } else {
            LOGGER.warning("TransactionsController is null! Trying to get it from the tab content...");
            // Try to get the controller from the tab content
            if (transactionsTab != null && transactionsTab.getContent() != null) {
                try {
                    // This is a workaround - try to get the controller from the scene
                    javafx.scene.Node content = transactionsTab.getContent();
                    if (content instanceof javafx.scene.Parent) {
                        javafx.scene.Parent parent = (javafx.scene.Parent) content;
                        // Try to find the controller in the scene
                        for (javafx.scene.Node node : parent.lookupAll("*")) {
                            if (node.getUserData() instanceof TransactionsController) {
                                transactionsViewController = (TransactionsController) node.getUserData();
                                LOGGER.info("Found TransactionsController from scene lookup");
                                break;
                            }
                        }
                    }
                    
                    if (transactionsViewController != null) {
                        transactionsViewController.setCurrentUser(currentUser);
                        transactionsViewController.refreshData();
                        transactionsViewController.refreshTags();
                    } else {
                        LOGGER.severe("Could not find TransactionsController in the scene");
                    }
                } catch (Exception e) {
                    LOGGER.severe("Error trying to get TransactionsController: " + e.getMessage());
                }
            }
        }
    }
    private void onStatisticsTabSelected() { if (statisticsViewController != null) statisticsViewController.refreshData(); }
    private void onScheduledTabSelected() { 
        if (scheduledViewController != null) {
            scheduledViewController.refreshData();
            scheduledViewController.refreshTags();
        }
    }
    private void onBudgetsTabSelected() { 
        if (budgetsViewController != null) {
            budgetsViewController.refreshData();
            budgetsViewController.refreshTags();
        }
    }
    private void onTagsTabSelected() { if (tagsViewController != null) tagsViewController.refreshData(); }
    private void onSettingsTabSelected() { if (settingsViewController != null) settingsViewController.refreshData(); }
    private void onDeadlinesTabSelected() { if (deadlinesViewController != null) deadlinesViewController.refreshData(); }
    private void onLoanAmortizationTabSelected() { if (loanAmortizationViewController != null) loanAmortizationViewController.refreshData(); }
    private void onUserManagementTabSelected() {
        if (userManagementViewController != null) {
            userManagementViewController.loadData();
        }
    }

    public void refreshAllViews() {
        if (transactionsViewController != null) {
            transactionsViewController.refreshData();
            transactionsViewController.refreshTags();
        }
        if (statisticsViewController != null) statisticsViewController.refreshData();
        if (scheduledViewController != null) {
            scheduledViewController.refreshData();
            scheduledViewController.refreshTags();
        }
        if (budgetsViewController != null) {
            budgetsViewController.refreshData();
            budgetsViewController.refreshTags();
        }
        if (tagsViewController != null) tagsViewController.refreshData();
        if (settingsViewController != null) settingsViewController.refreshData();
        if (deadlinesViewController != null) {
            deadlinesViewController.refreshData();
            deadlinesViewController.refreshTags();
        }
        if (loanAmortizationViewController != null) loanAmortizationViewController.refreshData();
        if (dashboardViewController != null) dashboardViewController.refreshData();
        if (userManagementViewController != null) userManagementViewController.refreshData();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (transactionsViewController != null) transactionsViewController.cleanup();
        if (statisticsViewController != null) statisticsViewController.cleanup();
        if (scheduledViewController != null) scheduledViewController.cleanup();
        if (budgetsViewController != null) budgetsViewController.cleanup();
        if (tagsViewController != null) tagsViewController.cleanup();
        if (settingsViewController != null) settingsViewController.cleanup();
        if (deadlinesViewController != null) deadlinesViewController.cleanup();
        if (loanAmortizationViewController != null) loanAmortizationViewController.cleanup();
        if (dashboardViewController != null) dashboardViewController.cleanup();
        if (userManagementViewController != null) userManagementViewController.cleanup();
    }
}
