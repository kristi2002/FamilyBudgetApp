package it.unicam.cs.mpgc.jbudget120002.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import it.unicam.cs.mpgc.jbudget120002.service.ServiceFactory;
import it.unicam.cs.mpgc.jbudget120002.service.TransactionService;
import it.unicam.cs.mpgc.jbudget120002.service.StatisticsService;
import it.unicam.cs.mpgc.jbudget120002.model.CategoryExpense;
import javafx.scene.chart.PieChart.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import it.unicam.cs.mpgc.jbudget120002.model.MonthlyBalance;
import javafx.scene.chart.XYChart;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TableCell;

public class DashboardController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    @FXML private Label lblCurrentBalance;
    @FXML private PieChart pieSpendingByCategory;
    @FXML private TableView<Transaction> tableRecentTransactions;
    @FXML private TableColumn<Transaction, java.time.LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colDesc;
    @FXML private TableColumn<Transaction, java.math.BigDecimal> colAmount;
    @FXML private ComboBox<String> cbPeriod;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Button btnUpdate;

    private TransactionService transactionService;
    private StatisticsService statisticsService;
    private LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
    private LocalDate periodEnd = LocalDate.now();

    @Override
    protected void initializeServices() {
        transactionService = serviceFactory.getTransactionService();
        statisticsService = serviceFactory.getStatisticsService();
    }

    @Override
    protected void setupUI() {
        // Setup period selector
        cbPeriod.getItems().setAll("This Month", "Last Month", "This Year", "Custom");
        cbPeriod.setValue("This Month");
        updatePeriodDates();
        cbPeriod.setOnAction(e -> updatePeriodDates());
        dpStart.setOnAction(e -> {
            if (cbPeriod.getValue().equals("Custom")) updatePeriodDates();
        });
        dpEnd.setOnAction(e -> {
            if (cbPeriod.getValue().equals("Custom")) updatePeriodDates();
        });
        // Enable/disable date pickers based on period
        cbPeriod.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean custom = "Custom".equals(newVal);
            dpStart.setDisable(!custom);
            dpEnd.setDisable(!custom);
        });
        dpStart.setDisable(true);
        dpEnd.setDisable(true);
        // Display current balance
        updateBalanceLabel();
    }

    private void updatePeriodDates() {
        String period = cbPeriod.getValue();
        LocalDate now = LocalDate.now();
        switch (period) {
            case "This Month":
                periodStart = now.withDayOfMonth(1);
                periodEnd = now;
                break;
            case "Last Month":
                LocalDate firstOfThisMonth = now.withDayOfMonth(1);
                LocalDate lastMonth = firstOfThisMonth.minusMonths(1);
                periodStart = lastMonth;
                periodEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                break;
            case "This Year":
                periodStart = now.withDayOfYear(1);
                periodEnd = now;
                break;
            case "Custom":
                periodStart = dpStart.getValue() != null ? dpStart.getValue() : now.withDayOfMonth(1);
                periodEnd = dpEnd.getValue() != null ? dpEnd.getValue() : now;
                break;
        }
        if (!"Custom".equals(period)) {
            dpStart.setValue(periodStart);
            dpEnd.setValue(periodEnd);
        }
        updateBalanceLabel();
        loadData();
    }

    @FXML
    private void handleUpdatePeriod() {
        updatePeriodDates();
        updateBalanceLabel();
        loadData();
    }

    private void updateBalanceLabel() {
        BigDecimal balance = transactionService.calculateBalance(periodStart, periodEnd);
        lblCurrentBalance.setText("Current Balance: " + String.format("€%.2f", balance));
    }

    @Override
    protected void loadData() {
        try {
            // Populate pie chart with top expense categories
            List<CategoryExpense> topCategories = statisticsService.getTopExpenseCategories(periodStart, periodEnd, 6);
            // Calculate total expenses for the period
            BigDecimal totalExpenses = topCategories.stream()
                .map(CategoryExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            pieSpendingByCategory.getData().clear();
            for (CategoryExpense ce : topCategories) {
                String name = ce.getCategory() != null ? ce.getCategory().getName() : "Other";
                double percent = totalExpenses.compareTo(BigDecimal.ZERO) == 0
                    ? 0
                    : ce.getAmount().divide(totalExpenses, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
                String label = String.format("%s (%.1f%%)", name, percent);
                pieSpendingByCategory.getData().add(new PieChart.Data(label, ce.getAmount().doubleValue()));
            }

            // Populate recent transactions table (for selected period)
            // Use a more efficient query to get only the transactions we need
            List<Transaction> recent = transactionService.findTransactionsInPeriod(periodStart, periodEnd, 10);
            ObservableList<Transaction> recentObs = FXCollections.observableArrayList(recent);
            tableRecentTransactions.setItems(recentObs);

            // Set up columns if not already set
            if (colDate.getCellValueFactory() == null) {
                colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
                colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
                colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

                // Format date and amount columns
                colDate.setCellFactory(column -> new TextFieldTableCell<>(new StringConverter<java.time.LocalDate>() {
                    @Override public String toString(java.time.LocalDate d) {
                        return d != null ? it.unicam.cs.mpgc.jbudget120002.util.DateTimeUtils.formatDate(d) : "";
                    }
                    @Override public java.time.LocalDate fromString(String s) { return null; }
                }));
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
                            } else {
                                setText(String.format("€%.2f", amount));
                                setStyle("");
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading dashboard data", e);
            showError("Data Loading Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        cbPeriod.setValue("This Month");
        updatePeriodDates();
    }
} 