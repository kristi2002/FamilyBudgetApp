package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import it.unicam.cs.mpgc.jbudget120002.service.TransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsService;
import it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils;
import it.unicam.cs.mpgc.jbudget120002.util.CurrencyUtils;
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
    @FXML private ComboBox<Tag> cbTags;
    @FXML private FlowPane flowSelectedTags;
    
    // Advanced filtering controls
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbCurrency;
    @FXML private CheckBox cbIncludeChildTags;
    @FXML private CheckBox cbMatchAllTags;
    @FXML private TextField tfSearch;
    @FXML private Button btnClearFilters;
    @FXML private Button btnSave;
    @FXML private Button btnClearForm;
    
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

    private TransactionService transactionService;
    private TagService tagService;
    private UserSettingsService settingsService;
    private ObservableList<Transaction> transactions;
    private Set<Tag> selectedTags;
    private String currentCurrency;

    @Override
    protected void initializeServices() {
        transactionService = serviceFactory.getTransactionService();
        tagService = serviceFactory.getTagService();
        settingsService = serviceFactory.getUserSettingsService();
        transactions = FXCollections.observableArrayList();
        selectedTags = new HashSet<>();
        
        // Load currency setting
        settingsService.findFirst().ifPresent(settings -> 
            currentCurrency = settings.getCurrency()
        );
        if (currentCurrency == null) currentCurrency = "EUR";
    }

    @Override
    protected void setupUI() {
        setupDatePickers();
        setupTable();
        setupTagsUI();
        setupFilters();
        setupContextMenu();
        btnSave = new Button("Save Changes");
        btnSave.setOnAction(e -> handleSaveTransaction());
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshData());
        btnClearFilters.setOnAction(e -> handleClearFilters());
        btnClearForm.setOnAction(e -> handleClearForm());
    }

    private void setupDatePickers() {
        // Initialize date pickers
        dpDate.setValue(LocalDate.now());
        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());
        
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
        colAmount.setCellFactory(column -> new TableCell<Transaction, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    Transaction transaction = getTableRow().getItem();
                    if (transaction != null) {
                        String formattedAmount = CurrencyUtils.formatAmount(amount, currentCurrency);
                        setText(transaction.isIncome() ? formattedAmount : "-" + formattedAmount);
                    }
                }
            }
        });
        
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

    private void setupTagsUI() {
        cbTags.setItems(FXCollections.observableArrayList(tagService.findRootTags()));
        cbTags.setOnAction(e -> {
            Tag selected = cbTags.getValue();
            if (selected != null) {
                selectedTags.add(selected);
                updateSelectedTagsList();
                cbTags.setValue(null);
            }
        });
    }

    private void setupFilters() {
        cbCurrency.setItems(FXCollections.observableArrayList("EUR", "USD", "GBP"));
        cbCurrency.setValue(currentCurrency);
        cbCurrency.setOnAction(e -> {
            currentCurrency = cbCurrency.getValue();
            refreshData();
        });
        
        cbIncludeChildTags.setSelected(true);
        cbMatchAllTags.setSelected(false);
        
        cbIncludeChildTags.selectedProperty().addListener((obs, oldVal, newVal) -> refreshData());
        cbMatchAllTags.selectedProperty().addListener((obs, oldVal, newVal) -> refreshData());
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editMenuItem = new MenuItem("Edit");
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem manageTagsMenuItem = new MenuItem("Manage Tags");
        
        editMenuItem.setOnAction(e -> handleEditTransaction());
        deleteMenuItem.setOnAction(e -> handleDeleteTransaction());
        manageTagsMenuItem.setOnAction(e -> handleManageTags());
        
        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem, new SeparatorMenuItem(), manageTagsMenuItem);
        table.setContextMenu(contextMenu);
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        List<Transaction> filteredTransactions;
        if (selectedTags.isEmpty()) {
            filteredTransactions = transactionService.findByDateRange(
                dpStartDate.getValue(),
                dpEndDate.getValue()
            );
        } else {
            filteredTransactions = transactionService.findByTags(
                selectedTags,
                cbMatchAllTags.isSelected()
            );
            // Apply date filter
            filteredTransactions = filteredTransactions.stream()
                .filter(t -> !t.getDate().isBefore(dpStartDate.getValue())
                    && !t.getDate().isAfter(dpEndDate.getValue()))
                .collect(Collectors.toList());
        }
        // Apply search filter
        String searchText = tfSearch.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.toLowerCase();
            filteredTransactions = filteredTransactions.stream()
                .filter(t -> t.getDescription().toLowerCase().contains(lower)
                    || t.getAmount().toPlainString().contains(lower))
                .collect(Collectors.toList());
        }
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
        
        lblTotalIncome.setText(CurrencyUtils.formatAmount(totalIncome, currentCurrency));
        lblTotalExpense.setText(CurrencyUtils.formatAmount(totalExpense, currentCurrency));
        lblBalance.setText(CurrencyUtils.formatAmount(balance, currentCurrency));
    }

    @FXML
    private void handleAddTransaction() {
        String amountText = tfAmount.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            showError("Invalid Amount", "Amount cannot be empty. Please enter a value.");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountText.trim());
            Transaction transaction = transactionService.createTransaction(
                dpDate.getValue(),
                tfDesc.getText(),
                amount,
                cbIncome.isSelected(),
                selectedTags.stream().map(Tag::getId).collect(Collectors.toSet())
            );
            transactions.add(0, transaction);
            clearForm();
            refreshData();
        } catch (NumberFormatException e) {
            showError("Invalid Amount", "Please enter a valid number for the amount.");
        } catch (Exception e) {
            showError("Error", "Failed to add transaction: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditTransaction() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dpDate.setValue(selected.getDate());
            tfDesc.setText(selected.getDescription());
            tfAmount.setText(selected.getAmount().toString());
            cbIncome.setSelected(selected.isIncome());
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
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
    private void handleManageTags() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
        }
    }

    @FXML
    private void handleClearFilters() {
        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());
        selectedTags.clear();
        updateSelectedTagsList();
        tfSearch.clear();
        cbIncludeChildTags.setSelected(true);
        cbMatchAllTags.setSelected(false);
        refreshData();
    }

    @FXML
    private void handleSaveTransaction() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a transaction to edit.");
            return;
        }

        String amountText = tfAmount.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            showError("Invalid Amount", "Amount cannot be empty. Please enter a value.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText.trim());
            System.out.println("Saving tags: " + selectedTags.stream().map(Tag::getId).collect(Collectors.toSet()));
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
            e.printStackTrace(); // Print stack trace for debugging
            showError("Error", "Failed to update transaction: " + e.getMessage());
        }
    }

    private void clearForm() {
        dpDate.setValue(LocalDate.now());
        tfDesc.clear();
        tfAmount.clear();
        cbIncome.setSelected(false);
        selectedTags.clear();
        updateSelectedTagsList();
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
                refreshData();
            });
            tagBox.getChildren().addAll(tagLabel, removeBtn);
            flowSelectedTags.getChildren().add(tagBox);
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }
}
