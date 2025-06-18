package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledTransactionsController extends BaseController {
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField tfDesc;
    @FXML private TextField tfAmount;
    @FXML private CheckBox cbIncome;
    @FXML private ComboBox<Tag> cbTags;
    @FXML private ListView<Tag> lvSelectedTags;
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
    private ObservableList<ScheduledTransaction> transactions;
    private Set<Tag> selectedTags;

    @Override
    protected void initializeServices() {
        scheduledService = serviceFactory.getScheduledTransactionService();
        tagService = serviceFactory.getTagService();
        transactions = FXCollections.observableArrayList();
        selectedTags = new HashSet<>();
    }

    @Override
    protected void setupUI() {
        // Initialize date pickers
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));

        // Setup recurrence patterns
        cbPattern.setItems(FXCollections.observableArrayList(
            ScheduledTransaction.RecurrencePattern.values()));

        // Setup table columns
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTags.setCellValueFactory(cellData -> {
            String tags = cellData.getValue().getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(tags);
        });
        colPattern.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPattern().toString()));

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

        // Setup selected tags list view
        lvSelectedTags.setItems(FXCollections.observableArrayList());
        lvSelectedTags.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tag tag, boolean empty) {
                super.updateItem(tag, empty);
                if (empty || tag == null) {
                    setText(null);
                } else {
                    setText(tag.getName());
                }
            }
        });

        // Add context menu to remove tags
        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setOnAction(e -> {
            Tag selected = lvSelectedTags.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedTags.remove(selected);
                updateSelectedTagsList();
            }
        });
        contextMenu.getItems().add(removeItem);
        lvSelectedTags.setContextMenu(contextMenu);

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
        selectedTags.clear();
        updateSelectedTagsList();
        cbPattern.setValue(null);
        tfRecurrenceValue.clear();
    }

    private void updateSelectedTagsList() {
        lvSelectedTags.setItems(FXCollections.observableArrayList(selectedTags));
    }
}
