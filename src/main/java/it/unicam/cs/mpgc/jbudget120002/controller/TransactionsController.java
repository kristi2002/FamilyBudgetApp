package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import it.unicam.cs.mpgc.jbudget120002.service.TransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsService;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import javafx.util.StringConverter;

/**
 * Controller class managing the transactions view in the Family Budget App.
 * This class handles the user interface for viewing, creating, editing, and
 * managing financial transactions.
 *
 * Responsibilities:
 * - Display and manage transaction list
 * - Handle transaction creation and editing
 * - Implement transaction filtering and search
 * - Manage transaction categorization
 * - Coordinate with TransactionService for data operations
 *
 * Usage:
 * Used by MainController to manage the transactions tab and provide
 * transaction management functionality to users.
 */
public class TransactionsController extends BaseController {
    private User currentUser;

    // Basic transaction form controls
    @FXML private DatePicker dpDate;
    @FXML private TextField tfDesc;
    @FXML private TextField tfAmount;
    @FXML private CheckBox cbIncome;
    
    // Advanced filtering controls
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField tfSearch;
    @FXML private Button btnClearFilters;
    @FXML private Button btnSave;
    @FXML private Button btnClearForm;
    @FXML private Button btnCancel;
    @FXML private Button btnAdd;
    @FXML private Button btnGenerateScheduled;
    
    // Statistics labels
    @FXML private Label lblTotalIncome;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblBalance;
    
    // Table
    @FXML private TableView<Transaction> table;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colDesc;
    @FXML private TableColumn<Transaction, BigDecimal> colAmount;
    @FXML private TableColumn<Transaction, String> colTags;
    @FXML private TableColumn<Transaction, Boolean> colScheduled;

    @FXML private CheckBox cbIncludeSubcategories;
    private boolean isEditMode = false;

    // Add new category filter fields
    @FXML private ComboBox<Tag> cbCategory;

    @FXML private ComboBox<Tag> cbTags;
    @FXML private FlowPane flowSelectedTags;
    private Set<Tag> selectedTags;

    private TransactionService transactionService;
    private TagService tagService;
    private UserSettingsService settingsService;
    private ScheduledTransactionService scheduledTransactionService;
    private ObservableList<Transaction> transactions;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    protected void initializeServices() {
        transactionService = serviceFactory.getTransactionService(false);
        tagService = serviceFactory.getTagService(false);
        settingsService = serviceFactory.getUserSettingsService(false);
        scheduledTransactionService = serviceFactory.getScheduledTransactionService(false);
        transactions = FXCollections.observableArrayList();
        selectedTags = new HashSet<>();
    }

    @Override
    protected void setupUI() {
        //setupDatePickers(); // Will be called from loadData
        setupTable();
        setupTagsUI();
        setupCategoryFilter();
        setupFilters();
        setupContextMenu();
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshData());
        btnClearFilters.setOnAction(e -> handleClearFilters());
        btnClearForm.setOnAction(e -> handleClearForm());
        updateButtonStates();

