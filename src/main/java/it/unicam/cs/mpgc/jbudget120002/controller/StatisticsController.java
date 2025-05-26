package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.Statistic;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.service.StatisticsService;
import it.unicam.cs.mpgc.jbudget120002.service.TagService;
import it.unicam.cs.mpgc.jbudget120002.service.TransactionService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatisticsController extends BaseController {
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TableView<Statistic.MonthlyBalance> tableMonthly;
    @FXML private TableColumn<Statistic.MonthlyBalance, String> colMonth;
    @FXML private TableColumn<Statistic.MonthlyBalance, String> colIncome;
    @FXML private TableColumn<Statistic.MonthlyBalance, String> colExpenses;
    @FXML private TableColumn<Statistic.MonthlyBalance, String> colBalance;
    
    @FXML private TableView<Statistic.CategoryExpense> tableExpenses;
    @FXML private TableColumn<Statistic.CategoryExpense, String> colCategory;
    @FXML private TableColumn<Statistic.CategoryExpense, String> colAmount;
    @FXML private TableColumn<Statistic.CategoryExpense, String> colPercentage;

    @FXML private LineChart<String, Number> chartMonthlyBalance;
    @FXML private PieChart chartCategoryDistribution;
    @FXML private BarChart<String, Number> chartCategoryComparison;
    @FXML private Label lblNetWorth;
    @FXML private ComboBox<Integer> cbYearStart;
    @FXML private ComboBox<Integer> cbYearEnd;

    private StatisticsService statisticsService;
    private ObservableList<Statistic.MonthlyBalance> monthlyBalances;
    private ObservableList<Statistic.CategoryExpense> categoryExpenses;

    @Override
    protected void initializeServices() {
        statisticsService = serviceFactory.getStatisticsService();
        monthlyBalances = FXCollections.observableArrayList();
    }

    @Override
    protected void setupUI() {
        dpStartDate.setValue(LocalDate.now().minusMonths(6));
        dpEndDate.setValue(LocalDate.now());

        // Setup monthly balance table
        colMonth.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMonth().toString()));
        colIncome.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getIncome().toString()));
        colExpenses.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getExpenses().toString()));
        colBalance.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBalance().toString()));

        tableMonthly.setItems(monthlyBalances);
        
        // Initialize with empty data to prevent selection issues
        monthlyBalances.setAll(Collections.emptyList());

        // Add listeners for date changes
        dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    public void refreshData() {
        if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
            List<Statistic.MonthlyBalance> balances = statisticsService
                .getMonthlyBalances(dpStartDate.getValue(), dpEndDate.getValue());
            monthlyBalances.setAll(balances != null ? balances : Collections.emptyList());
        }
    }

    @FXML
    private void handleUpdateStatistics() {
        refreshData();
    }
}
