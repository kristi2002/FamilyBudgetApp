package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsService;
import it.unicam.cs.mpgc.jbudget120002.service.DeadlineService;
import it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Controller class managing scheduled and recurring transactions in the Family Budget App.
 * This class handles the creation, monitoring, and management of transactions that
 * occur on a regular basis or are scheduled for future execution.
 *
 * Responsibilities:
 * - Display and manage scheduled transactions
 * - Handle recurring transaction setup
 * - Monitor upcoming scheduled payments
 * - Process scheduled transaction execution
 * - Coordinate with TransactionService for scheduled operations
 *
 * Usage:
 * Used by MainController to manage the scheduled transactions tab and provide
 * automated transaction management functionality to users.
 */
public class ScheduledController extends BaseController {
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField tfDesc;
    @FXML private TextField tfAmount;
    @FXML private CheckBox cbIncome;
    @FXML private ComboBox<Tag> cbTags;
    @FXML private FlowPane flowSelectedTags;
    @FXML private ComboBox<ScheduledTransaction.RecurrencePattern> cbPattern;
    @FXML private TextField tfRecurrenceValue;
    @FXML private TableView<ScheduledTransaction> table;
    @FXML private TableColumn<ScheduledTransaction, LocalDate> colStartDate;
    @FXML private TableColumn<ScheduledTransaction, LocalDate> colEndDate;
    @FXML private TableColumn<ScheduledTransaction, String> colDesc;
    @FXML private TableColumn<ScheduledTransaction, BigDecimal> colAmount;
    @FXML private TableColumn<ScheduledTransaction, String> colTags;
    @FXML private TableColumn<ScheduledTransaction, String> colPattern;
    @FXML private Button btnGenerateScheduled;
    @FXML private Button btnAdd;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnClearForm;
    @FXML private Button btnClearFilters;
    @FXML private DatePicker dpFilterStartDate;
    @FXML private DatePicker dpFilterEndDate;
    @FXML private ComboBox<Tag> cbCategory;
    @FXML private CheckBox cbIncludeSubcategories;
    @FXML private TextField tfSearch;
    @FXML private Label lblTotalIncome;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblBalance;

    private ScheduledTransactionService scheduledService;
    private TagService tagService;
    private UserSettingsService settingsService;
    private DeadlineService deadlineService;
    private ObservableList<ScheduledTransaction> transactions;
    private Set<Tag> selectedTags;
    private boolean isEditMode = false;
    private it.unicam.cs.mpgc.jbudget120002.model.User currentUser;

    public void setCurrentUser(it.unicam.cs.mpgc.jbudget120002.model.User user) {
        this.currentUser = user;
    }

    @Override
    protected void initializeServices() {
        scheduledService = serviceFactory.getScheduledTransactionService(false);
        tagService = serviceFactory.getTagService(false);
        settingsService = serviceFactory.getUserSettingsService(false);
        deadlineService = serviceFactory.getDeadlineService(false);
        transactions = FXCollections.observableArrayList();
        selectedTags = new HashSet<>();
    }

