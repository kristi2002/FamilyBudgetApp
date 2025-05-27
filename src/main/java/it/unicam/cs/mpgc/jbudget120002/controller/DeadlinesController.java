package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.service.ScheduledTransactionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.paint.Color;

public class DeadlinesController extends BaseController {
    @FXML private DatePicker dpMonth;
    @FXML private TableView<ScheduledTransaction> tableDeadlines;
    @FXML private TableColumn<ScheduledTransaction, LocalDate> colDueDate;
    @FXML private TableColumn<ScheduledTransaction, String> colDesc;
    @FXML private TableColumn<ScheduledTransaction, String> colAmount;
    @FXML private TableColumn<ScheduledTransaction, String> colStatus;
    @FXML private TableColumn<ScheduledTransaction, String> colTags;
    @FXML private TableColumn<ScheduledTransaction, String> colCategory;
    @FXML private ComboBox<String> cbCategoryFilter;
    @FXML private Label lblTotalDue;
    @FXML private Label notificationBanner;

    private ScheduledTransactionService scheduledService;
    private ObservableList<ScheduledTransaction> deadlines;

    @Override
    protected void initializeServices() {
        scheduledService = serviceFactory.getScheduledTransactionService();
        deadlines = FXCollections.observableArrayList();
    }

    @Override
    protected void setupUI() {
        // Initialize month picker
        dpMonth.setValue(LocalDate.now());

        // Setup table columns
        colDueDate.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getStartDate()));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAmount().toString()));
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProcessingState().toString()));
        colTags.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.joining(", "))));
        colCategory.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategory())
        );

        tableDeadlines.setItems(deadlines);

        // Add listener for month changes
        dpMonth.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());

        // Add listener for category filter
        cbCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterByCategory());
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        if (dpMonth.getValue() != null) {
            YearMonth selectedMonth = YearMonth.from(dpMonth.getValue());
            List<ScheduledTransaction> monthlyDeadlines = scheduledService
                .findDeadlinesForMonth(selectedMonth);
            deadlines.setAll(monthlyDeadlines);
            // Populate category filter
            List<String> categories = monthlyDeadlines.stream()
                .map(ScheduledTransaction::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
            cbCategoryFilter.getItems().setAll(categories);
            cbCategoryFilter.setValue(null);
        }
        updateTotalDue();
        updateNotifications();
    }

    private void updateTotalDue() {
        BigDecimal total = deadlines.stream()
            .filter(d -> d.getProcessingState() == ScheduledTransaction.ProcessingState.PENDING)
            .map(ScheduledTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        lblTotalDue.setText(String.format("Total Due: $%,.2f", total));
    }

    private void updateNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3);
        List<ScheduledTransaction> soonOrOverdue = deadlines.stream()
            .filter(d -> d.getStartDate() != null && (d.getStartDate().isBefore(today) || (d.getStartDate().isAfter(today.minusDays(1)) && d.getStartDate().isBefore(soon))))
            .filter(d -> d.getProcessingState() == ScheduledTransaction.ProcessingState.PENDING)
            .collect(Collectors.toList());
        if (!soonOrOverdue.isEmpty()) {
            notificationBanner.setText("⚠️ You have " + soonOrOverdue.size() + " deadlines due soon or overdue!");
            notificationBanner.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
        } else {
            notificationBanner.setText("");
            notificationBanner.setStyle("");
        }
    }

    @FXML
    private void handleMarkPaid() {
        ScheduledTransaction selected = tableDeadlines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                scheduledService.updateProcessingState(
                    selected.getId(), ScheduledTransaction.ProcessingState.COMPLETED);
                refreshData();
            } catch (Exception e) {
                showError("Error", "Failed to mark transaction as paid: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleMarkUnpaid() {
        ScheduledTransaction selected = tableDeadlines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                scheduledService.updateProcessingState(
                    selected.getId(), ScheduledTransaction.ProcessingState.PENDING);
                refreshData();
            } catch (Exception e) {
                showError("Error", "Failed to mark transaction as unpaid: " + e.getMessage());
            }
        }
    }

    private void filterByCategory() {
        String selectedCategory = cbCategoryFilter.getValue();
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            tableDeadlines.setItems(deadlines);
        } else {
            ObservableList<ScheduledTransaction> filtered = deadlines.filtered(d ->
                selectedCategory.equals(d.getCategory())
            );
            tableDeadlines.setItems(filtered);
        }
    }
}
