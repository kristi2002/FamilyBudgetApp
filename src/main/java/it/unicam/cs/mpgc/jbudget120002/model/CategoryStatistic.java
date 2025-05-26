package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;

public class CategoryStatistic {
    private final Tag category;
    private final BigDecimal currentAmount;
    private final BigDecimal previousAmount;
    private final BigDecimal difference;
    private final double percentageChange;

    public CategoryStatistic(Tag category, BigDecimal currentAmount, BigDecimal previousAmount) {
        this.category = category;
        this.currentAmount = currentAmount;
        this.previousAmount = previousAmount;
        this.difference = currentAmount.subtract(previousAmount);
        this.percentageChange = previousAmount.doubleValue() == 0 ? 0 :
            (difference.doubleValue() / previousAmount.doubleValue()) * 100;
    }

    public Tag getCategory() {
        return category;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public BigDecimal getPreviousAmount() {
        return previousAmount;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public double getPercentageChange() {
        return percentageChange;
    }
} 