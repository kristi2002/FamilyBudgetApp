package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import it.unicam.cs.mpgc.jbudget120002.service.DeadlineService;
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
import javafx.scene.layout.GridPane;

public class DeadlinesController extends BaseController {
    @FXML private DatePicker dpMonth;
    @FXML private TableView<Deadline> tableDeadlines;
    @FXML private TableColumn<Deadline, LocalDate> colDueDate;
    @FXML private TableColumn<Deadline, String> colDesc;
    @FXML private TableColumn<Deadline, String> colAmount;
    @FXML private TableColumn<Deadline, String> colStatus;
    @FXML private TableColumn<Deadline, String> colCategory;
    @FXML private ComboBox<String> cbCategoryFilter;
    @FXML private Label lblTotalDue;
    @FXML private Label notificationBanner;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private DeadlineService deadlineService;
    private ObservableList<Deadline> deadlines;

    @Override
    protected void initializeServices() {
        deadlineService = serviceFactory.getDeadlineService();
        deadlines = FXCollections.observableArrayList();
    }

    @Override
    protected void setupUI() {
        dpMonth.setValue(LocalDate.now());
        colDueDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDueDate()));
        colDesc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        colAmount.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getAmount())));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isPaid() ? "Paid" : "Unpaid"));
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        tableDeadlines.setItems(deadlines);
        dpMonth.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        cbCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterByCategory());
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        DeadlinesCalendarController.setGlobalDateSelectCallback(this::filterByDate);
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        if (dpMonth.getValue() != null) {
            LocalDate start = dpMonth.getValue().withDayOfMonth(1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            List<Deadline> monthlyDeadlines = deadlineService.findAll().stream()
                .filter(d -> !d.getDueDate().isBefore(start) && !d.getDueDate().isAfter(end))
                .toList();
            deadlines.setAll(monthlyDeadlines);
            List<String> categories = monthlyDeadlines.stream()
                .map(Deadline::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .toList();
            cbCategoryFilter.getItems().setAll(categories);
            cbCategoryFilter.setValue(null);
        }
        updateTotalDue();
        updateNotifications();
    }

    private void updateTotalDue() {
        double total = deadlines.stream()
            .filter(d -> !d.isPaid())
            .mapToDouble(Deadline::getAmount)
            .sum();
        lblTotalDue.setText(String.format("Total Due: $%,.2f", total));
    }

    private void updateNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3);
        List<Deadline> soonOrOverdue = deadlines.stream()
            .filter(d -> d.getDueDate() != null && (d.getDueDate().isBefore(today) || (d.getDueDate().isAfter(today.minusDays(1)) && d.getDueDate().isBefore(soon))))
            .filter(d -> !d.isPaid())
            .toList();
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
        Deadline selected = tableDeadlines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setPaid(true);
                deadlineService.update(selected);
                refreshData();
            } catch (Exception e) {
                showError("Error", "Failed to mark deadline as paid: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleMarkUnpaid() {
        Deadline selected = tableDeadlines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setPaid(false);
                deadlineService.update(selected);
                refreshData();
            } catch (Exception e) {
                showError("Error", "Failed to mark deadline as unpaid: " + e.getMessage());
            }
        }
    }

    private void filterByCategory() {
        String selectedCategory = cbCategoryFilter.getValue();
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            tableDeadlines.setItems(deadlines);
        } else {
            ObservableList<Deadline> filtered = deadlines.filtered(d ->
                selectedCategory.equals(d.getCategory())
            );
            tableDeadlines.setItems(filtered);
        }
    }

    private void handleAdd() {
        DeadlineDialog dialog = new DeadlineDialog(null);
        dialog.setTitle("Add Deadline");
        dialog.showAndWait().ifPresent(deadline -> {
            deadlineService.create(deadline);
            refreshData();
            refreshCalendarTab();
        });
    }

    private void handleEdit() {
        Deadline selected = tableDeadlines.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a deadline to edit.");
            return;
        }
        DeadlineDialog dialog = new DeadlineDialog(selected);
        dialog.setTitle("Edit Deadline");
        dialog.showAndWait().ifPresent(updated -> {
            updated.setId(selected.getId());
            deadlineService.update(updated);
            refreshData();
            refreshCalendarTab();
        });
    }

    private void handleDelete() {
        Deadline selected = tableDeadlines.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a deadline to delete.");
            return;
        }
        if (confirm("Delete Deadline", "Are you sure you want to delete this deadline?")) {
            deadlineService.delete(selected.getId());
            refreshData();
            refreshCalendarTab();
        }
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait().filter(btn -> btn == ButtonType.YES).isPresent();
    }

    private void filterByDate(LocalDate date) {
        if (date == null) {
            tableDeadlines.setItems(deadlines);
            return;
        }
        ObservableList<Deadline> filtered = deadlines.filtered(d -> date.equals(d.getDueDate()));
        tableDeadlines.setItems(filtered);
    }

    private void refreshCalendarTab() {
        DeadlinesCalendarController.globalRefresh();
    }

    // Simple dialog for adding/editing deadlines
    private static class DeadlineDialog extends Dialog<Deadline> {
        private final TextField tfDescription = new TextField();
        private final DatePicker dpDueDate = new DatePicker();
        private final TextField tfAmount = new TextField();
        private final CheckBox cbPaid = new CheckBox("Paid");
        private final TextField tfCategory = new TextField();

        public DeadlineDialog(Deadline deadline) {
            setTitle(deadline == null ? "Add Deadline" : "Edit Deadline");
            setHeaderText(null);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Description:"), 0, 0);
            grid.add(tfDescription, 1, 0);
            grid.add(new Label("Due Date:"), 0, 1);
            grid.add(dpDueDate, 1, 1);
            grid.add(new Label("Amount:"), 0, 2);
            grid.add(tfAmount, 1, 2);
            grid.add(cbPaid, 1, 3);
            grid.add(new Label("Category:"), 0, 4);
            grid.add(tfCategory, 1, 4);
            getDialogPane().setContent(grid);
            if (deadline != null) {
                tfDescription.setText(deadline.getDescription());
                dpDueDate.setValue(deadline.getDueDate());
                tfAmount.setText(String.valueOf(deadline.getAmount()));
                cbPaid.setSelected(deadline.isPaid());
                tfCategory.setText(deadline.getCategory());
            }
            setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    Deadline d = new Deadline();
                    d.setDescription(tfDescription.getText());
                    d.setDueDate(dpDueDate.getValue());
                    try {
                        d.setAmount(Double.parseDouble(tfAmount.getText()));
                    } catch (Exception e) {
                        d.setAmount(0);
                    }
                    d.setPaid(cbPaid.isSelected());
                    d.setCategory(tfCategory.getText());
                    return d;
                }
                return null;
            });
        }
    }
}
