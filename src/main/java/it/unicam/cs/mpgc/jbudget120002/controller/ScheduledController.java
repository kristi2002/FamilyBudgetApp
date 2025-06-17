package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import it.unicam.cs.mpgc.jbudget120002.service.UserSettingsService;
import it.unicam.cs.mpgc.jbudget120002.service.DeadlineService;
import it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils;
import it.unicam.cs.mpgc.jbudget120002.util.CurrencyUtils;
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

    private ScheduledTransactionService scheduledService;
    private TagService tagService;
    private UserSettingsService settingsService;
    private ObservableList<ScheduledTransaction> transactions;
    private Set<Tag> selectedTags;
    private String currentCurrency;

    @Override
    protected void initializeServices() {
        scheduledService = serviceFactory.getScheduledTransactionService();
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
        // Initialize date pickers with custom format
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));

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

        dpStartDate.setConverter(dateConverter);
        dpEndDate.setConverter(dateConverter);

        // Setup recurrence patterns
        cbPattern.setItems(FXCollections.observableArrayList(
            ScheduledTransaction.RecurrencePattern.values()));

        // Setup table columns
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colStartDate.setCellFactory(column -> new TableCell<ScheduledTransaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(DateTimeUtils.formatDate(date));
                }
            }
        });

        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colEndDate.setCellFactory(column -> new TableCell<ScheduledTransaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(DateTimeUtils.formatDate(date));
                }
            }
        });

        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Setup amount column with currency formatting
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(column -> new TableCell<ScheduledTransaction, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount, currentCurrency));
                }
            }
        });

        colTags.setCellValueFactory(cellData -> {
            String tags = cellData.getValue().getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(tags);
        });

        colPattern.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRecurrencePattern().toString()));

        // Setup amount field prompt text with currency symbol
        tfAmount.setPromptText("Amount (" + CurrencyUtils.getSymbol(currentCurrency) + ")");

        // Setup tags combobox
        cbTags.setItems(FXCollections.observableArrayList(tagService.findRootTags()));
        cbTags.setOnAction(e -> {
            Tag selected = cbTags.getValue();
            if (selected != null) {
                selectedTags.add(selected);
                updateSelectedTagsList();
                cbTags.setValue(null);
            }
        });

        table.setItems(transactions);
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        transactions.setAll(scheduledService.findAll());
    }

    @FXML
    private void handleAddScheduled() {
        try {
            if (cbPattern.getValue() == null) {
                showWarning("Invalid Input", "Please select a recurrence pattern.");
                return;
            }

            BigDecimal amount = new BigDecimal(tfAmount.getText());
            int recurrenceValue = Integer.parseInt(tfRecurrenceValue.getText());

            ScheduledTransaction scheduled = scheduledService.createScheduledTransaction(
                tfDesc.getText(),
                amount,
                cbIncome.isSelected(),
                dpStartDate.getValue(),
                dpEndDate.getValue(),
                cbPattern.getValue(),
                recurrenceValue,
                selectedTags.stream().map(Tag::getId).collect(Collectors.toSet())
            );
            
            transactions.add(scheduled);
            // Create deadlines for all occurrences if not income
            if (!scheduled.isIncome()) {
                var currentDate = new Object() { LocalDate date = scheduled.getStartDate(); };
                LocalDate endDate = scheduled.getEndDate() != null ? scheduled.getEndDate() : currentDate.date.plusYears(1);
                DeadlineService deadlineService = serviceFactory.getDeadlineService();
                String category = scheduled.getCategory();
                if (category == null || category.isEmpty()) {
                    // Use the first tag's name as category if available
                    if (!scheduled.getTags().isEmpty()) {
                        category = scheduled.getTags().iterator().next().getName();
                    }
                }
                while (!currentDate.date.isAfter(endDate)) {
                    deadlineService.create(
                        new it.unicam.cs.mpgc.jbudget120002.model.Deadline(
                            scheduled.getDescription(),
                            currentDate.date,
                            scheduled.getAmount().doubleValue(),
                            false, // isPaid
                            null, // relatedTransaction
                            category
                        )
                    );
                    // Calculate next occurrence based on pattern
                    currentDate.date = switch (scheduled.getRecurrencePattern()) {
                        case DAILY -> currentDate.date.plusDays(scheduled.getRecurrenceValue());
                        case WEEKLY -> currentDate.date.plusWeeks(scheduled.getRecurrenceValue());
                        case MONTHLY -> currentDate.date.plusMonths(scheduled.getRecurrenceValue());
                        case YEARLY -> currentDate.date.plusYears(scheduled.getRecurrenceValue());
                    };
                }
            }
            clearForm();
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter valid numbers for amount and recurrence value.");
        } catch (Exception e) {
            showError("Error", "Failed to add scheduled transaction: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteScheduled() {
        ScheduledTransaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Delete all related deadlines first
                if (!selected.isIncome()) {
                    DeadlineService deadlineService = serviceFactory.getDeadlineService();
                    var currentDate = new Object() { LocalDate date = selected.getStartDate(); };
                    LocalDate endDate = selected.getEndDate() != null ? selected.getEndDate() : currentDate.date.plusYears(1);
                    
                    while (!currentDate.date.isAfter(endDate)) {
                        deadlineService.findAll().stream()
                            .filter(d -> d.getDescription().equals(selected.getDescription()) 
                                && d.getDueDate().equals(currentDate.date))
                            .forEach(d -> deadlineService.delete(d.getId()));
                            
                        currentDate.date = switch (selected.getRecurrencePattern()) {
                            case DAILY -> currentDate.date.plusDays(selected.getRecurrenceValue());
                            case WEEKLY -> currentDate.date.plusWeeks(selected.getRecurrenceValue());
                            case MONTHLY -> currentDate.date.plusMonths(selected.getRecurrenceValue());
                            case YEARLY -> currentDate.date.plusYears(selected.getRecurrenceValue());
                        };
                    }
                }
                
                scheduledService.deleteScheduledTransaction(selected.getId());
                transactions.remove(selected);
            } catch (Exception e) {
                showError("Error", "Failed to delete scheduled transaction: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditScheduled() {
        ScheduledTransaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a scheduled transaction to edit.");
            return;
        }

        // Show edit dialog
        Dialog<ScheduledTransaction> dialog = new Dialog<>();
        dialog.setTitle("Edit Scheduled Transaction");
        dialog.setHeaderText(null);

        // Create the custom dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField tfDesc = new TextField(selected.getDescription());
        TextField tfAmount = new TextField(selected.getAmount().toString());
        DatePicker dpStartDate = new DatePicker(selected.getStartDate());
        DatePicker dpEndDate = new DatePicker(selected.getEndDate());
        CheckBox cbIncome = new CheckBox("Income");
        cbIncome.setSelected(selected.isIncome());
        ComboBox<ScheduledTransaction.RecurrencePattern> cbPattern = 
            new ComboBox<>(FXCollections.observableArrayList(ScheduledTransaction.RecurrencePattern.values()));
        cbPattern.setValue(selected.getRecurrencePattern());
        TextField tfRecurrenceValue = new TextField(String.valueOf(selected.getRecurrenceValue()));

        grid.add(new Label("Description:"), 0, 0);
        grid.add(tfDesc, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(tfAmount, 1, 1);
        grid.add(new Label("Start Date:"), 0, 2);
        grid.add(dpStartDate, 1, 2);
        grid.add(new Label("End Date:"), 0, 3);
        grid.add(dpEndDate, 1, 3);
        grid.add(cbIncome, 1, 4);
        grid.add(new Label("Pattern:"), 0, 5);
        grid.add(cbPattern, 1, 5);
        grid.add(new Label("Recurrence Value:"), 0, 6);
        grid.add(tfRecurrenceValue, 1, 6);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    BigDecimal amount = new BigDecimal(tfAmount.getText());
                    int recurrenceValue = Integer.parseInt(tfRecurrenceValue.getText());
                    
                    // Update the scheduled transaction
                    scheduledService.updateScheduledTransaction(
                        selected.getId(),
                        tfDesc.getText(),
                        amount,
                        cbIncome.isSelected(),
                        dpStartDate.getValue(),
                        dpEndDate.getValue(),
                        cbPattern.getValue(),
                        recurrenceValue,
                        selectedTags.stream().map(Tag::getId).collect(Collectors.toSet())
                    );

                    // Update related deadlines
                    if (!selected.isIncome()) {
                        DeadlineService deadlineService = serviceFactory.getDeadlineService();
                        
                        // Delete old deadlines
                        var oldDate = new Object() { LocalDate date = selected.getStartDate(); };
                        LocalDate oldEndDate = selected.getEndDate() != null ? selected.getEndDate() : oldDate.date.plusYears(1);
                        while (!oldDate.date.isAfter(oldEndDate)) {
                            deadlineService.findAll().stream()
                                .filter(d -> d.getDescription().equals(selected.getDescription()) 
                                    && d.getDueDate().equals(oldDate.date))
                                .forEach(d -> deadlineService.delete(d.getId()));
                                
                            oldDate.date = switch (selected.getRecurrencePattern()) {
                                case DAILY -> oldDate.date.plusDays(selected.getRecurrenceValue());
                                case WEEKLY -> oldDate.date.plusWeeks(selected.getRecurrenceValue());
                                case MONTHLY -> oldDate.date.plusMonths(selected.getRecurrenceValue());
                                case YEARLY -> oldDate.date.plusYears(selected.getRecurrenceValue());
                            };
                        }
                        
                        // Create new deadlines
                        LocalDate newDate = dpStartDate.getValue();
                        LocalDate newEndDate = dpEndDate.getValue() != null ? dpEndDate.getValue() : newDate.plusYears(1);
                        while (!newDate.isAfter(newEndDate)) {
                            deadlineService.create(
                                new it.unicam.cs.mpgc.jbudget120002.model.Deadline(
                                    tfDesc.getText(),
                                    newDate,
                                    amount.doubleValue(),
                                    false,
                                    null,
                                    selected.getCategory()
                                )
                            );
                            
                            newDate = switch (cbPattern.getValue()) {
                                case DAILY -> newDate.plusDays(recurrenceValue);
                                case WEEKLY -> newDate.plusWeeks(recurrenceValue);
                                case MONTHLY -> newDate.plusMonths(recurrenceValue);
                                case YEARLY -> newDate.plusYears(recurrenceValue);
                            };
                        }
                    }
                    
                    return selected;
                } catch (NumberFormatException e) {
                    showError("Invalid Input", "Please enter valid numbers for amount and recurrence value.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            refreshData();
        });
    }

    private void clearForm() {
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));
        tfDesc.clear();
        tfAmount.clear();
        cbIncome.setSelected(false);
        cbPattern.setValue(null);
        tfRecurrenceValue.clear();
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
            });
            tagBox.getChildren().addAll(tagLabel, removeBtn);
            flowSelectedTags.getChildren().add(tagBox);
        }
    }
} 