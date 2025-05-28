package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class BudgetTableItem {
    private final Long id;
    private final String name;
    private final BigDecimal budgetedAmount;
    private final BigDecimal actualAmount;
    private final BigDecimal remainingAmount;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Set<Tag> tags;

    public BudgetTableItem(Long id, String name, BigDecimal budgetedAmount,
                          BigDecimal actualAmount, BigDecimal remainingAmount,
                          LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.budgetedAmount = budgetedAmount;
        this.actualAmount = actualAmount;
        this.remainingAmount = remainingAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tags = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public Set<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }
} 