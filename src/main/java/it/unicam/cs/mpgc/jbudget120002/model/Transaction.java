package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Core entity class representing a financial transaction in the Family Budget App.
 * This class encapsulates all details of a monetary transaction, including amount,
 * date, description, and categorization.
 *
 * Responsibilities:
 * - Store transaction details (amount, date, description)
 * - Track transaction type (income/expense)
 * - Manage transaction categorization through tags
 * - Support currency handling
 * - Link to related entities (scheduled transactions, loan plans)
 *
 * Usage:
 * Used throughout the application to represent financial transactions,
 * serving as the primary data model for transaction management and reporting.
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean isIncome;

    @ManyToMany
    @JoinTable(
        name = "transaction_tags",
        joinColumns = @JoinColumn(name = "transaction_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_transaction_id")
    private ScheduledTransaction scheduledTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_plan_id")
    private LoanAmortizationPlan loanPlan;

    @Column(name = "principal_amount")
    private BigDecimal principalAmount;

    @Column(name = "interest_amount")
    private BigDecimal interestAmount;

    @Column(name = "currency", nullable = false)
    private String currency = "EUR"; // Default currency

    // Default constructor for JPA
    protected Transaction() {}

    public Transaction(LocalDate date, String description, BigDecimal amount, boolean isIncome) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    public Transaction(LocalDate date, String description, BigDecimal amount, boolean isIncome, String currency) {
        this(date, description, amount, isIncome);
        this.currency = currency;
    }

    // Getters
    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
    public Set<Tag> getTags() { return Collections.unmodifiableSet(tags); }
    public ScheduledTransaction getScheduledTransaction() { return scheduledTransaction; }
    public LoanAmortizationPlan getLoanPlan() { return loanPlan; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public BigDecimal getInterestAmount() { return interestAmount; }
    public String getCurrency() { return currency; }

    // Setters
    public void setDate(LocalDate date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setIncome(boolean income) { isIncome = income; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    // Tag Management
    public void addTag(Tag tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        if (tag != null) {
            tags.remove(tag);
        }
    }

    public void clearTags() {
        tags = new HashSet<>();
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public boolean hasAnyTag(Collection<Tag> searchTags) {
        return searchTags.stream().anyMatch(this::hasTag);
    }

    public boolean hasAllTags(Collection<Tag> searchTags) {
        return tags.containsAll(searchTags);
    }

    public void setTags(Collection<Tag> newTags) {
        tags = new HashSet<>();
        if (newTags != null) {
            for (Tag tag : newTags) {
                addTag(tag);
            }
        }
    }

    // Loan Management
    public void setScheduledTransaction(ScheduledTransaction scheduledTransaction) {
        this.scheduledTransaction = scheduledTransaction;
    }

    public void setLoanDetails(LoanAmortizationPlan plan, BigDecimal principal, BigDecimal interest) {
        this.loanPlan = plan;
        this.principalAmount = principal;
        this.interestAmount = interest;
        this.amount = principal.add(interest);
    }

    // Utility Methods
    public boolean isExpense() {
        return !isIncome;
    }

    public BigDecimal getSignedAmount() {
        return isIncome ? amount : amount.negate();
    }

    public boolean isLoanPayment() {
        return loanPlan != null;
    }

    public boolean isScheduled() {
        return scheduledTransaction != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s: %s %s %s", 
            date, 
            description, 
            isIncome ? "+" : "-",
            amount);
    }
}
