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
    
    // Category Filter Controls
    @FXML private ComboBox<Tag> cbMainCategory;
    @FXML private CheckBox chkIncludeSubcategories;
    
    // Summary Labels
    @FXML private Label lblCurrentIncome;
    @FXML private Label lblCurrentExpenses;
    @FXML private Label lblCurrentBalance;
    @FXML private Label lblCurrentSavings;
    
    // Charts
    @FXML private VBox chartContainer;
    @FXML private BarChart<String, Number> overviewChart;
    @FXML private LineChart<String, Number> trendChart;
    @FXML private PieChart distributionChart;
    
    // Tables
    @FXML private VBox tableContainer;
    
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
    
    @FXML private ComboBox<String> cbAnalysisInterval;
    @FXML private BarChart<String, Number> patternChart;

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
        setupAnalysisControls();
        setupCharts();

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
        cbPeriodType.setOnAction(e -> {
            updateDateRange();
            boolean isCustom = "Custom".equals(cbPeriodType.getValue());
            dpStartDate.setDisable(!isCustom);
            dpEndDate.setDisable(!isCustom);
            refreshData();
        });
        dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpEndDate.getValue() != null && "Custom".equals(cbPeriodType.getValue())) {
                refreshData();
            }
        });
        dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpStartDate.getValue() != null && "Custom".equals(cbPeriodType.getValue())) {
                refreshData();
            }
        });
        updateDateRange();
        boolean isCustom = "Custom".equals(cbPeriodType.getValue());
        dpStartDate.setDisable(!isCustom);
        dpEndDate.setDisable(!isCustom);
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
        cbMainCategory.setOnAction(e -> refreshData());
        chkIncludeSubcategories.setOnAction(e -> refreshData());
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
                LocalDate firstOfThisMonth = now.withDayOfMonth(1);
                LocalDate lastMonth = firstOfThisMonth.minusMonths(1);
                dpStartDate.setValue(lastMonth);
                dpEndDate.setValue(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
                break;
            case "This Quarter":
                int currentQuarter = (now.getMonthValue() - 1) / 3;
                LocalDate quarterStart = now.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1);
                dpStartDate.setValue(quarterStart);
                dpEndDate.setValue(now);
                break;
            case "Last Quarter":
                int lastQuarter = ((now.getMonthValue() - 1) / 3) - 1;
                if (lastQuarter < 0) lastQuarter = 3;
                int lastQuarterMonth = lastQuarter * 3 + 1;
                LocalDate lastQuarterStart = now.withMonth(lastQuarterMonth).withDayOfMonth(1);
                LocalDate lastQuarterEnd = lastQuarterStart.plusMonths(2).withDayOfMonth(lastQuarterStart.plusMonths(2).lengthOfMonth());
                dpStartDate.setValue(lastQuarterStart);
                dpEndDate.setValue(lastQuarterEnd);
                break;
            case "This Year":
                dpStartDate.setValue(now.withDayOfYear(1));
                dpEndDate.setValue(now);
                break;
            case "Last Year":
                dpStartDate.setValue(now.minusYears(1).withDayOfYear(1));
                dpEndDate.setValue(now.minusYears(1).withDayOfYear(now.minusYears(1).lengthOfYear()));
                break;
            case "Custom":
                // Do not change dates
                break;
        }
        refreshData();
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
        Tag selectedCategory = cbMainCategory.getValue();
        if (selectedCategory != null && "All Categories".equals(selectedCategory.getName())) {
            selectedCategory = null;
        }
        // Trend Chart
        trendChart.getData().clear();
        String interval = cbAnalysisInterval.getValue();
        if (interval == null) interval = "MONTHLY";
        List<CategoryTrend> trends = statisticsService.getCategoryTrends(
            dpStartDate.getValue(), dpEndDate.getValue(), selectedCategory, interval
        );
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Balance");
        if (trends.isEmpty()) {
            balanceSeries.getData().add(new XYChart.Data<>("No Data", 0));
        } else {
            for (CategoryTrend trend : trends) {
                String dateStr = formatDate(trend.date(), interval);
                balanceSeries.getData().add(new XYChart.Data<>(dateStr, trend.amount().doubleValue()));
            }
        }
        trendChart.getData().add(balanceSeries);
        // Pattern Chart
        patternChart.getData().clear();
        SpendingPattern pattern = statisticsService.getSpendingPatterns(dpStartDate.getValue(), dpEndDate.getValue(), selectedCategory);
        if (pattern == null) {
            XYChart.Series<String, Number> dummySeries = new XYChart.Series<>();
            dummySeries.setName("No data available");
            dummySeries.getData().add(new XYChart.Data<>("No Data", 0));
            patternChart.getData().add(dummySeries);
            return;
        }
        XYChart.Series<String, Number> averageSeries = new XYChart.Series<>();
        averageSeries.setName("Average Amount");
        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Maximum Amount");
        XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Minimum Amount");
        String categoryName = selectedCategory != null ? selectedCategory.getName() : "All Categories";
        averageSeries.getData().add(new XYChart.Data<>(categoryName, pattern.averageAmount().doubleValue()));
        maxSeries.getData().add(new XYChart.Data<>(categoryName, pattern.maxAmount().doubleValue()));
        minSeries.getData().add(new XYChart.Data<>(categoryName, pattern.minAmount().doubleValue()));
        patternChart.getData().addAll(averageSeries, maxSeries, minSeries);
        patternChart.layout();
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
