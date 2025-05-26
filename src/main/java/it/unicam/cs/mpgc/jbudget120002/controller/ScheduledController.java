package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
                scheduledService.deleteScheduledTransaction(selected.getId());
                transactions.remove(selected);
            } catch (Exception e) {
                showError("Error", "Failed to delete scheduled transaction: " + e.getMessage());
            }
        }
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