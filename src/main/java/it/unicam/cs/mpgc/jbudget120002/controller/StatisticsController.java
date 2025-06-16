package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.service.*;
import it.unicam.cs.mpgc.jbudget120002.util.CurrencyUtils;
import it.unicam.cs.mpgc.jbudget120002.model.StatisticsModels.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Controller class managing the statistics and analytics view in the Family Budget App.
 * This class handles the visualization and analysis of financial data through
 * various charts, graphs, and statistical reports.
 *
 * Responsibilities:
 * - Display financial statistics and trends
 * - Generate and update charts and graphs
 * - Handle date range selection for analysis
 * - Present budget utilization insights
 * - Show spending patterns and anomalies
 *
 * Usage:
 * Used by MainController to manage the statistics tab and provide
 * comprehensive financial analysis and reporting to users.
 */
public class StatisticsController extends BaseController {
    // Period Selection Controls
    @FXML private ComboBox<String> cbPeriodType;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbComparisonPeriod;
    
    // Category Filter Controls
    @FXML private ComboBox<Tag> cbMainCategory;
    @FXML private CheckBox chkIncludeSubcategories;
    @FXML private ToggleButton btnShowChart;
    @FXML private ComboBox<String> cbChartType;
    
    // Summary Labels
    @FXML private Label lblCurrentIncome;
    @FXML private Label lblCurrentExpenses;
    @FXML private Label lblCurrentBalance;
    @FXML private Label lblCurrentSavings;
    @FXML private Label lblCompIncome;
    @FXML private Label lblCompExpenses;
    @FXML private Label lblCompBalance;
    @FXML private Label lblCompSavings;
    @FXML private Label lblDiffIncome;
    @FXML private Label lblDiffExpenses;
    @FXML private Label lblDiffBalance;
    @FXML private Label lblDiffSavings;
    
    // Charts
    @FXML private VBox chartContainer;
    @FXML private BarChart<String, Number> overviewChart;
    @FXML private LineChart<String, Number> trendChart;
    @FXML private PieChart distributionChart;
    
    // Tables
    @FXML private VBox tableContainer;
    @FXML private TableView<MonthlyStatistic> tableMonthlySummary;
    @FXML private TableView<CategoryStatistic> tableCategoryAnalysis;
    @FXML private TableView<BudgetStatistic> tableBudgetTracking;

    // Monthly Summary Table Columns
    @FXML private TableColumn<MonthlyStatistic, String> colMonth;
    @FXML private TableColumn<MonthlyStatistic, BigDecimal> colIncome;
    @FXML private TableColumn<MonthlyStatistic, BigDecimal> colExpenses;
    @FXML private TableColumn<MonthlyStatistic, BigDecimal> colBalance;
    @FXML private TableColumn<MonthlyStatistic, Double> colSavings;

    // Category Analysis Table Columns
    @FXML private TableColumn<CategoryStatistic, String> colCategoryName;
    @FXML private TableColumn<CategoryStatistic, BigDecimal> colCurrentAmount;
    @FXML private TableColumn<CategoryStatistic, BigDecimal> colPreviousAmount;
    @FXML private TableColumn<CategoryStatistic, BigDecimal> colDifference;
    @FXML private TableColumn<CategoryStatistic, Double> colPercentage;

    // Budget Tracking Table Columns
    @FXML private TableColumn<BudgetStatistic, String> colBudgetCategory;
    @FXML private TableColumn<BudgetStatistic, BigDecimal> colBudgetAmount;
    @FXML private TableColumn<BudgetStatistic, BigDecimal> colActualAmount;
    @FXML private TableColumn<BudgetStatistic, BigDecimal> colVariance;
    @FXML private TableColumn<BudgetStatistic, Double> colProgress;
    
    // Services
    private StatisticsService statisticsService;
    private TagService tagService;
    private BudgetService budgetService;
    private TransactionService transactionService;
    
    // Data
    private ObservableList<MonthlyStatistic> monthlyStats;
    private ObservableList<CategoryStatistic> categoryStats;
    private ObservableList<BudgetStatistic> budgetStats;