    @Override
    protected void setupUI() {
        setupTable();
        setupDatePickers();
        setupPatternComboBox();
        setupCategoryFilter();
        setupFilters();
        setupContextMenu();
        setupTagsUI();
        
        // Setup table selection listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadScheduledTransactionToForm(newSelection);
            }
        });
        
        btnSave.setOnAction(e -> handleSaveScheduled());
        btnCancel.setOnAction(e -> handleCancelEdit());
        btnClearForm.setOnAction(e -> handleClearForm());
        btnClearFilters.setOnAction(e -> handleClearFilters());
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshData());
        
        updateButtonStates();
    }

    private void loadScheduledTransactionToForm(ScheduledTransaction transaction) {
        dpStartDate.setValue(transaction.getStartDate());
        dpEndDate.setValue(transaction.getEndDate());
        tfDesc.setText(transaction.getDescription());
        tfAmount.setText(transaction.getAmount().toString());
        cbIncome.setSelected(transaction.isIncome());
        cbPattern.setValue(transaction.getPattern());
        tfRecurrenceValue.setText(String.valueOf(transaction.getRecurrenceValue()));
        selectedTags.clear();
        selectedTags.addAll(transaction.getTags());
        updateSelectedTagsList();
    }

    private void setupDatePickers() {
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));
        dpFilterStartDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        dpFilterEndDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
    }

    private void setupTable() {
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colStartDate.setCellFactory(column -> new TableCell<ScheduledTransaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty ? null : DateTimeUtils.formatDate(date));
            }
        });
        
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colEndDate.setCellFactory(column -> new TableCell<ScheduledTransaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty ? null : DateTimeUtils.formatDate(date));
            }
        });
        
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(column -> new TableCell<ScheduledTransaction, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    ScheduledTransaction transaction = getTableRow().getItem();
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
        
        colPattern.setCellValueFactory(cellData -> {
            ScheduledTransaction st = cellData.getValue();
            ScheduledTransaction.RecurrencePattern pattern = st.getPattern();
            String value = (pattern != null ? st.getRecurrenceValue() + " " + pattern.toString() : "");
            return new SimpleStringProperty(value);
        });
        
        colTags.setCellValueFactory(cellData -> {
            String tags = cellData.getValue().getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(tags);
        });

        table.setItems(transactions);
    }

    private void setupPatternComboBox() {
        cbPattern.getItems().setAll(ScheduledTransaction.RecurrencePattern.values());
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
        MenuItem convertToDeadlineItem = new MenuItem("Convert to Deadline");
        
        editMenuItem.setOnAction(e -> handleEditScheduled());
        deleteMenuItem.setOnAction(e -> handleDeleteScheduled());
        convertToDeadlineItem.setOnAction(e -> handleConvertToDeadline());
        
        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem, new SeparatorMenuItem(), convertToDeadlineItem);
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

    @Override
    public void refreshData() {
        if (currentUser == null) return;
        // TODO: update all queries to use currentUser if needed
        LocalDate startDate = dpFilterStartDate.getValue();
        LocalDate endDate = dpFilterEndDate.getValue();
        String searchText = tfSearch.getText().trim();
        Tag selectedCategory = cbCategory.getValue();
        boolean includeSubcategories = cbIncludeSubcategories.isSelected();
        List<ScheduledTransaction> filteredTransactions;
        if (selectedCategory != null) {
            filteredTransactions = scheduledService.findByTag(selectedCategory, includeSubcategories);
        } else {
            filteredTransactions = scheduledService.findAll();
        }
        // Apply date and search filters
        filteredTransactions = filteredTransactions.stream()
            .filter(t -> (startDate == null || !t.getStartDate().isBefore(startDate)) &&
                        (endDate == null || !t.getEndDate().isAfter(endDate)) &&
                        (searchText.isEmpty() || t.getDescription().toLowerCase().contains(searchText.toLowerCase())))
            .collect(Collectors.toList());
        transactions.setAll(filteredTransactions);
        updateStatistics();
    }

    private void updateStatistics() {
        BigDecimal totalIncome = scheduledService.calculateIncomeForPeriod(
            dpFilterStartDate.getValue(), 
            dpFilterEndDate.getValue()
        );
        
        BigDecimal totalExpense = scheduledService.calculateExpensesForPeriod(
            dpFilterStartDate.getValue(), 
            dpFilterEndDate.getValue()
        );
        
        BigDecimal balance = totalIncome.subtract(totalExpense);
        
        lblTotalIncome.setText(String.format("€%.2f", totalIncome));
        lblTotalExpense.setText(String.format("€%.2f", totalExpense));
        lblBalance.setText(String.format("€%.2f", balance));
    }

    @FXML
    private void handleAddScheduled() {
        try {
            ScheduledTransaction.RecurrencePattern pattern = cbPattern.getValue();
            if (pattern == null) {
                showWarning("Invalid Input", "No recurrence pattern selected. Defaulting to MONTHLY.");
                pattern = ScheduledTransaction.RecurrencePattern.MONTHLY;
                cbPattern.setValue(pattern);
            }

            BigDecimal amount = new BigDecimal(tfAmount.getText());
            int recurrenceValue = Integer.parseInt(tfRecurrenceValue.getText());

            ScheduledTransaction scheduled = scheduledService.createScheduledTransaction(
                tfDesc.getText(),
                amount,
                cbIncome.isSelected(),
                dpStartDate.getValue(),
                dpEndDate.getValue(),
                pattern,
                recurrenceValue,
                selectedTags.stream().map(Tag::getId).collect(Collectors.toSet()),
                currentUser
            );
            
            transactions.add(scheduled);
            clearForm();
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter valid numbers for amount and recurrence value.");
        } catch (Exception e) {
            showError("Error", "Failed to add scheduled transaction: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditScheduled() {
        ScheduledTransaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            isEditMode = true;
            dpStartDate.setValue(selected.getStartDate());
            dpEndDate.setValue(selected.getEndDate());
            tfDesc.setText(selected.getDescription());
            tfAmount.setText(selected.getAmount().toString());
            cbIncome.setSelected(selected.isIncome());
            cbPattern.setValue(selected.getPattern());
            tfRecurrenceValue.setText(String.valueOf(selected.getRecurrenceValue()));
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
            updateButtonStates();
        }
    }

    @FXML
    private void handleSaveScheduled() {
        String desc = tfDesc.getText();
        BigDecimal amount;
        try {
            amount = new BigDecimal(tfAmount.getText());
        } catch (NumberFormatException e) {
            // Handle error: show alert
            return;
        }

        Set<Long> tagIds = selectedTags.stream().map(Tag::getId).collect(Collectors.toSet());

        if (isEditMode) {
            ScheduledTransaction selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                scheduledService.updateScheduledTransaction(
                    selected.getId(),
                    desc,
                    amount,
                    cbIncome.isSelected(),
                    dpStartDate.getValue(),
                    dpEndDate.getValue(),
                    cbPattern.getValue(),
                    Integer.parseInt(tfRecurrenceValue.getText()),
                    tagIds
                );
            }
        } else {
            scheduledService.createScheduledTransaction(
                desc,
                amount,
                cbIncome.isSelected(),
                dpStartDate.getValue(),
                dpEndDate.getValue(),
                cbPattern.getValue(),
                Integer.parseInt(tfRecurrenceValue.getText()),
                tagIds,
                currentUser
            );
        }
        refreshData();
        clearForm();
    }

    @FXML
    private void handleDeleteScheduled() {
        ScheduledTransaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            scheduledService.deleteScheduledTransaction(selected.getId());
            refreshData();
        }
    }

    @FXML
    private void handleClearFilters() {
        dpFilterStartDate.setValue(null);
        dpFilterEndDate.setValue(null);
        tfSearch.clear();
        cbCategory.setValue(null);
        cbIncludeSubcategories.setSelected(true);
        refreshData();
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleCancelEdit() {
        ScheduledTransaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dpStartDate.setValue(selected.getStartDate());
            dpEndDate.setValue(selected.getEndDate());
            tfDesc.setText(selected.getDescription());
            tfAmount.setText(selected.getAmount().toString());
            cbIncome.setSelected(selected.isIncome());
            cbPattern.setValue(selected.getPattern());
            tfRecurrenceValue.setText(String.valueOf(selected.getRecurrenceValue()));
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
        }
        clearForm();
    }

    private void clearForm() {
        isEditMode = false;
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));
        tfDesc.clear();
        tfAmount.clear();
        cbIncome.setSelected(false);
        cbPattern.setValue(null);
        tfRecurrenceValue.clear();
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
    private void handleGenerateScheduled() {
        List<ScheduledTransaction> scheduled = scheduledService.findAll();
        for (var st : scheduled) {
            scheduledService.generateTransactions(st.getId(), LocalDate.now());
        }
        refreshData();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Scheduled transactions generated up to today.", ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Generation Complete");
        alert.showAndWait();
    }

    @FXML
    private void handleConvertToDeadline() {
        ScheduledTransaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a scheduled transaction to convert to a deadline.");
            return;
        }

        // Create a dialog to get the due date for the deadline
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Convert to Deadline");
        dialog.setHeaderText("Set the due date for this deadline");
        dialog.setContentText("The deadline will be created based on the selected scheduled transaction.");

        // Set up the dialog content
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(selected.getStartDate()); // Default to start date
        dueDatePicker.setPromptText("Select due date");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Due Date:"), 0, 0);
        grid.add(dueDatePicker, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Set up buttons
        ButtonType convertButtonType = new ButtonType("Convert", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(convertButtonType, ButtonType.CANCEL);

        // Set the result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == convertButtonType) {
                return dueDatePicker.getValue();
            }
            return null;
        });

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(dueDate -> {
            try {
                // Create the deadline
                Deadline deadline = new Deadline();
                deadline.setDescription(selected.getDescription());
                deadline.setAmount(selected.getAmount().doubleValue());
                deadline.setDueDate(dueDate);
                deadline.setCategory(selected.getTags().isEmpty() ? "" : selected.getTags().iterator().next().getName());
                deadline.setPaid(false);
                deadline.setUser(currentUser);

                deadlineService.create(deadline);

                showInfo("Success", "Scheduled transaction '" + selected.getDescription() + "' has been converted to a deadline with due date " + dueDate + ".");

            } catch (Exception e) {
                showError("Error", "Failed to convert scheduled transaction to deadline: " + e.getMessage());
            }
        });
    }
} 