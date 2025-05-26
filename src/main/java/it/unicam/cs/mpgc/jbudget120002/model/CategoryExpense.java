package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;

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