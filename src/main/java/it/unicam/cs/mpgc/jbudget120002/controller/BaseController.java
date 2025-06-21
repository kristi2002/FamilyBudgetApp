package it.unicam.cs.mpgc.jbudget120002.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import it.unicam.cs.mpgc.jbudget120002.service.ServiceFactory;
import it.unicam.cs.mpgc.jbudget120002.view.UserSession;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Base controller class that provides common functionality for all controllers
 * in the Family Budget App.
 * 
 * <p>This class implements shared features and lifecycle management for all UI
 * controllers. It provides a consistent foundation for controller initialization,
 * service management, error handling, and resource cleanup.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Initialize and manage service dependencies</li>
 *   <li>Handle common UI setup and cleanup</li>
 *   <li>Provide shared utility methods</li>
 *   <li>Manage controller lifecycle</li>
 *   <li>Implement error handling and logging</li>
 *   <li>Manage EntityManager lifecycle</li>
 *   <li>Provide user feedback mechanisms</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * public class MyController extends BaseController {
 *     @Override
 *     protected void initializeServices() {
 *         // Initialize service dependencies
 *     }
 *     
 *     @Override
 *     protected void setupUI() {
 *         // Setup UI components
 *     }
 *     
 *     @Override
 *     protected void loadData() {
 *         // Load initial data
 *     }
 * }
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseController implements Initializable {
    
    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    
    /** EntityManagerFactory instance shared across all controllers */
    private static EntityManagerFactory emf;
    
    /** EntityManager instance for this controller */
    protected EntityManager entityManager;
    
    /** ServiceFactory instance for creating services */
    protected ServiceFactory serviceFactory;
    
    /** Parent controller reference for navigation */
    protected BaseController parentController;
    
    /** Flag indicating if the controller has been initialized */
    private boolean isInitialized = false;
    
    /** User session information */
    protected UserSession userSession;
    
    /** Main controller reference */
    protected MainController mainController;

    // ==================== STATIC INITIALIZATION ====================

    /**
     * Static initialization block to create the EntityManagerFactory.
     * Also registers a shutdown hook to properly close the factory.
     */
    static {
        try {
            emf = Persistence.createEntityManagerFactory("jbudgetPU");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (emf != null && emf.isOpen()) {
                    emf.close();
                }
            }));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create EntityManagerFactory", e);
            throw new RuntimeException("Failed to create EntityManagerFactory", e);
        }
    }

    // ==================== INITIALIZATION METHODS ====================

    /**
     * Initializes the controller with the specified location and resources.
     * This method is called by JavaFX when the FXML is loaded.
     * 
     * @param location the location used to resolve relative paths for the root object
     * @param resources the resources used to localize the root object
     * @throws RuntimeException if initialization fails
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (!isInitialized) {
                LOGGER.info("Initializing controller: " + getClass().getSimpleName());
                
                // Create a new EntityManager if needed
                if (entityManager == null || !entityManager.isOpen()) {
                    LOGGER.info("Creating new EntityManager instance");
                    entityManager = emf.createEntityManager();
                    serviceFactory = new ServiceFactory(entityManager);
                }
                
                try {
                    initializeServices();
                    setupUI();
                    loadData();
                    isInitialized = true;
                    LOGGER.info("Successfully initialized controller: " + getClass().getSimpleName());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error during controller initialization", e);
                    cleanup();
                    throw e;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize controller: " + getClass().getSimpleName(), e);
            showError("Initialization Error", "Failed to initialize: " + e.getMessage());
            throw new RuntimeException("Failed to initialize controller", e);
        }
    }

    /**
     * Sets the parent controller for navigation purposes.
     * 
     * @param parent the parent controller
     */
    public void setParentController(BaseController parent) {
        this.parentController = parent;
    }

    /**
     * Gets the parent controller.
     * 
     * @return the parent controller, or null if not set
     */
    protected BaseController getParentController() {
        return parentController;
    }

    /**
     * Sets the main controller reference.
     * 
     * @param mainController the main controller
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Gets the main controller reference.
     * 
     * @return the main controller, or null if not set
     */
    public MainController getMainController() {
        return mainController;
    }

    /**
     * Sets the user session information.
     * 
     * @param userSession the user session
     */
    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
    }

    /**
     * Gets the user session information.
     * 
     * @return the user session, or null if not set
     */
    public UserSession getUserSession() {
        return userSession;
    }

    // ==================== DATA MANAGEMENT METHODS ====================

    /**
     * Refreshes the data in the controller.
     * This method creates a new EntityManager if needed and reloads the data.
     */
    public void refreshData() {
        try {
            // Only create a new EntityManager if the current one is closed
            if (entityManager == null || !entityManager.isOpen()) {
                LOGGER.info("Creating new EntityManager instance for refresh");
                entityManager = emf.createEntityManager();
                serviceFactory = new ServiceFactory(entityManager);
            }
            
            // Only begin transaction if not already active
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }
            
            try {
                loadData();
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().commit();
                }
            } catch (Exception e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to refresh data in controller: " + getClass().getSimpleName(), e);
            showError("Refresh Error", "Failed to refresh data: " + e.getMessage());
        }
    }

    /**
     * Checks if the controller has been initialized.
     * 
     * @return true if the controller is initialized, false otherwise
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Initialize service dependencies.
     * This method should be implemented by subclasses to set up their required services.
     */
    protected abstract void initializeServices();

    /**
     * Setup UI components.
     * This method should be implemented by subclasses to configure their UI elements.
     */
    protected abstract void setupUI();

    /**
     * Load initial data.
     * This method should be implemented by subclasses to load their required data.
     */
    protected abstract void loadData();

    // ==================== USER FEEDBACK METHODS ====================

    /**
     * Shows an error dialog with the specified title and content.
     * 
     * @param title the dialog title
     * @param content the error message
     */
    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows an information dialog with the specified title and content.
     * 
     * @param title the dialog title
     * @param content the information message
     */
    protected void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows a warning dialog with the specified title and content.
     * 
     * @param title the dialog title
     * @param content the warning message
     */
    protected void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with the specified title and content.
     * 
     * @param title the dialog title
     * @param content the confirmation message
     * @return true if the user confirmed, false otherwise
     */
    protected boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }

    // ==================== RESOURCE MANAGEMENT METHODS ====================

    /**
     * Cleans up resources used by this controller.
     * This method should be called when the controller is no longer needed.
     */
    public void cleanup() {
        try {
            LOGGER.info("Cleaning up controller: " + getClass().getSimpleName());
            if (entityManager != null && entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    LOGGER.info("Rolling back active transaction in " + getClass().getSimpleName());
                    entityManager.getTransaction().rollback();
                }
                LOGGER.info("Closing EntityManager for " + getClass().getSimpleName());
                entityManager.close();
                entityManager = null;
            }
            // Clear service factory reference
            serviceFactory = null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during cleanup of controller: " + getClass().getSimpleName(), e);
        }
    }

    /**
     * Closes the EntityManagerFactory.
     * This method should be called when the application is shutting down.
     */
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            try {
                emf.close();
                emf = null;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error closing EntityManagerFactory", e);
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Gets the EntityManager instance.
     * 
     * @return the EntityManager, or null if not initialized
     */
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Gets the ServiceFactory instance.
     * 
     * @return the ServiceFactory, or null if not initialized
     */
    protected ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    /**
     * Checks if the EntityManager is open and available.
     * 
     * @return true if the EntityManager is open, false otherwise
     */
    protected boolean isEntityManagerOpen() {
        return entityManager != null && entityManager.isOpen();
    }

    /**
     * Logs an error message.
     * 
     * @param message the error message
     * @param throwable the exception, if any
     */
    protected void logError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    /**
     * Logs an info message.
     * 
     * @param message the info message
     */
    protected void logInfo(String message) {
        LOGGER.info(message);
    }

    /**
     * Logs a warning message.
     * 
     * @param message the warning message
     */
    protected void logWarning(String message) {
        LOGGER.warning(message);
    }
} 