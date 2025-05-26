package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;

public class CategoryComparison {
    private final Tag category;
    private final BigDecimal previousAmount;
    private final BigDecimal currentAmount;
    private final BigDecimal difference;
    private final double changePercentage;

    public CategoryComparison(Tag category, BigDecimal previousAmount, BigDecimal currentAmount,
                            BigDecimal difference, double changePercentage) {
        this.category = category;
        this.previousAmount = previousAmount;
        this.currentAmount = currentAmount;
        this.difference = difference;
        this.changePercentage = changePercentage;
    }

    public Tag getCategory() {
        return category;
    }

    public BigDecimal getPreviousAmount() {
        return previousAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public double getChangePercentage() {
        return changePercentage;
    }
} 