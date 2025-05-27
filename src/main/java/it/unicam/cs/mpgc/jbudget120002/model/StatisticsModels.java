package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class StatisticsModels {
    public record CategoryTrend(
        LocalDate date,
        Tag category,
        BigDecimal amount,
        BigDecimal average,
        BigDecimal trend
    ) {}

    public record SpendingPattern(
        Tag category,
        BigDecimal averageAmount,
        BigDecimal maxAmount,
        BigDecimal minAmount,
        BigDecimal standardDeviation,
        List<LocalDate> peakDates,
        List<LocalDate> valleyDates
    ) {}

    public record BudgetUtilization(
        LocalDate date,
        Tag category,
        BigDecimal budgeted,
        BigDecimal actual,
        double utilizationPercentage,
        boolean isOverBudget
    ) {}

    public record SpendingForecast(
        Tag category,
        BigDecimal currentAverage,
        BigDecimal projectedAmount,
        double confidenceLevel,
        List<BigDecimal> historicalData
    ) {}

    public record SavingsProgress(
        LocalDate date,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal monthlyContribution,
        double progressPercentage,
        LocalDate projectedCompletionDate
    ) {}

    public record BudgetRecommendation(
        Tag category,
        BigDecimal currentBudget,
        BigDecimal recommendedBudget,
        String reasoning,
        double confidenceScore
    ) {}

    public record SpendingAnomaly(
        LocalDateTime timestamp,
        Tag category,
        BigDecimal amount,
        BigDecimal expectedAmount,
        double deviationPercentage,
        String anomalyType
    ) {}

    public record TimeBasedPattern(
        Tag category,
        Map<Integer, BigDecimal> hourlyDistribution, // 0-23
        Map<Integer, BigDecimal> dailyDistribution,  // 1-7
        Map<Integer, BigDecimal> monthlyDistribution // 1-12
    ) {}
} 