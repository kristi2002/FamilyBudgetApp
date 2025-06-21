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
 * in the Family Budget App. This class implements shared features and lifecycle
 * management for all UI controllers.
 *
 * Responsibilities:
 * - Initialize and manage service dependencies
 * - Handle common UI setup and cleanup
 * - Provide shared utility methods
 * - Manage controller lifecycle
 * - Implement error handling and logging
 *
 * Usage:
 * Extended by all other controllers to inherit common functionality
 * and maintain consistent behavior across the application.
 */
public abstract class BaseController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    private static EntityManagerFactory emf;
    protected EntityManager entityManager;
    protected ServiceFactory serviceFactory;
    protected BaseController parentController;
    private boolean isInitialized = false;
    protected UserSession userSession;
    protected MainController mainController;

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

    public void setParentController(BaseController parent) {
        this.parentController = parent;
    }

    protected BaseController getParentController() {
        return parentController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public MainController getMainController() {
        return mainController;
    }

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

    protected abstract void initializeServices();
    protected abstract void setupUI();
    protected abstract void loadData();

    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

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
} 