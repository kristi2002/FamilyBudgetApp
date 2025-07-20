package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Role;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.service.UserService;
import it.unicam.cs.mpgc.jbudget120002.view.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashSet;
import java.util.Set;

public class LoginController extends BaseController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField fullNameField;
    @FXML
    private VBox emailGroup;
    @FXML
    private VBox fullNameGroup;
    @FXML
    private Button actionButton;
    @FXML
    private Text titleText;
    @FXML
    private Text subtitleText;
    @FXML
    private Text toggleText;
    @FXML
    private Button toggleButton;
    @FXML
    private Text errorText;
    @FXML
    private VBox errorContainer;

    private UserService userService;
    private MainApp mainApp;
    private boolean isLoginMode = true;

    @Override
    protected void initializeServices() {
        userService = serviceFactory.getUserService(false);
    }

    @Override
    protected void setupUI() {
        // Initialize in login mode
        updateUI();
    }

    @FXML
    public void initialize() {
        // Initialize in login mode
        updateUI();
    }

    @Override
    public void loadData() {
        // No data to load initially
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleAction() {
        if (isLoginMode) {
            handleLogin();
        } else {
            handleRegister();
        }
    }

    @FXML
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateUI();
        clearFields();
    }

    private void updateUI() {
        if (isLoginMode) {
            titleText.setText("JBudget");
            subtitleText.setText("Your Personal Finance Manager");
            actionButton.setText("Login");
            toggleText.setText("Don't have an account?");
            toggleButton.setText("Register");
            emailGroup.setVisible(false);
            fullNameGroup.setVisible(false);
        } else {
            titleText.setText("JBudget");
            subtitleText.setText("Create Your Account");
            actionButton.setText("Register");
            toggleText.setText("Already have an account?");
            toggleButton.setText("Login");
            emailGroup.setVisible(true);
            fullNameGroup.setVisible(true);
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        fullNameField.clear();
        hideError();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisible(true);
        errorContainer.setVisible(true);
        errorText.getStyleClass().clear();
        errorText.getStyleClass().add("error-text");
    }

    private void showSuccess(String message) {
        errorText.setText(message);
        errorText.setVisible(true);
        errorContainer.setVisible(true);
        errorText.getStyleClass().clear();
        errorText.getStyleClass().add("success-text");
    }

    private void hideError() {
        errorText.setVisible(false);
        errorContainer.setVisible(false);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username == null || username.trim().isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        try {
            User userToLogin = userService.findUserByName(username);
            System.out.println("Login attempt for user: " + username + ", found: " + (userToLogin != null));

            if (userToLogin != null && userToLogin.getPassword().equals(password)) {
                hideError();
                mainApp.showMainView(userToLogin);
            } else {
                showError("Invalid username or password.");
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            showError("Login failed: " + e.getMessage());
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String fullName = fullNameField.getText();
        
        if (username == null || username.trim().isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }
        
        if (email == null || email.trim().isEmpty()) {
            showError("Email cannot be empty.");
            return;
        }
        
        if (fullName == null || fullName.trim().isEmpty()) {
            showError("Full name cannot be empty.");
            return;
        }

        // Check if username already exists
        User existingUser = userService.findUserByName(username);
        if (existingUser != null) {
            showError("Username already exists. Please choose a different username.");
            return;
        }

        // Create new user
        User newUser = new User(username, password, email, fullName);
        
        // Set default role as MEMBER
        Set<Role> roles = new HashSet<>();
        roles.add(Role.MEMBER);
        newUser.setRoles(roles);

        try {
            userService.saveUser(newUser);
            System.out.println("User saved successfully: " + newUser.getUsername());
            showSuccess("Registration successful! You can now login.");
            // Switch back to login mode after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        isLoginMode = true;
                        updateUI();
                        clearFields();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
            showError("Registration failed: " + e.getMessage());
        }
    }
} 