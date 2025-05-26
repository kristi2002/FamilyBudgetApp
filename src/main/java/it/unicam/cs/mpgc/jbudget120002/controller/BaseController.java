package it.unicam.cs.mpgc.jbudget120002.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import it.unicam.cs.mpgc.jbudget120002.service.ServiceFactory;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseController implements Initializable {
    protected static final EntityManagerFactory emf = 
        Persistence.createEntityManagerFactory("jbudgetPU");
    protected final EntityManager entityManager;
    protected final ServiceFactory serviceFactory;

    public BaseController() {
        this.entityManager = emf.createEntityManager();
        this.serviceFactory = new ServiceFactory(entityManager);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupUI();
        loadData();
    }

    protected abstract void initializeServices();
    protected abstract void setupUI();
    protected abstract void loadData();

    protected void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showWarning(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void cleanup() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }
} 