    // New UI Controls
    @FXML private TabPane analysisTabs;
    @FXML private Tab tabTrends;
    @FXML private Tab tabPatterns;
    @FXML private Tab tabForecast;
    @FXML private Tab tabAnomalies;
    
    @FXML private ComboBox<String> cbAnalysisInterval;
    @FXML private ComboBox<String> cbForecastMethod;
    @FXML private Slider sldConfidenceLevel;
    
    @FXML private BarChart<String, Number> patternChart;
    @FXML private ScatterChart<String, Number> forecastChart;
    @FXML private TableView<SpendingAnomaly> tableAnomalies;
    
    // Table Columns for Anomalies
    @FXML private TableColumn<SpendingAnomaly, LocalDateTime> colTimestamp;
    @FXML private TableColumn<SpendingAnomaly, String> colAnomalyCategory;
    @FXML private TableColumn<SpendingAnomaly, BigDecimal> colAmount;
    @FXML private TableColumn<SpendingAnomaly, BigDecimal> colExpected;
    @FXML private TableColumn<SpendingAnomaly, Double> colDeviation;
    @FXML private TableColumn<SpendingAnomaly, String> colType;

    @Override
    protected void initializeServices() {
        statisticsService = serviceFactory.getStatisticsService();
        tagService = serviceFactory.getTagService();
        budgetService = serviceFactory.getBudgetService();
        transactionService = serviceFactory.getTransactionService();
        
        monthlyStats = FXCollections.observableArrayList();
        categoryStats = FXCollections.observableArrayList();
        budgetStats = FXCollections.observableArrayList();
    }

    @Override
    protected void setupUI() {
        setupPeriodControls();
        setupCategoryControls();
        setupChartControls();
        setupTables();
        setupEventHandlers();
        setupAnalysisControls();
        setupCharts();
        setupAnomaliesTable();

        // --- ADDED: Set default date range and categories based on available data ---
        List<Transaction> allTransactions = transactionService.findAll();
        if (!allTransactions.isEmpty()) {
            LocalDate minDate = allTransactions.stream().map(Transaction::getDate).min(LocalDate::compareTo).orElse(LocalDate.now());
            LocalDate maxDate = allTransactions.stream().map(Transaction::getDate).max(LocalDate::compareTo).orElse(LocalDate.now());
            dpStartDate.setValue(minDate);
            dpEndDate.setValue(maxDate);
        }
        // Removed code that overwrites cbMainCategory items
        // --- END ADDED ---
    }

    private void setupPeriodControls() {
        cbPeriodType.setItems(FXCollections.observableArrayList(
            "This Month", "Last Month", "This Quarter", "Last Quarter", "This Year", "Last Year", "Custom"
        ));
        cbPeriodType.setValue("This Month");
        
        cbComparisonPeriod.setItems(FXCollections.observableArrayList(
            "Previous Period", "Same Period Last Year", "None"
        ));
        cbComparisonPeriod.setValue("Previous Period");
        
        updateDateRange();
    }

