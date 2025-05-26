package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import it.unicam.cs.mpgc.jbudget120002.service.TransactionService;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TransactionsController extends BaseController {
    @FXML private DatePicker dpDate;
    @FXML private TextField tfDesc;
    @FXML private TextField tfAmount;
    @FXML private CheckBox cbIncome;
    @FXML private ComboBox<Tag> cbTags;
    @FXML private FlowPane flowSelectedTags;
    @FXML private TableView<Transaction> table;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colDesc;
    @FXML private TableColumn<Transaction, BigDecimal> colAmount;
    @FXML private TableColumn<Transaction, String> colTags;

    private TransactionService transactionService;
    private TagService tagService;
    private ObservableList<Transaction> transactions;
    private Set<Tag> selectedTags;

    @Override
    protected void initializeServices() {
        transactionService = serviceFactory.getTransactionService();
        tagService = serviceFactory.getTagService();
        transactions = FXCollections.observableArrayList();
        selectedTags = new HashSet<>();
    }

    @Override
    protected void setupUI() {
        // Initialize date picker
        dpDate.setValue(LocalDate.now());

        // Setup table columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTags.setCellValueFactory(cellData -> {
            String tags = cellData.getValue().getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(tags);
        });

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
        List<Transaction> allTransactions = transactionService.findByDateRange(
            LocalDate.now().minusYears(1), LocalDate.now());
        transactions.setAll(allTransactions);
    }

    @FXML
    private void handleAddTransaction() {
        try {
            BigDecimal amount = new BigDecimal(tfAmount.getText());
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
            showError("Error", "Failed to add transaction: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTransaction() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                transactionService.deleteTransaction(selected.getId());
                transactions.remove(selected);
            } catch (Exception e) {
                showError("Error", "Failed to delete transaction: " + e.getMessage());
            }
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
            });
            tagBox.getChildren().addAll(tagLabel, removeBtn);
            flowSelectedTags.getChildren().add(tagBox);
        }
    }
}
