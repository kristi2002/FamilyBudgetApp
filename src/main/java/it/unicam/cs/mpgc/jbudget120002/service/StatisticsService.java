package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.model.StatisticsModels.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    /**
     * Get monthly statistics for a given period
     */
    List<MonthlyStatistic> getMonthlyStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get category-based statistics for a given period
     */
    List<CategoryStatistic> getCategoryStatistics(
        LocalDate startDate, 
        LocalDate endDate, 
        Tag category,
        boolean includeSubcategories
    );
    
    /**
     * Get budget tracking statistics for a given period
     */
    List<BudgetStatistic> getBudgetStatistics(
        LocalDate startDate,
        LocalDate endDate,
        Tag category
    );
    
    /**
     * Calculate total income for a period
     */
    BigDecimal calculateTotalIncome(LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate total expenses for a period
     */
    BigDecimal calculateTotalExpenses(LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate savings rate for a period
     */
    double calculateSavingsRate(LocalDate startDate, LocalDate endDate);

    /**
     * Get top expense categories for a period
     */
    List<CategoryExpense> getTopExpenseCategories(LocalDate startDate, LocalDate endDate, int limit);
    // User-aware version
    List<CategoryExpense> getTopExpenseCategories(User user, LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Get monthly balances for a period
     */
    List<MonthlyBalance> getMonthlyBalances(LocalDate startDate, LocalDate endDate);

    /**
     * Compare category statistics between two periods
     */
    List<CategoryComparison> comparePeriods(
        LocalDate previousStart, LocalDate previousEnd,
        LocalDate currentStart, LocalDate currentEnd
    );

    /**
     * Get category percentages for a period
     */
    Map<Tag, Double> getCategoryPercentages(LocalDate startDate, LocalDate endDate);

    /**
     * Get net worth as of a specific date
     */
    BigDecimal getNetWorth(LocalDate asOfDate);

    /**
     * Get yearly comparison of expenses by category
     */
    Map<Integer, Map<Tag, BigDecimal>> getYearlyComparison(int startYear, int endYear);

    /**
     * Get trend analysis for a specific category over time
     */
    List<CategoryTrend> getCategoryTrends(
        LocalDate startDate,
        LocalDate endDate,
        Tag category,
        String interval // "DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"
    );

    /**
     * Get spending patterns analysis
     */
    SpendingPattern getSpendingPatterns(
        LocalDate startDate,
        LocalDate endDate,
        Tag category
    );

    /**
     * Get budget utilization over time
     */
    List<BudgetUtilization> getBudgetUtilization(
        LocalDate startDate,
        LocalDate endDate,
        Tag category
    );

    /**
     * Get category-wise spending forecast
     */
    Map<Tag, SpendingForecast> getSpendingForecast(
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Get savings progress analysis
     */
    SavingsProgress getSavingsProgress(
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Get category-wise budget recommendations based on historical data
     */
    Map<Tag, BudgetRecommendation> getBudgetRecommendations(
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Get spending anomalies detection
     */
    List<SpendingAnomaly> detectSpendingAnomalies(
        LocalDate startDate,
        LocalDate endDate,
        Tag category
    );

    /**
     * Get category-wise spending patterns by time of day/week
     */
    Map<Tag, TimeBasedPattern> getTimeBasedPatterns(
        LocalDate startDate,
        LocalDate endDate
    );
} 