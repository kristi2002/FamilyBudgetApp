package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.service.BudgetService;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import javafx.beans.property.SimpleObjectProperty;
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

/**
 * Controller class managing the budgets view in the Family Budget App.
 * This class handles the user interface for creating, monitoring, and
 * managing budget plans and their utilization.
 *
 * Responsibilities:
 * - Display and manage budget list
 * - Handle budget creation and editing
 * - Monitor budget utilization and alerts
 * - Implement budget templates
 * - Coordinate with BudgetService for data operations
 *
 * Usage:
 * Used by MainController to manage the budgets tab and provide
 * budget management functionality to users.
 */
public class BudgetsController extends BaseController {
    @FXML private TextField tfName;
    @FXML private TextField tfAmount;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<Tag> cbTags;
    @FXML private FlowPane flowSelectedTags;
    @FXML private TableView<BudgetTableItem> table;
    @FXML private TableColumn<BudgetTableItem, String> colName;
    @FXML private TableColumn<BudgetTableItem, BigDecimal> colAmount;
    @FXML private TableColumn<BudgetTableItem, LocalDate> colStartDate;
    @FXML private TableColumn<BudgetTableItem, LocalDate> colEndDate;
    @FXML private TableColumn<BudgetTableItem, BigDecimal> colSpent;
    @FXML private TableColumn<BudgetTableItem, BigDecimal> colRemaining;
    @FXML private TableColumn<BudgetTableItem, String> colTags;

    private BudgetService budgetService;
    private TagService tagService;
    private ObservableList<BudgetTableItem> budgets;
    private Set<Tag> selectedTags;
    private User currentUser;

    @Override
    protected void initializeServices() {
        budgetService = serviceFactory.getBudgetService(false);
        tagService = serviceFactory.getTagService(false);
        budgets = FXCollections.observableArrayList();
        selectedTags = new HashSet<>();
    }

    @Override
    protected void setupUI() {
        // Initialize date pickers
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));

        // Setup table columns
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("budgetedAmount"));
        colSpent.setCellValueFactory(new PropertyValueFactory<>("actualAmount"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
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

        table.setItems(budgets);

        colStartDate.setCellFactory(column -> new TableCell<BudgetTableItem, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils.formatDate(date));
            }
        });
        colEndDate.setCellFactory(column -> new TableCell<BudgetTableItem, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils.formatDate(date));
            }
        });
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void refreshData() {
        if (currentUser == null) {
            return;
        }
        budgets.setAll(budgetService.findAllByUser(currentUser).stream()
            .map(budget -> {
                BigDecimal spent = budgetService.calculateSpentAmount(budget.getId());
                BigDecimal remaining = budget.getAmount().subtract(spent);
                BudgetTableItem item = new BudgetTableItem(
                    budget.getId(),
                    budget.getName(),
                    budget.getAmount(),
                    spent,
                    remaining,
                    budget.getStartDate(),
                    budget.getEndDate()
                );
                budget.getTags().forEach(item::addTag);
                return item;
            })
            .collect(Collectors.toList()));
    }
    
    public void refreshTags() {
        if (tagService != null) {
            // Refresh the tags combobox
            cbTags.setItems(FXCollections.observableArrayList(tagService.findRootTags()));
        }
    }

    @FXML
    private void handleAddBudget() {
        try {
            if (selectedTags.isEmpty()) {
                showError("Error", "Please select at least one category");
                return;
            }

            Budget newBudget = new Budget(
                tfName.getText(),
                new BigDecimal(tfAmount.getText()),
                dpStartDate.getValue(),
                dpEndDate.getValue()
            );
            newBudget.setTags(new HashSet<>(selectedTags));
            
            // Set group if user has one, otherwise leave it null (no group requirement)
            if (!currentUser.getGroups().isEmpty()) {
                newBudget.setGroup(currentUser.getGroups().iterator().next());
            }
            
            budgetService.save(newBudget);
            refreshData();
            clearForm();
        } catch (Exception e) {
            showError("Error adding budget", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteBudget() {
        BudgetTableItem selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                budgetService.delete(selected.getId());
                refreshData();
                clearForm();
            } catch (Exception e) {
                showError("Error", "Failed to delete budget: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdateBudget() {
        BudgetTableItem selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (selectedTags.isEmpty()) {
                    showError("Error", "Please select at least one category");
                    return;
                }
                
                Budget budgetToUpdate = budgetService.findById(selected.getId()).orElse(null);
                if (budgetToUpdate == null) {
                    showError("Error", "Budget not found.");
                    return;
                }

                budgetToUpdate.setName(tfName.getText());
                budgetToUpdate.setAmount(new BigDecimal(tfAmount.getText()));
                budgetToUpdate.setStartDate(dpStartDate.getValue());
                budgetToUpdate.setEndDate(dpEndDate.getValue());
                budgetToUpdate.setTags(new HashSet<>(selectedTags));

                budgetService.save(budgetToUpdate);
                refreshData();
                clearForm();
            } catch (Exception e) {
                showError("Error updating budget", e.getMessage());
            }
        }
    }

    @FXML
    private void handleTableSelection() {
        BudgetTableItem selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfName.setText(selected.getName());
            tfAmount.setText(selected.getBudgetedAmount().toString());
            dpStartDate.setValue(selected.getStartDate());
            dpEndDate.setValue(selected.getEndDate());
            selectedTags.clear();
            selectedTags.addAll(selected.getTags());
            updateSelectedTagsList();
        }
    }

    private void clearForm() {
        tfName.clear();
        tfAmount.clear();
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));
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

    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
