package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;

/**
 * Entity class representing expense data for a specific category in the Family Budget App.
 * This class tracks the total amount spent in a particular category and provides
 * a simple data structure for category-wise expense tracking.
 *
 * Responsibilities:
 * - Store category-specific expense amounts
 * - Link expenses to their respective categories
 * - Support expense tracking and reporting
 *
 * Usage:
 * Used by StatisticsService and BudgetService to track and report
 * category-wise spending patterns.
 */
public class CategoryExpense {
    private final Tag category;
    private final BigDecimal amount;

    public CategoryExpense(Tag category, BigDecimal amount) {
        this.category = category;
        this.amount = amount;
    }

    public Tag getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }
} 