    private void setupCategoryControls() {
        List<Tag> allTags = tagService.findAll();
        Tag allOption = new Tag("All Categories");
        allOption.setId(null);
        List<Tag> comboTags = new ArrayList<>();
        comboTags.add(allOption);
        comboTags.addAll(allTags);
        cbMainCategory.setItems(FXCollections.observableArrayList(comboTags));
        cbMainCategory.setValue(allOption);
        cbMainCategory.setConverter(new StringConverter<Tag>() {
            @Override
            public String toString(Tag tag) {
                return tag != null ? tag.getName() : "";
            }
            @Override
            public Tag fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
    }

    private void setupChartControls() {
        cbChartType.setItems(FXCollections.observableArrayList(
            "Bar Chart", "Line Chart", "Pie Chart"
        ));
        cbChartType.setValue("Bar Chart");
        
        btnShowChart.selectedProperty().addListener((obs, oldVal, newVal) -> {
            chartContainer.setVisible(newVal);
            chartContainer.setManaged(newVal);
            tableContainer.setVisible(!newVal);
            tableContainer.setManaged(!newVal);
        });
    }

    private void setupTables() {
        // Monthly Summary Table
        tableMonthlySummary.setItems(monthlyStats);
        
        // Set up column cell factories for Monthly Summary
        colMonth.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMonth().toString()));
        colIncome.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getIncome()));
        colExpenses.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getExpenses()));
        colBalance.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getBalance()));
        colSavings.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getSavingsRate()));

        // Format currency columns
        colIncome.setCellFactory(column -> new TableCell<MonthlyStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colExpenses.setCellFactory(column -> new TableCell<MonthlyStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colBalance.setCellFactory(column -> new TableCell<MonthlyStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colSavings.setCellFactory(column -> new TableCell<MonthlyStatistic, Double>() {
            @Override
            protected void updateItem(Double rate, boolean empty) {
                super.updateItem(rate, empty);
                if (empty || rate == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", rate));
                }
            }
        });

        // Category Analysis Table
        tableCategoryAnalysis.setItems(categoryStats);
        
        // Set up column cell factories for Category Analysis
        colCategoryName.setCellValueFactory(cellData -> {
            Tag category = cellData.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getName() : "");
        });
        colCurrentAmount.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getCurrentAmount()));
        colPreviousAmount.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getPreviousAmount()));
        colDifference.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDifference()));
        colPercentage.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getPercentageChange()));

        // Format currency and percentage columns for Category Analysis
        colCurrentAmount.setCellFactory(column -> new TableCell<CategoryStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colPreviousAmount.setCellFactory(column -> new TableCell<CategoryStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colDifference.setCellFactory(column -> new TableCell<CategoryStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colPercentage.setCellFactory(column -> new TableCell<CategoryStatistic, Double>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                } else {
                    setText(String.format("%+.1f%%", percentage));
                }
            }
        });

        // Budget Tracking Table
        tableBudgetTracking.setItems(budgetStats);
        
        // Set up column cell factories for Budget Tracking
        colBudgetCategory.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory().getName()));
        colBudgetAmount.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getBudgetAmount()));
        colActualAmount.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getActualAmount()));
        colVariance.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getVariance()));
        colProgress.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getProgress()));

        // Format currency and percentage columns for Budget Tracking
        colBudgetAmount.setCellFactory(column -> new TableCell<BudgetStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colActualAmount.setCellFactory(column -> new TableCell<BudgetStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colVariance.setCellFactory(column -> new TableCell<BudgetStatistic, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colProgress.setCellFactory(column -> new TableCell<BudgetStatistic, Double>() {
            @Override
            protected void updateItem(Double progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", progress));
                }
            }
        });
    }

    private void setupEventHandlers() {
        cbPeriodType.setOnAction(e -> updateDateRange());
        dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpEndDate.getValue() != null) {
                refreshData();
                updateTrendAnalysis(newVal, dpEndDate.getValue(), cbMainCategory.getValue(), cbAnalysisInterval.getValue());
                updatePatternAnalysis(newVal, dpEndDate.getValue(), cbMainCategory.getValue());
            }
        });
        dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpStartDate.getValue() != null) {
                refreshData();
                updateTrendAnalysis(dpStartDate.getValue(), newVal, cbMainCategory.getValue(), cbAnalysisInterval.getValue());
                updatePatternAnalysis(dpStartDate.getValue(), newVal, cbMainCategory.getValue());
            }
        });
        cbMainCategory.setOnAction(e -> {
            refreshData();
            if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
                updateTrendAnalysis(dpStartDate.getValue(), dpEndDate.getValue(), cbMainCategory.getValue(), cbAnalysisInterval.getValue());
                updatePatternAnalysis(dpStartDate.getValue(), dpEndDate.getValue(), cbMainCategory.getValue());
            }
        });
        chkIncludeSubcategories.setOnAction(e -> refreshData());
        cbChartType.setOnAction(e -> updateCharts());
        cbAnalysisInterval.setOnAction(e -> {
            if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
                updateTrendAnalysis(dpStartDate.getValue(), dpEndDate.getValue(), cbMainCategory.getValue(), cbAnalysisInterval.getValue());
            }
        });
        // Add tab change listener to update charts when switching tabs
        analysisTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabPatterns && dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
                updatePatternAnalysis(dpStartDate.getValue(), dpEndDate.getValue(), cbMainCategory.getValue());
            }
            if (newTab == tabForecast && dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
                updateForecastAnalysis(dpStartDate.getValue(), dpEndDate.getValue(), cbMainCategory.getValue());
            }
        });
    }

    @Override
    protected void loadData() {
        refreshData();
    }

    @FXML
    private void handleUpdateStatistics() {
        refreshData();
    }

    private void updateDateRange() {
        LocalDate now = LocalDate.now();
        switch (cbPeriodType.getValue()) {
            case "This Month":
                dpStartDate.setValue(now.withDayOfMonth(1));
                dpEndDate.setValue(now);
                break;
            case "Last Month":
                dpStartDate.setValue(now.minusMonths(1).withDayOfMonth(1));
                dpEndDate.setValue(now.withDayOfMonth(1).minusDays(1));
                break;
            case "This Quarter":
                dpStartDate.setValue(now.withDayOfMonth(1).minusMonths((now.getMonthValue() - 1) % 3));
                dpEndDate.setValue(now);
                break;
            case "Last Quarter":
                LocalDate quarterStart = now.withDayOfMonth(1).minusMonths((now.getMonthValue() - 1) % 3);
                dpStartDate.setValue(quarterStart.minusMonths(3));
                dpEndDate.setValue(quarterStart.minusDays(1));
                break;
            case "This Year":
                dpStartDate.setValue(now.withDayOfYear(1));
                dpEndDate.setValue(now);
                break;
            case "Last Year":
                dpStartDate.setValue(now.minusYears(1).withDayOfYear(1));
                dpEndDate.setValue(now.withDayOfYear(1).minusDays(1));
                break;
            // "Custom" - leave dates as they are
        }
    }

    public void refreshData() {
        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) return;
        
        updateSummary();
        updateTables();
        updateCharts();
    }

    private void updateSummary() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();
        
        // Current period statistics
        BigDecimal currentIncome = transactionService.calculateIncomeForPeriod(start, end);
        BigDecimal currentExpenses = transactionService.calculateExpensesForPeriod(start, end);
        BigDecimal currentBalance = currentIncome.subtract(currentExpenses);
        double currentSavingsRate = currentIncome.doubleValue() == 0 ? 0 :
            (currentBalance.doubleValue() / currentIncome.doubleValue()) * 100;
        
        // Update current period labels
        lblCurrentIncome.setText("Income: " + CurrencyUtils.formatAmount(currentIncome));
        lblCurrentExpenses.setText("Expenses: " + CurrencyUtils.formatAmount(currentExpenses));
        lblCurrentBalance.setText("Balance: " + CurrencyUtils.formatAmount(currentBalance));
        lblCurrentSavings.setText(String.format("Savings Rate: %.1f%%", currentSavingsRate));
        
        // Comparison period if selected
        if (!"None".equals(cbComparisonPeriod.getValue())) {
            long periodLength = ChronoUnit.DAYS.between(start, end);
            LocalDate compEnd = start.minusDays(1);
            LocalDate compStart = compEnd.minusDays(periodLength);
            
            if ("Same Period Last Year".equals(cbComparisonPeriod.getValue())) {
                compStart = start.minusYears(1);
                compEnd = end.minusYears(1);
            }
            
            BigDecimal compIncome = transactionService.calculateIncomeForPeriod(compStart, compEnd);
            BigDecimal compExpenses = transactionService.calculateExpensesForPeriod(compStart, compEnd);
            BigDecimal compBalance = compIncome.subtract(compExpenses);
            double compSavingsRate = compIncome.doubleValue() == 0 ? 0 :
                (compBalance.doubleValue() / compIncome.doubleValue()) * 100;
            
            // Update comparison labels
            lblCompIncome.setText("Income: " + CurrencyUtils.formatAmount(compIncome));
            lblCompExpenses.setText("Expenses: " + CurrencyUtils.formatAmount(compExpenses));
            lblCompBalance.setText("Balance: " + CurrencyUtils.formatAmount(compBalance));
            lblCompSavings.setText(String.format("Savings Rate: %.1f%%", compSavingsRate));
            
            // Calculate and update differences
            BigDecimal diffIncome = currentIncome.subtract(compIncome);
            BigDecimal diffExpenses = currentExpenses.subtract(compExpenses);
            BigDecimal diffBalance = currentBalance.subtract(compBalance);
            double diffSavings = currentSavingsRate - compSavingsRate;
            
            lblDiffIncome.setText("Income: " + CurrencyUtils.formatAmount(diffIncome));
            lblDiffExpenses.setText("Expenses: " + CurrencyUtils.formatAmount(diffExpenses));
            lblDiffBalance.setText("Balance: " + CurrencyUtils.formatAmount(diffBalance));
            lblDiffSavings.setText(String.format("Savings Rate: %+.1f%%", diffSavings));
        }
    }

    private void updateTables() {
        // Update monthly summary
        List<MonthlyStatistic> monthlyData = statisticsService.getMonthlyStatistics(
            dpStartDate.getValue(), 
            dpEndDate.getValue()
        );
        monthlyStats.setAll(monthlyData);
        
        // Update category analysis
        Tag selectedCategory = cbMainCategory.getValue();
        List<CategoryStatistic> categoryData = statisticsService.getCategoryStatistics(
            dpStartDate.getValue(),
            dpEndDate.getValue(),
            selectedCategory,
            chkIncludeSubcategories.isSelected()
        );
        categoryStats.setAll(categoryData);
        
        // Update budget tracking - only if a category is selected
        if (selectedCategory != null) {
            List<BudgetStatistic> budgetData = statisticsService.getBudgetStatistics(
                dpStartDate.getValue(),
                dpEndDate.getValue(),
                selectedCategory
            );
            budgetStats.setAll(budgetData);
        } else {
            budgetStats.clear();
        }
    }

    private void updateCharts() {
        if (!btnShowChart.isSelected()) return;
        
        switch (cbChartType.getValue()) {
            case "Bar Chart":
                updateBarChart();
                break;
            case "Line Chart":
                updateLineChart();
                break;
            case "Pie Chart":
                updatePieChart();
                break;
        }
    }

    private void updateBarChart() {
        overviewChart.getData().clear();
        
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenses");
        
        for (MonthlyStatistic stat : monthlyStats) {
            incomeSeries.getData().add(new XYChart.Data<>(
                stat.getMonth().toString(),
                stat.getIncome().doubleValue()
            ));
            expensesSeries.getData().add(new XYChart.Data<>(
                stat.getMonth().toString(),
                stat.getExpenses().doubleValue()
            ));
        }
        
        overviewChart.getData().addAll(incomeSeries, expensesSeries);
    }

    private void updateLineChart() {
        trendChart.getData().clear();
        
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Balance");
        
        for (MonthlyStatistic stat : monthlyStats) {
            balanceSeries.getData().add(new XYChart.Data<>(
                stat.getMonth().toString(),
                stat.getBalance().doubleValue()
            ));
        }
        
        trendChart.getData().add(balanceSeries);
    }

    private void updatePieChart() {
        distributionChart.getData().clear();
        
        for (CategoryStatistic stat : categoryStats) {
            distributionChart.getData().add(new PieChart.Data(
                stat.getCategory().getName(),
                stat.getCurrentAmount().abs().doubleValue()
            ));
        }
    }

    @FXML
    private void handleExportPDF() {
        // TODO: Implement PDF export
        showInfo("Export", "PDF export will be implemented in a future update.");
    }

    @FXML
    private void handleExportExcel() {
        // TODO: Implement Excel export
        showInfo("Export", "Excel export will be implemented in a future update.");
    }

    private void setupAnalysisControls() {
        cbAnalysisInterval.getItems().addAll(
            "DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"
        );
        cbAnalysisInterval.setValue("MONTHLY");

        // Add listener for interval changes
        cbAnalysisInterval.setOnAction(e -> {
            if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
                updateTrendAnalysis(
                    dpStartDate.getValue(),
                    dpEndDate.getValue(),
                    cbMainCategory.getValue(),
                    cbAnalysisInterval.getValue()
                );
            }
        });

        cbForecastMethod.getItems().addAll(
            "Moving Average", "Linear Regression", "Exponential Smoothing"
        );
        cbForecastMethod.setValue("Moving Average");

        sldConfidenceLevel.setMin(0.5);
        sldConfidenceLevel.setMax(0.99);
        sldConfidenceLevel.setValue(0.95);
        sldConfidenceLevel.setShowTickLabels(true);
        sldConfidenceLevel.setShowTickMarks(true);
    }

    private void setupCharts() {
        // Trend Chart
        trendChart.setTitle("Category Spending Trends");
        trendChart.getXAxis().setLabel("Time");
        trendChart.getYAxis().setLabel("Amount");

        // Pattern Chart
        patternChart.setTitle("Spending Patterns");
        patternChart.getXAxis().setLabel("Category");
        patternChart.getYAxis().setLabel("Amount");

        // Forecast Chart
        forecastChart.setTitle("Spending Forecast");
        forecastChart.getXAxis().setLabel("Time");
        forecastChart.getYAxis().setLabel("Amount");
    }

    private void setupAnomaliesTable() {
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colAnomalyCategory.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().category().getName()));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colExpected.setCellValueFactory(new PropertyValueFactory<>("expectedAmount"));
        colDeviation.setCellValueFactory(new PropertyValueFactory<>("deviationPercentage"));
        colType.setCellValueFactory(new PropertyValueFactory<>("anomalyType"));

        // Format currency columns
        colAmount.setCellFactory(column -> new TableCell<SpendingAnomaly, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });
        colExpected.setCellFactory(column -> new TableCell<SpendingAnomaly, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyUtils.formatAmount(amount));
                }
            }
        });

        // Format percentage column
        colDeviation.setCellFactory(column -> new TableCell<SpendingAnomaly, Double>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                } else {
                    setText(String.format("%+.1f%%", percentage));
                }
            }
        });
    }

    @FXML
    private void handleAnalysisUpdate() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();
        Tag category = cbMainCategory.getValue();
        
        if (start == null || end == null) {
            showError("Error", "Please select a valid date range");
            return;
        }

        updateTrendAnalysis(start, end, category, cbAnalysisInterval.getValue());
        updatePatternAnalysis(start, end, category);
        updateForecastAnalysis(start, end, category);
        updateAnomalyDetection(start, end, category);
        
        // Force chart updates
        trendChart.layout();
        patternChart.layout();
        forecastChart.layout();
    }

    private void updateTrendAnalysis(LocalDate start, LocalDate end, Tag category, String interval) {
        // If 'All Categories' is selected, pass null as the category
        if (category != null && "All Categories".equals(category.getName())) {
            category = null;
        }
        
        List<CategoryTrend> trends = statisticsService.getCategoryTrends(start, end, category, interval);
        
        // Clear existing data
        trendChart.getData().clear();
        
        if (trends.isEmpty()) {
            // Add a dummy data point to indicate no data
            XYChart.Series<String, Number> dummySeries = new XYChart.Series<>();
            dummySeries.setName("No data available");
            dummySeries.getData().add(new XYChart.Data<>("No Data", 0));
            trendChart.getData().add(dummySeries);
            return;
        }
        
        // Create series for actual spending
        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Actual Spending");
        
        // Create series for average
        XYChart.Series<String, Number> averageSeries = new XYChart.Series<>();
        averageSeries.setName("Average");
        
        // Create series for trend
        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("Trend");
        
        // Add data points
        for (CategoryTrend trend : trends) {
            String dateStr = formatDate(trend.date(), interval);
            actualSeries.getData().add(new XYChart.Data<>(dateStr, trend.amount().doubleValue()));
            averageSeries.getData().add(new XYChart.Data<>(dateStr, trend.average().doubleValue()));
            trendSeries.getData().add(new XYChart.Data<>(dateStr, trend.trend().doubleValue()));
        }
        
        // Add all series to the chart
        trendChart.getData().addAll(actualSeries, averageSeries, trendSeries);
        
        // Force chart update
        trendChart.layout();
    }

    private void updatePatternAnalysis(LocalDate start, LocalDate end, Tag category) {
        // Clear existing data
        patternChart.getData().clear();
        
        // Get patterns for the selected category
        SpendingPattern pattern = statisticsService.getSpendingPatterns(start, end, category);
        
        if (pattern == null) {
            // Add a dummy data point to indicate no data
            XYChart.Series<String, Number> dummySeries = new XYChart.Series<>();
            dummySeries.setName("No data available");
            dummySeries.getData().add(new XYChart.Data<>("No Data", 0));
            patternChart.getData().add(dummySeries);
            return;
        }
        
        // Create series for average amount
        XYChart.Series<String, Number> averageSeries = new XYChart.Series<>();
        averageSeries.setName("Average Amount");
        
        // Create series for max amount
        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Maximum Amount");
        
        // Create series for min amount
        XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Minimum Amount");
        
        // Add data points
        String categoryName = category != null ? category.getName() : "All Categories";
        averageSeries.getData().add(new XYChart.Data<>(categoryName, pattern.averageAmount().doubleValue()));
        maxSeries.getData().add(new XYChart.Data<>(categoryName, pattern.maxAmount().doubleValue()));
        minSeries.getData().add(new XYChart.Data<>(categoryName, pattern.minAmount().doubleValue()));
        
        // Add all series to the chart
        patternChart.getData().addAll(averageSeries, maxSeries, minSeries);
        
        // Force chart update
        patternChart.layout();
    }

    private void updateForecastAnalysis(LocalDate start, LocalDate end, Tag category) {
        System.out.println("[DEBUG] updateForecastAnalysis called: start=" + start + ", end=" + end + ", category=" + (category != null ? category.getName() + " (ID=" + category.getId() + ")" : "null"));
        Map<Tag, SpendingForecast> forecasts = statisticsService.getSpendingForecast(start, end);
        
        // Debug output
        System.out.println("[DEBUG] Forecast map keys:");
        for (Tag t : forecasts.keySet()) {
            System.out.println("  - " + t.getName() + " (ID=" + t.getId() + ")");
        }
        
        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Actual");
        
        XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
        forecastSeries.setName("Forecast");

        // Fix: Use the correct key for 'All Categories'
        Tag lookupTag = category;
        if (category == null || "All Categories".equals(category.getName())) {
            lookupTag = new Tag("All Categories");
            lookupTag.setId(null);
        }

        SpendingForecast forecast = forecasts.get(lookupTag);
        if (forecast != null) {
            // Add historical data
            for (int i = 0; i < forecast.historicalData().size(); i++) {
                String dateStr = formatDate(start.plusDays(i), "DAILY");
                actualSeries.getData().add(new XYChart.Data<>(dateStr, forecast.historicalData().get(i)));
            }
            
            // Add forecast point
            forecastSeries.getData().add(new XYChart.Data<>(
                formatDate(end.plusDays(1), "DAILY"),
                forecast.projectedAmount()
            ));
        }

        forecastChart.getData().clear();
        forecastChart.getData().addAll(actualSeries, forecastSeries);
    }

    private void updateAnomalyDetection(LocalDate start, LocalDate end, Tag category) {
        List<SpendingAnomaly> anomalies = statisticsService.detectSpendingAnomalies(start, end, category);
        tableAnomalies.getItems().setAll(anomalies);
    }

    private String formatDate(LocalDate date, String interval) {
        switch (interval) {
            case "DAILY":
                return date.toString();
            case "WEEKLY":
                return "Week " + date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
            case "MONTHLY":
                return date.getMonth().toString() + " " + date.getYear();
            case "QUARTERLY":
                return "Q" + ((date.getMonthValue() - 1) / 3 + 1) + " " + date.getYear();
            case "YEARLY":
                return String.valueOf(date.getYear());
            default:
                return date.toString();
        }
    }
}
