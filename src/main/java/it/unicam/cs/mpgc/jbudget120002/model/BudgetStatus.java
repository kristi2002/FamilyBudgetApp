package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity class representing the status of a budget in the Family Budget App.
 * This class tracks the current state, progress, and utilization of a budget
 * over its defined period.
 *
 * Responsibilities:
 * - Store current budget status and progress
 * - Track utilization and remaining budget
 * - Provide status updates for reporting and alerts
 * - Link to related budget and transactions
 *
 * Usage:
 * Used by BudgetService and StatisticsService to monitor and report
 * on the status and health of budgets.
 */
public class BudgetStatus {
    private final Long budgetId;
    private final String budgetName;
    private final BigDecimal budgetedAmount;
    private final BigDecimal actualAmount;
    private final BigDecimal remainingAmount;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean isOverBudget;
    private final double percentageUsed;

    public BudgetStatus(Long budgetId, String budgetName, BigDecimal budgetedAmount,
                       BigDecimal actualAmount, LocalDate startDate, LocalDate endDate) {
        this.budgetId = budgetId;
        this.budgetName = budgetName;
        this.budgetedAmount = budgetedAmount;
        this.actualAmount = actualAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remainingAmount = budgetedAmount.subtract(actualAmount);
        this.isOverBudget = actualAmount.compareTo(budgetedAmount) > 0;
        this.percentageUsed = actualAmount.divide(budgetedAmount, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal("100")).doubleValue();
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public BigDecimal getBudgetedAmount() {
        return budgetedAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isOverBudget() {
        return isOverBudget;
    }

    public double getPercentageUsed() {
        return percentageUsed;
    }
} 