package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity class representing a budget alert in the Family Budget App.
 * This class is used to notify users when certain budget thresholds are reached
 * or exceeded, helping users stay informed about their spending.
 *
 * Responsibilities:
 * - Store alert conditions and thresholds
 * - Track alert status (triggered, resolved, etc.)
 * - Link alerts to specific budgets or categories
 * - Provide alert details for notification and reporting
 *
 * Usage:
 * Used by BudgetService and NotificationService to monitor budgets and
 * notify users when spending approaches or exceeds defined limits.
 */
public class BudgetAlert {
    private final Long budgetId;
    private final String budgetName;
    private final BigDecimal budgetedAmount;
    private final BigDecimal actualAmount;
    private final BigDecimal threshold;
    private final LocalDate alertDate;
    private final AlertType alertType;
    private final AlertSeverity severity;

    public enum AlertType {
        OVER_BUDGET,
        APPROACHING_LIMIT,
        UNUSUAL_SPENDING,
        BUDGET_EXPIRING
    }

    public enum AlertSeverity {
        INFO,
        WARNING,
        CRITICAL
    }

    public BudgetAlert(Long budgetId, String budgetName, BigDecimal budgetedAmount,
                      BigDecimal actualAmount, BigDecimal threshold, LocalDate alertDate,
                      AlertType alertType, AlertSeverity severity) {
        this.budgetId = budgetId;
        this.budgetName = budgetName;
        this.budgetedAmount = budgetedAmount;
        this.actualAmount = actualAmount;
        this.threshold = threshold;
        this.alertDate = alertDate;
        this.alertType = alertType;
        this.severity = severity;
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

    public BigDecimal getThreshold() {
        return threshold;
    }

    public LocalDate getAlertDate() {
        return alertDate;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return switch (alertType) {
            case OVER_BUDGET -> String.format("Budget '%s' has exceeded its limit of %s. Current spending: %s",
                budgetName, budgetedAmount, actualAmount);
            case APPROACHING_LIMIT -> String.format("Budget '%s' is approaching its limit. Current spending: %s of %s",
                budgetName, actualAmount, budgetedAmount);
            case UNUSUAL_SPENDING -> String.format("Unusual spending detected in budget '%s'. Amount: %s",
                budgetName, actualAmount);
            case BUDGET_EXPIRING -> String.format("Budget '%s' will expire soon on %s",
                budgetName, alertDate);
        };
    }
} 