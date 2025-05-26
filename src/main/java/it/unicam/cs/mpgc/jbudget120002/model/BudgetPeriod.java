package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "budget_periods")
public class BudgetPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalExpectedIncome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalExpectedExpenses;

    @ElementCollection
    @CollectionTable(
        name = "budget_category_expenses",
        joinColumns = @JoinColumn(name = "budget_period_id")
    )
    @MapKeyJoinColumn(name = "tag_id")
    @Column(name = "expected_amount", precision = 10, scale = 2)
    private Map<Tag, BigDecimal> categoryBudgets = new HashMap<>();

    @Transient
    private Map<Tag, BigDecimal> actualExpensesByCategory = new HashMap<>();

    // Default constructor for JPA
    protected BudgetPeriod() {}

    public BudgetPeriod(LocalDate startDate, LocalDate endDate,
                        BigDecimal totalExpectedIncome,
                        BigDecimal totalExpectedExpenses) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalExpectedIncome = totalExpectedIncome;
        this.totalExpectedExpenses = totalExpectedExpenses;
    }

    public void setBudgetForCategory(Tag category, BigDecimal amount) {
        categoryBudgets.put(category, amount);
        // Recalculate total expected expenses
        totalExpectedExpenses = categoryBudgets.values()
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void calculateActualExpenses(Set<Transaction> transactions) {
        actualExpensesByCategory.clear();
        
        // Filter transactions within the budget period
        Set<Transaction> periodTransactions = transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .collect(Collectors.toSet());

        // Calculate expenses by category
        for (Transaction transaction : periodTransactions) {
            if (!transaction.isIncome()) {
                for (Tag tag : transaction.getTags()) {
                    actualExpensesByCategory.merge(
                        tag,
                        transaction.getAmount(),
                        BigDecimal::add
                    );
                }
            }
        }
    }

    public BigDecimal getBudgetForCategory(Tag category) {
        return categoryBudgets.getOrDefault(category, BigDecimal.ZERO);
    }

    public BigDecimal getActualExpensesForCategory(Tag category) {
        return actualExpensesByCategory.getOrDefault(category, BigDecimal.ZERO);
    }

    public BigDecimal getRemainingBudgetForCategory(Tag category) {
        return getBudgetForCategory(category)
            .subtract(getActualExpensesForCategory(category));
    }

    public boolean isOverBudget(Tag category) {
        return getRemainingBudgetForCategory(category).compareTo(BigDecimal.ZERO) < 0;
    }

    public Map<Tag, BigDecimal> getCategoryBudgets() {
        return new HashMap<>(categoryBudgets);
    }

    public Map<Tag, BigDecimal> getActualExpensesByCategory() {
        return new HashMap<>(actualExpensesByCategory);
    }

    // Getters
    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getTotalExpectedIncome() { return totalExpectedIncome; }
    public BigDecimal getTotalExpectedExpenses() { return totalExpectedExpenses; }

    // Setters
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTotalExpectedIncome(BigDecimal totalExpectedIncome) { 
        this.totalExpectedIncome = totalExpectedIncome; 
    }
}
