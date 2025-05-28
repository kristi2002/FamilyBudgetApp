package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a budget plan in the Family Budget App.
 * This class manages budget allocations for specific categories and time periods,
 * tracking spending limits and actual expenditures.
 *
 * Responsibilities:
 * - Define budget amounts and time periods
 * - Track budget categories through tags
 * - Monitor budget utilization
 * - Support budget templates and recurring budgets
 * - Enable budget alerts and notifications
 *
 * Usage:
 * Used by BudgetService to manage budget planning and tracking,
 * and by StatisticsService to provide budget utilization insights.
 */
@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToMany
    @JoinTable(
        name = "budget_tags",
        joinColumns = @JoinColumn(name = "budget_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Default constructor for JPA
    protected Budget() {}

    public Budget(String name, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Set<Tag> getTags() { return tags; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }
} 