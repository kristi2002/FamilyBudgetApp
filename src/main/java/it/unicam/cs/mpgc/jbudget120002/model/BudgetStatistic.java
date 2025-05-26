package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;

public class BudgetStatistic {
    private final Tag category;
    private final BigDecimal budgetAmount;
    private final BigDecimal actualAmount;
    private final BigDecimal variance;
    private final double progress;

    public BudgetStatistic(Tag category, BigDecimal budgetAmount, BigDecimal actualAmount) {
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.actualAmount = actualAmount;
        this.variance = budgetAmount.subtract(actualAmount);
        this.progress = budgetAmount.doubleValue() == 0 ? 0 :
            (actualAmount.doubleValue() / budgetAmount.doubleValue()) * 100;
    }

    public Tag getCategory() {
        return category;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public double getProgress() {
        return progress;
    }

    public double getUtilizationPercentage() {
        if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return actualAmount.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }
        return actualAmount
            .divide(budgetAmount, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
} 