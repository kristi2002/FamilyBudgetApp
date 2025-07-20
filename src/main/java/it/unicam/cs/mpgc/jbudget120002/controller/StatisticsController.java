package it.unicam.cs.mpgc.jbudget120002.controller;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.service.*;
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
import java.util.stream.Collectors;

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
    
    // Summary Labels - Current Period
    @FXML private Label lblCurrentIncome;
    @FXML private Label lblCurrentExpenses;
    @FXML private Label lblCurrentBalance;
    @FXML private Label lblCurrentSavings;
    
    // Summary Labels - Comparison Period
    @FXML private Label lblCompIncome;
    @FXML private Label lblCompExpenses;
    @FXML private Label lblCompBalance;
    @FXML private Label lblCompSavings;
    
    // Summary Labels - Difference
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

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    protected void initializeServices() {
        statisticsService = serviceFactory.getStatisticsService(false);
        tagService = serviceFactory.getTagService(false);
        budgetService = serviceFactory.getBudgetService(false);
        transactionService = serviceFactory.getTransactionService(false);
        
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

        // Set default date range and categories based on available data
        List<Transaction> allTransactions = transactionService.findAll();
        if (!allTransactions.isEmpty()) {
            LocalDate minDate = allTransactions.stream().map(Transaction::getDate).min(LocalDate::compareTo).orElse(LocalDate.now());
            LocalDate maxDate = allTransactions.stream().map(Transaction::getDate).max(LocalDate::compareTo).orElse(LocalDate.now());
            dpStartDate.setValue(minDate);
            dpEndDate.setValue(maxDate);
        }
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
        updateCategoryFilter();
        cbMainCategory.setOnAction(e -> refreshData());
        chkIncludeSubcategories.setOnAction(e -> updateCategoryFilter());
    }
    
    private void updateCategoryFilter() {
        boolean includeSubcategories = chkIncludeSubcategories.isSelected();
        List<Tag> availableTags;
        
        if (includeSubcategories) {
            // Include all tags (root and subcategories)
            availableTags = tagService.findAll();
        } else {
            // Only include root tags
            availableTags = tagService.findRootTags();
        }
        
        Tag allOption = new Tag("All Categories");
        allOption.setId(null);
        List<Tag> comboTags = new ArrayList<>();
        comboTags.add(allOption);
        comboTags.addAll(availableTags);
        
        Tag currentSelection = cbMainCategory.getValue();
        cbMainCategory.setItems(FXCollections.observableArrayList(comboTags));
        
        // Try to maintain the current selection if it's still available
        if (currentSelection != null) {
            if ("All Categories".equals(currentSelection.getName())) {
                cbMainCategory.setValue(allOption);
            } else if (availableTags.contains(currentSelection)) {
                cbMainCategory.setValue(currentSelection);
            } else {
                cbMainCategory.setValue(allOption);
            }
        } else {
            cbMainCategory.setValue(allOption);
        }
        
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
        
        // Overview Chart
        overviewChart.setTitle("Category Overview");
        overviewChart.getXAxis().setLabel("Category");
        overviewChart.getYAxis().setLabel("Amount");
        
        // Distribution Chart
        distributionChart.setTitle("Spending Distribution");
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
                int currentQuarter = (now.getMonthValue() - 1) / 3;
                int quarterMonth = currentQuarter * 3 + 1;
                LocalDate quarterStart = now.withMonth(quarterMonth).withDayOfMonth(1);
                LocalDate quarterEnd = quarterStart.plusMonths(2).withDayOfMonth(quarterStart.plusMonths(2).lengthOfMonth());
                dpStartDate.setValue(quarterStart);
                dpEndDate.setValue(quarterEnd);
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

    @Override
    public void refreshData() {
        if (currentUser == null) return;
        updateSummary();
        updateTables();
        updateCharts();
    }

    private void updateSummary() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();
        
        if (start == null || end == null) return;
        
        // Calculate current period statistics
        BigDecimal currentIncome = transactionService.calculateIncomeForPeriodForUser(currentUser, start, end);
        BigDecimal currentExpenses = transactionService.calculateExpensesForPeriodForUser(currentUser, start, end);
        BigDecimal currentBalance = currentIncome.subtract(currentExpenses);
        double currentSavingsRate = currentIncome.doubleValue() == 0 ? 0 :
            (currentBalance.doubleValue() / currentIncome.doubleValue()) * 100;
        
        // Calculate comparison period (previous period of same length)
        long daysBetween = ChronoUnit.DAYS.between(start, end) + 1;
        LocalDate compStart = start.minusDays(daysBetween);
        LocalDate compEnd = start.minusDays(1);
        
        BigDecimal compIncome = transactionService.calculateIncomeForPeriodForUser(currentUser, compStart, compEnd);
        BigDecimal compExpenses = transactionService.calculateExpensesForPeriodForUser(currentUser, compStart, compEnd);
        BigDecimal compBalance = compIncome.subtract(compExpenses);
        double compSavingsRate = compIncome.doubleValue() == 0 ? 0 :
            (compBalance.doubleValue() / compIncome.doubleValue()) * 100;
        
        // Calculate differences
        BigDecimal diffIncome = currentIncome.subtract(compIncome);
        BigDecimal diffExpenses = currentExpenses.subtract(compExpenses);
        BigDecimal diffBalance = currentBalance.subtract(compBalance);
        double diffSavingsRate = currentSavingsRate - compSavingsRate;
        
        // Update current period labels
        lblCurrentIncome.setText("Income: " + String.format("€%.2f", currentIncome));
        lblCurrentExpenses.setText("Expenses: " + String.format("€%.2f", currentExpenses));
        lblCurrentBalance.setText("Balance: " + String.format("€%.2f", currentBalance));
        lblCurrentSavings.setText(String.format("Savings Rate: %.1f%%", currentSavingsRate));
        
        // Update comparison period labels
        lblCompIncome.setText("Income: " + String.format("€%.2f", compIncome));
        lblCompExpenses.setText("Expenses: " + String.format("€%.2f", compExpenses));
        lblCompBalance.setText("Balance: " + String.format("€%.2f", compBalance));
        lblCompSavings.setText(String.format("Savings Rate: %.1f%%", compSavingsRate));
        
        // Update difference labels with color coding
        String diffIncomeText = String.format("Income: %s€%.2f", diffIncome.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "", diffIncome);
        String diffExpensesText = String.format("Expenses: %s€%.2f", diffExpenses.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "", diffExpenses);
        String diffBalanceText = String.format("Balance: %s€%.2f", diffBalance.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "", diffBalance);
        String diffSavingsText = String.format("Savings Rate: %s%.1f%%", diffSavingsRate >= 0 ? "+" : "", diffSavingsRate);
        
        lblDiffIncome.setText(diffIncomeText);
        lblDiffExpenses.setText(diffExpensesText);
        lblDiffBalance.setText(diffBalanceText);
        lblDiffSavings.setText(diffSavingsText);
        
        // Apply color styling for differences
        applyDifferenceStyling(lblDiffIncome, diffIncome);
        applyDifferenceStyling(lblDiffExpenses, diffExpenses);
        applyDifferenceStyling(lblDiffBalance, diffBalance);
        applyDifferenceStyling(lblDiffSavings, BigDecimal.valueOf(diffSavingsRate));
    }
    
    private void applyDifferenceStyling(Label label, BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green for positive
        } else if (value.compareTo(BigDecimal.ZERO) < 0) {
            label.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red for negative
        } else {
            label.setStyle("-fx-text-fill: #7f8c8d;"); // Gray for zero
        }
    }

    private void updateTables() {
        if (currentUser == null) return;
        
        // Update monthly summary
        List<MonthlyStatistic> monthlyData = statisticsService.getMonthlyStatistics(
            dpStartDate.getValue(), 
            dpEndDate.getValue()
        );
        monthlyStats.setAll(monthlyData);
        
        // Update category analysis - handle "All Categories" properly
        Tag selectedCategory = cbMainCategory.getValue();
        List<CategoryStatistic> categoryData;
        
        if (selectedCategory != null && "All Categories".equals(selectedCategory.getName())) {
            // For "All Categories", get statistics for all categories
            categoryData = statisticsService.getCategoryStatistics(
                dpStartDate.getValue(),
                dpEndDate.getValue(),
                null, // null means all categories
                chkIncludeSubcategories.isSelected()
            );
        } else {
            categoryData = statisticsService.getCategoryStatistics(
                dpStartDate.getValue(),
                dpEndDate.getValue(),
                selectedCategory,
                chkIncludeSubcategories.isSelected()
            );
        }
        categoryStats.setAll(categoryData);
        
        // Update budget tracking - only if a specific category is selected (not "All Categories")
        if (selectedCategory != null && !"All Categories".equals(selectedCategory.getName())) {
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
        
        // Update Overview Chart
        updateOverviewChart();
        
        // Update Distribution Chart
        updateDistributionChart();
    }
    
    private void updateOverviewChart() {
        overviewChart.getData().clear();
        
        Tag selectedCategory = cbMainCategory.getValue();
        if (selectedCategory != null && "All Categories".equals(selectedCategory.getName())) {
            selectedCategory = null;
        }
        
        // Get category statistics for overview
        List<CategoryStatistic> categoryData = statisticsService.getCategoryStatistics(
            dpStartDate.getValue(),
            dpEndDate.getValue(),
            selectedCategory,
            chkIncludeSubcategories.isSelected()
        );
        
        if (categoryData.isEmpty()) {
            XYChart.Series<String, Number> dummySeries = new XYChart.Series<>();
            dummySeries.setName("No data available");
            dummySeries.getData().add(new XYChart.Data<>("No Data", 0));
            overviewChart.getData().add(dummySeries);
            return;
        }
        
        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("Current Period");
        
        for (CategoryStatistic stat : categoryData) {
            currentSeries.getData().add(new XYChart.Data<>(
                stat.getCategory().getName(), 
                stat.getCurrentAmount().doubleValue()
            ));
        }
        
        overviewChart.getData().add(currentSeries);
        overviewChart.layout();
    }
    
    private void updateDistributionChart() {
        distributionChart.getData().clear();
        
        Tag selectedCategory = cbMainCategory.getValue();
        if (selectedCategory != null && "All Categories".equals(selectedCategory.getName())) {
            selectedCategory = null;
        }
        
        // Get category percentages for distribution
        Map<Tag, Double> percentages = statisticsService.getCategoryPercentages(
            dpStartDate.getValue(),
            dpEndDate.getValue()
        );
        
        if (percentages.isEmpty()) {
            PieChart.Data dummyData = new PieChart.Data("No Data", 100);
            distributionChart.getData().add(dummyData);
            return;
        }
        
        // Filter by selected category if specified
        final Tag finalSelectedCategory = selectedCategory;
        if (finalSelectedCategory != null) {
            percentages = percentages.entrySet().stream()
                .filter(entry -> entry.getKey().equals(finalSelectedCategory))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        
        // Calculate total for percentage calculation
        double total = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        
        for (Map.Entry<Tag, Double> entry : percentages.entrySet()) {
            if (entry.getValue() > 0) {
                double percentage = total > 0 ? (entry.getValue() / total) * 100 : 0;
                String label = String.format("%s (%.1f%%)", entry.getKey().getName(), percentage);
                PieChart.Data data = new PieChart.Data(label, entry.getValue());
                distributionChart.getData().add(data);
            }
        }
        
        distributionChart.layout();
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
