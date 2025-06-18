package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import it.unicam.cs.mpgc.jbudget120002.service.TransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsService;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils;
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

    @Override
    protected void initializeServices() {
        transactionService = serviceFactory.getTransactionService();
        tagService = serviceFactory.getTagService();
        settingsService = serviceFactory.getUserSettingsService();
        scheduledTransactionService = serviceFactory.getScheduledTransactionService();
        transactions = FXCollections.observableArrayList();
        selectedTags = new HashSet<>();
    }

    @Override
    protected void setupUI() {
        setupDatePickers();
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
        // Initialize date pickers
        List<Transaction> all = transactionService.findAll();
        LocalDate minDate = all.stream().map(Transaction::getDate).min(LocalDate::compareTo).orElse(LocalDate.now().minusYears(10));
        LocalDate maxDate = all.stream().map(Transaction::getDate).max(LocalDate::compareTo).orElse(LocalDate.now().plusYears(10));
        dpDate.setValue(LocalDate.now());
        dpStartDate.setValue(minDate);
        dpEndDate.setValue(maxDate);
        
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
        cbCategory.setItems(FXCollections.observableArrayList(tagService.findRootTags()));
        cbCategory.setOnAction(e -> refreshData());
        cbIncludeSubcategories.selectedProperty().addListener((obs, oldVal, newVal) -> refreshData());
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
            HBox tagBox = new HBox(5);
            tagBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 3 5; -fx-background-radius: 3;");
            Label tagLabel = new Label(tag.getName());
            Button removeBtn = new Button("×");
            removeBtn.setStyle("-fx-padding: 0 3; -fx-background-radius: 2; -fx-min-width: 16;");
            removeBtn.setOnAction(e -> {
                selectedTags.remove(tag);
                updateSelectedTagsList();
            });
            tagBox.getChildren().addAll(tagLabel, removeBtn);
            flowSelectedTags.getChildren().add(tagBox);
        }
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        String searchText = tfSearch.getText().trim();
        Tag selectedCategory = cbCategory.getValue();
        boolean includeSubcategories = cbIncludeSubcategories.isSelected();

        List<Transaction> filteredTransactions;
        if (selectedCategory != null) {
            filteredTransactions = transactionService.findByTag(selectedCategory, includeSubcategories);
        } else {
            filteredTransactions = transactionService.findAll();
        }

        // Apply date and search filters
        filteredTransactions = filteredTransactions.stream()
            .filter(t -> (startDate == null || !t.getDate().isBefore(startDate)) &&
                        (endDate == null || !t.getDate().isAfter(endDate)) &&
                        (searchText.isEmpty() || t.getDescription().toLowerCase().contains(searchText.toLowerCase())))
            .collect(Collectors.toList());

        transactions.setAll(filteredTransactions);
        updateStatistics();
    }

    private void updateStatistics() {
        BigDecimal totalIncome = transactionService.calculateIncomeForPeriod(
            dpStartDate.getValue(), 
            dpEndDate.getValue()
        );
        
        BigDecimal totalExpense = transactionService.calculateExpensesForPeriod(
            dpStartDate.getValue(), 
            dpEndDate.getValue()
        );
        
        BigDecimal balance = totalIncome.subtract(totalExpense);
        
        lblTotalIncome.setText(String.format("€%.2f", totalIncome));
        lblTotalExpense.setText(String.format("€%.2f", totalExpense));
        lblBalance.setText(String.format("€%.2f", balance));
    }

    @FXML
    private void handleAddTransaction() {
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
        cbCategory.setValue(null);
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
        List<it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction> scheduled = scheduledTransactionService.findAll();
        for (var st : scheduled) {
            scheduledTransactionService.generateTransactions(st.getId(), LocalDate.now());
        }
        refreshData();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Scheduled transactions generated up to today.", ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Generation Complete");
        alert.showAndWait();
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
