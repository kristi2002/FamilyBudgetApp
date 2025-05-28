package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BudgetComparison {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Map<Long, CategoryComparison> categoryComparisons;
    private final BigDecimal totalBudgeted;
    private final BigDecimal totalActual;
    private final BigDecimal totalVariance;
    private final double overallVariancePercentage;

    public static class CategoryComparison {
        private final Long categoryId;
        private final String categoryName;
        private final BigDecimal budgetedAmount;
        private final BigDecimal actualAmount;
        private final BigDecimal variance;
        private final double variancePercentage;

        public CategoryComparison(Long categoryId, String categoryName,
                                BigDecimal budgetedAmount, BigDecimal actualAmount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.budgetedAmount = budgetedAmount;
            this.actualAmount = actualAmount;
            this.variance = actualAmount.subtract(budgetedAmount);
            this.variancePercentage = budgetedAmount.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                variance.divide(budgetedAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public BigDecimal getBudgetedAmount() {
            return budgetedAmount;
        }

        public BigDecimal getActualAmount() {
            return actualAmount;
        }

        public BigDecimal getVariance() {
            return variance;
        }

        public double getVariancePercentage() {
            return variancePercentage;
        }

        public boolean isOverBudget() {
            return variance.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    public BudgetComparison(LocalDate startDate, LocalDate endDate,
                           Map<Long, CategoryComparison> categoryComparisons) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryComparisons = new HashMap<>(categoryComparisons);
        
        this.totalBudgeted = categoryComparisons.values().stream()
            .map(CategoryComparison::getBudgetedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        this.totalActual = categoryComparisons.values().stream()
            .map(CategoryComparison::getActualAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        this.totalVariance = totalActual.subtract(totalBudgeted);
        
        this.overallVariancePercentage = totalBudgeted.compareTo(BigDecimal.ZERO) == 0 ? 0 :
            totalVariance.divide(totalBudgeted, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Map<Long, CategoryComparison> getCategoryComparisons() {
        return new HashMap<>(categoryComparisons);
    }

    public BigDecimal getTotalBudgeted() {
        return totalBudgeted;
    }

    public BigDecimal getTotalActual() {
        return totalActual;
    }

    public BigDecimal getTotalVariance() {
        return totalVariance;
    }

    public double getOverallVariancePercentage() {
        return overallVariancePercentage;
    }

    public boolean isOverBudget() {
        return totalVariance.compareTo(BigDecimal.ZERO) > 0;
    }
} 