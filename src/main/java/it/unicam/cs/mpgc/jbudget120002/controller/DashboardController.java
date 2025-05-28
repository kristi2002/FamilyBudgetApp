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
import it.unicam.cs.mpgc.jbudget120002.util.CurrencyUtils;
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

public class DashboardController extends BaseController {
    @FXML private Label lblCurrentBalance;
    @FXML private PieChart pieSpendingByCategory;
    @FXML private LineChart<String, Number> lineBalanceOverTime;
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
        dpStart.setOnAction(e -> cbPeriod.setValue("Custom"));
        dpEnd.setOnAction(e -> cbPeriod.setValue("Custom"));
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
        dpStart.setValue(periodStart);
        dpEnd.setValue(periodEnd);
        updateBalanceLabel();
        loadData();
    }

    @FXML
    private void handleUpdatePeriod() {
        periodStart = dpStart.getValue();
        periodEnd = dpEnd.getValue();
        cbPeriod.setValue("Custom");
        updateBalanceLabel();
        loadData();
    }

    private void updateBalanceLabel() {
        BigDecimal balance = transactionService.calculateBalance(periodStart, periodEnd);
        lblCurrentBalance.setText("Current Balance: " + CurrencyUtils.formatAmount(balance, "EUR"));
    }

    @Override
    protected void loadData() {
        // Populate pie chart with top expense categories
        List<CategoryExpense> topCategories = statisticsService.getTopExpenseCategories(periodStart, periodEnd, 6);
        pieSpendingByCategory.getData().clear();
        for (CategoryExpense ce : topCategories) {
            String name = ce.getCategory() != null ? ce.getCategory().getName() : "Other";
            pieSpendingByCategory.getData().add(new Data(name, ce.getAmount().doubleValue()));
        }

        // Populate line chart with monthly balances (use full history for trend)
        List<MonthlyBalance> monthlyBalances = statisticsService.getMonthlyBalances(LocalDate.MIN, LocalDate.now());
        lineBalanceOverTime.getData().clear();
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Balance");
        for (MonthlyBalance mb : monthlyBalances) {
            String monthLabel = mb.getMonth().toString();
            balanceSeries.getData().add(new XYChart.Data<>(monthLabel, mb.getBalance()));
        }
        lineBalanceOverTime.getData().add(balanceSeries);

        // Populate recent transactions table (for selected period)
        List<Transaction> allTransactions = transactionService.findAll();
        allTransactions.removeIf(t -> t.getDate().isBefore(periodStart) || t.getDate().isAfter(periodEnd));
        allTransactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        List<Transaction> recent = allTransactions.stream().limit(10).toList();
        ObservableList<Transaction> recentObs = FXCollections.observableArrayList(recent);
        tableRecentTransactions.setItems(recentObs);

        // Set up columns if not already set
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Format date and amount columns
        colDate.setCellFactory(column -> new TextFieldTableCell<>(new StringConverter<java.time.LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            @Override public String toString(java.time.LocalDate d) {
                return d != null ? d.format(fmt) : "";
            }
            @Override public java.time.LocalDate fromString(String s) { return null; }
        }));
        colAmount.setCellFactory(column -> new TextFieldTableCell<>(new StringConverter<java.math.BigDecimal>() {
            @Override public String toString(java.math.BigDecimal bd) {
                return bd != null ? CurrencyUtils.formatAmount(bd, "EUR") : "";
            }
            @Override public java.math.BigDecimal fromString(String s) { return null; }
        }));
    }

    @FXML
    private void handleRefresh() {
        cbPeriod.setValue("This Month");
        updatePeriodDates();
    }
} 