        // Update amount column to always show EUR
        colAmount.setCellFactory(column -> new TableCell<Transaction, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction transaction = getTableRow().getItem();
                    if (transaction != null) {
                        String formattedAmount = String.format("€%.2f", amount);
                        if (transaction.isIncome()) {
                            setText(formattedAmount);
                            getStyleClass().removeAll("negative");
                            if (!getStyleClass().contains("positive")) getStyleClass().add("positive");
                        } else {
                            setText("-" + formattedAmount);
                            getStyleClass().removeAll("positive");
                            if (!getStyleClass().contains("negative")) getStyleClass().add("negative");
                        }
                    }
                }
            }
        });
    }

    private void setupDatePickers() {
        // Set up date format
        javafx.util.StringConverter<LocalDate> dateConverter = new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return DateTimeUtils.formatDate(date);
            }

            @Override
            public LocalDate fromString(String string) {
                return DateTimeUtils.parseDate(string);
            }
        };
        
        dpDate.setConverter(dateConverter);
        dpStartDate.setConverter(dateConverter);
        dpEndDate.setConverter(dateConverter);
        
        // Disable future dates in dpDate
        dpDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });
        
        // Add listeners for date range changes
        dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(column -> new TableCell<Transaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty ? null : DateTimeUtils.formatDate(date));
            }
        });
        
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        colTags.setCellValueFactory(cellData -> {
            String tags = cellData.getValue().getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(tags);
        });

        // Scheduled indicator column
        colScheduled.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isScheduled()));
        colScheduled.setCellFactory(column -> new TableCell<Transaction, Boolean>() {
            @Override
            protected void updateItem(Boolean scheduled, boolean empty) {
                super.updateItem(scheduled, empty);
                if (empty || scheduled == null) {
                    setText(null);
                    setGraphic(null);
                } else if (scheduled) {
                    setText("\u23F0"); // Unicode alarm clock as indicator
                    setTooltip(new Tooltip("Scheduled/Recurring Transaction"));
                } else {
                    setText("");
                    setTooltip(null);
                }
            }
        });

        table.setItems(transactions);
    }

    private void setupCategoryFilter() {
        updateCategoryFilter();
        cbCategory.setOnAction(e -> refreshData());
        cbIncludeSubcategories.selectedProperty().addListener((obs, oldVal, newVal) -> updateCategoryFilter());
    }
    
    private void updateCategoryFilter() {
        boolean includeSubcategories = cbIncludeSubcategories.isSelected();
        List<Tag> availableTags;
        
        if (includeSubcategories) {
            // Include all tags (root and subcategories)
            availableTags = tagService.findAll();
        } else {
            // Only include root tags
            availableTags = tagService.findRootTags();
        }
        
        Tag allOption = new Tag("All Categories");
        allOption.setId(null);
        List<Tag> comboTags = new ArrayList<>();
        comboTags.add(allOption);
        comboTags.addAll(availableTags);
        
        Tag currentSelection = cbCategory.getValue();
        cbCategory.setItems(FXCollections.observableArrayList(comboTags));
        
        // Try to maintain the current selection if it's still available
        if (currentSelection != null) {
            if ("All Categories".equals(currentSelection.getName())) {
                cbCategory.setValue(allOption);
            } else if (availableTags.contains(currentSelection)) {
                cbCategory.setValue(currentSelection);
            } else {
                cbCategory.setValue(allOption);
            }
        } else {
            cbCategory.setValue(allOption);
        }
        
        cbCategory.setConverter(new StringConverter<Tag>() {
            @Override
            public String toString(Tag tag) {
                return tag != null ? tag.getName() : "";
            }
            @Override
            public Tag fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
    }

    private void setupFilters() {
        cbIncludeSubcategories.setSelected(true);
        cbIncludeSubcategories.selectedProperty().addListener((obs, oldVal, newVal) -> refreshData());
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editMenuItem = new MenuItem("Edit");
        MenuItem deleteMenuItem = new MenuItem("Delete");
        
        editMenuItem.setOnAction(e -> handleEditTransaction());
        deleteMenuItem.setOnAction(e -> handleDeleteTransaction());
        
        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
        table.setContextMenu(contextMenu);
    }

    private void setupTagsUI() {
        cbTags.setItems(FXCollections.observableArrayList(tagService.findAll()));
        cbTags.setOnAction(e -> {
            Tag selected = cbTags.getValue();
            if (selected != null) {
                selectedTags.add(selected);
                updateSelectedTagsList();
                cbTags.setValue(null);
            }
        });
    }

    private void updateSelectedTagsList() {
        flowSelectedTags.getChildren().clear();
        for (Tag tag : selectedTags) {
            Label tagLabel = new Label(tag.getName());
            tagLabel.getStyleClass().add("tag-label");
            tagLabel.setOnMouseClicked(event -> {
                selectedTags.remove(tag);
                updateSelectedTagsList();
                refreshData();
            });
            flowSelectedTags.getChildren().add(tagLabel);
        }
    }

    @Override
    protected void loadData() {
        setupDatePickers();
        // Initialize date pickers
        List<Transaction> all = (currentUser == null) ? transactionService.findAll() : transactionService.findAllForUser(currentUser);
        LocalDate minDate = all.stream().map(Transaction::getDate).min(LocalDate::compareTo).orElse(LocalDate.now().minusYears(10));
        LocalDate maxDate = all.stream().map(Transaction::getDate).max(LocalDate::compareTo).orElse(LocalDate.now().plusYears(10));
        dpDate.setValue(LocalDate.now());
        dpStartDate.setValue(minDate);
        dpEndDate.setValue(maxDate);
        refreshData();
    }

    public void refreshData() {
        if (currentUser == null) return;
        loadTransactions();
        updateStatistics();
    }

    public void refreshTags() {
        if (tagService != null) {
            // Preserve selection
            List<Tag> allTags = tagService.findAll();
            Tag selectedInCb = cbTags.getValue();

            cbTags.setItems(FXCollections.observableArrayList(allTags));
            
            // Update category filter using the new method
            updateCategoryFilter();

            if (selectedInCb != null && allTags.contains(selectedInCb)) {
                cbTags.setValue(selectedInCb);
            }
        }
    }

    private void loadTransactions() {
        String searchTerm = tfSearch.getText();
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        Tag category = cbCategory.getValue();
        boolean includeSubcategories = cbIncludeSubcategories.isSelected();

        // Handle "All Categories" option
        if (category != null && "All Categories".equals(category.getName())) {
            category = null;
        }

        List<Transaction> filteredTransactions = transactionService.findTransactions(
            currentUser, searchTerm, startDate, endDate, category, includeSubcategories);
        
        transactions.setAll(filteredTransactions);
    }

    private void updateStatistics() {
        // Calculate totals based on the currently filtered transactions
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else {
                totalExpense = totalExpense.add(transaction.getAmount());
            }
        }
        
        BigDecimal balance = totalIncome.subtract(totalExpense);
        
        lblTotalIncome.setText(String.format("€%.2f", totalIncome));
        lblTotalExpense.setText(String.format("€%.2f", totalExpense));
        lblBalance.setText(String.format("€%.2f", balance));
    }

    @FXML
    private void handleAddTransaction() {
        if (currentUser == null) {
            showError("No User", "Please log in to create transactions.");
            return;
        }
        String amountText = tfAmount.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            showError("Invalid Amount", "Amount cannot be empty. Please enter a value.");
            return;
        }
        LocalDate selectedDate = dpDate.getValue();
        if (selectedDate != null && selectedDate.isAfter(LocalDate.now())) {
            showError("Invalid Date", "Cannot save a transaction with a future date.");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountText);
            Transaction transaction = transactionService.createTransaction(
                currentUser,
                dpDate.getValue(),
                tfDesc.getText(),
                amount,
                cbIncome.isSelected(),
                selectedTags.stream().map(Tag::getId).collect(Collectors.toSet())
            );
            transactions.add(0, transaction);
            clearForm();
        } catch (NumberFormatException e) {
            showError("Invalid Amount", "Please enter a valid number for the amount.");
        } catch (Exception e) {
            showError("Error", "Failed to create transaction: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditTransaction() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            isEditMode = true;
            dpDate.setValue(selected.getDate());
            tfDesc.setText(selected.getDescription());
            tfAmount.setText(selected.getAmount().toString());
            cbIncome.setSelected(selected.isIncome());
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
            updateButtonStates();
        }
    }

    @FXML
    private void handleDeleteTransaction() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                transactionService.deleteTransaction(selected.getId());
                transactions.remove(selected);
                refreshData();
                
                // Refresh all views including dashboard
                if (mainController != null) {
                    mainController.refreshAllViews();
                }
            } catch (Exception e) {
                showError("Error", "Failed to delete transaction: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearFilters() {
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        tfSearch.clear();
        
        // Reset to "All Categories"
        Tag allOption = new Tag("All Categories");
        allOption.setId(null);
        cbCategory.setValue(allOption);
        cbIncludeSubcategories.setSelected(true);
        refreshData();
    }

    @FXML
    private void handleSaveTransaction() {
        String amountText = tfAmount.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            showError("Invalid Amount", "Amount cannot be empty. Please enter a value.");
            return;
        }
        LocalDate selectedDate = dpDate.getValue();
        if (selectedDate != null && selectedDate.isAfter(LocalDate.now())) {
            showError("Invalid Date", "Cannot save a transaction with a future date.");
            return;
        }
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a transaction to edit.");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountText);
            transactionService.updateTransaction(
                selected.getId(),
                dpDate.getValue(),
                tfDesc.getText(),
                amount,
                cbIncome.isSelected(),
                selectedTags.stream().map(Tag::getId).collect(Collectors.toSet())
            );
            refreshData();
            clearForm();
        } catch (NumberFormatException e) {
            showError("Invalid Amount", "Please enter a valid number for the amount.");
        } catch (Exception e) {
            showError("Error", "Failed to update transaction: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        isEditMode = false;
        dpDate.setValue(LocalDate.now());
        tfDesc.clear();
        tfAmount.clear();
        cbIncome.setSelected(false);
        selectedTags.clear();
        updateSelectedTagsList();
        updateButtonStates();
    }

    private void updateButtonStates() {
        btnAdd.setVisible(!isEditMode);
        btnAdd.setManaged(!isEditMode);
        btnSave.setVisible(isEditMode);
        btnSave.setManaged(isEditMode);
        btnCancel.setVisible(isEditMode);
        btnCancel.setManaged(isEditMode);
        btnClearForm.setVisible(!isEditMode);
        btnClearForm.setManaged(!isEditMode);
        
        // Debug: Check if btnGenerateScheduled is enabled and visible
        System.out.println("btnGenerateScheduled - Visible: " + btnGenerateScheduled.isVisible() + 
                          ", Disabled: " + btnGenerateScheduled.isDisabled() + 
                          ", Managed: " + btnGenerateScheduled.isManaged());
    }

    @FXML
    private void handleCancelEdit() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Restore the original transaction data
            dpDate.setValue(selected.getDate());
            tfDesc.setText(selected.getDescription());
            tfAmount.setText(selected.getAmount().toString());
            cbIncome.setSelected(selected.isIncome());
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
        }
        clearForm();
    }

    @FXML
    private void handleGenerateScheduled() {
        System.out.println("=== Generate Scheduled Transactions Button Clicked ===");
        
        if (currentUser == null) {
            System.out.println("ERROR: currentUser is null");
            showError("No User", "Please log in to generate scheduled transactions.");
            return;
        }
        
        System.out.println("Current user: " + currentUser.getUsername());
        
        // Debug: Check if there are any scheduled transactions for the user
        try {
            List<ScheduledTransaction> userScheduled = scheduledTransactionService.findByUser(currentUser);
            System.out.println("Found " + userScheduled.size() + " scheduled transactions for user");
            
            if (userScheduled.isEmpty()) {
                System.out.println("No scheduled transactions found - showing warning");
                showWarning("No Scheduled Transactions", "You don't have any scheduled transactions to generate. Please create some scheduled transactions first.");
                return;
            }
            
            // List the scheduled transactions for debugging
            for (ScheduledTransaction st : userScheduled) {
                System.out.println("Scheduled transaction: " + st.getDescription() + " - " + st.getAmount() + " - " + st.getStartDate());
            }
            
            LocalDate untilDate = LocalDate.now().plusMonths(1);
            System.out.println("Generating transactions until: " + untilDate);
            
            // Check current transaction count before generation
            List<Transaction> beforeTransactions = transactionService.findAllForUser(currentUser);
            System.out.println("Transactions before generation: " + beforeTransactions.size());
            
            scheduledTransactionService.generateTransactionsForUser(currentUser, untilDate);
            System.out.println("Generation completed successfully");
            
            // Check transaction count after generation
            List<Transaction> afterTransactions = transactionService.findAllForUser(currentUser);
            System.out.println("Transactions after generation: " + afterTransactions.size());
            System.out.println("New transactions created: " + (afterTransactions.size() - beforeTransactions.size()));
            
            // List any new transactions
            if (afterTransactions.size() > beforeTransactions.size()) {
                System.out.println("New transactions created:");
                for (int i = beforeTransactions.size(); i < afterTransactions.size(); i++) {
                    Transaction t = afterTransactions.get(i);
                    System.out.println("  - " + t.getDescription() + " - " + t.getAmount() + " - " + t.getDate() + " - Scheduled: " + t.isScheduled());
                }
            }
            
            refreshData();
            System.out.println("Data refreshed - current transaction list size: " + transactions.size());
            
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Generated " + (afterTransactions.size() - beforeTransactions.size()) + " scheduled transactions up to " + untilDate);
            successAlert.showAndWait();
            
        } catch (Exception e) {
            System.out.println("ERROR during generation: " + e.getMessage());
            e.printStackTrace();
            showError("Generation Error", "Failed to generate scheduled transactions: " + e.getMessage());
        }
    }

    @FXML
    private void handleTableSelection() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            isEditMode = true;
            dpDate.setValue(selected.getDate());
            tfDesc.setText(selected.getDescription());
            tfAmount.setText(selected.getAmount().toString());
            cbIncome.setSelected(selected.isIncome());
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
            updateButtonStates();
        }
    }
}
