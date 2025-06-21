package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Objects;

/**
 * Core entity class representing a financial transaction in the Family Budget App.
 * 
 * <p>This class encapsulates all details of a monetary transaction, including amount,
 * date, description, and categorization. It serves as the primary data model for
 * financial tracking and reporting in the application.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Store transaction details (amount, date, description)</li>
 *   <li>Track transaction type (income/expense)</li>
 *   <li>Manage transaction categorization through tags</li>
 *   <li>Support currency handling</li>
 *   <li>Link to related entities (scheduled transactions, loan plans)</li>
 *   <li>Calculate signed amounts for balance calculations</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create an income transaction
 * Transaction income = new Transaction(LocalDate.now(), "Salary", new BigDecimal("2500.00"), true);
 * 
 * // Create an expense transaction
 * Transaction expense = new Transaction(LocalDate.now(), "Groceries", new BigDecimal("150.00"), false);
 * 
 * // Add tags for categorization
 * expense.addTag(foodTag);
 * 
 * // Calculate signed amount for balance
 * BigDecimal signedAmount = expense.getSignedAmount(); // Returns -150.00
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean isIncome;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "principal_amount", precision = 10, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 10, scale = 2)
    private BigDecimal interestAmount;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for JPA.
     */
    protected Transaction() {}

    /**
     * Creates a new transaction with basic information.
     * 
     * @param date the transaction date
     * @param description the transaction description
     * @param amount the transaction amount
     * @param isIncome true if this is an income transaction, false if expense
     * @throws IllegalArgumentException if any required parameter is null or invalid
     */
    public Transaction(LocalDate date, String description, BigDecimal amount, boolean isIncome) {
        validateConstructorParams(date, description, amount);
        
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates constructor parameters.
     * 
     * @param date the date to validate
     * @param description the description to validate
     * @param amount the amount to validate
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateConstructorParams(LocalDate date, String description, BigDecimal amount) {
        if (date == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction description cannot be null or empty");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Adds a tag to this transaction.
     * 
     * @param tag the tag to add
     * @return true if the tag was added, false if already present
     */
    public boolean addTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        return tags.add(tag);
    }

    /**
     * Removes a tag from this transaction.
     * 
     * @param tag the tag to remove
     * @return true if the tag was removed, false if not present
     */
    public boolean removeTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        return tags.remove(tag);
    }

    /**
     * Clears all tags from this transaction.
     */
    public void clearTags() {
        tags.clear();
    }

    /**
     * Checks if this transaction has a specific tag.
     * 
     * @param tag the tag to check
     * @return true if the transaction has the tag, false otherwise
     */
    public boolean hasTag(Tag tag) {
        return tag != null && tags.contains(tag);
    }

    /**
     * Checks if this transaction has any of the specified tags.
     * 
     * @param searchTags the tags to check
     * @return true if the transaction has any of the tags, false otherwise
     */
    public boolean hasAnyTag(Collection<Tag> searchTags) {
        if (searchTags == null || searchTags.isEmpty()) {
            return false;
        }
        return searchTags.stream().anyMatch(this::hasTag);
    }

    /**
     * Checks if this transaction has all of the specified tags.
     * 
     * @param searchTags the tags to check
     * @return true if the transaction has all the tags, false otherwise
     */
    public boolean hasAllTags(Collection<Tag> searchTags) {
        if (searchTags == null || searchTags.isEmpty()) {
            return false;
        }
        return tags.containsAll(searchTags);
    }

    /**
     * Sets the tags for this transaction, replacing any existing tags.
     * 
     * @param newTags the new tags to set
     */
    public void setTags(Collection<Tag> newTags) {
        tags.clear();
        if (newTags != null) {
            for (Tag tag : newTags) {
                if (tag != null) {
                    addTag(tag);
                }
            }
        }
    }

    /**
     * Sets the loan details for this transaction.
     * 
     * @param plan the loan amortization plan
     * @param principal the principal amount
     * @param interest the interest amount
     */
    public void setLoanDetails(LoanAmortizationPlan plan, BigDecimal principal, BigDecimal interest) {
        this.loanPlan = plan;
        this.principalAmount = principal;
        this.interestAmount = interest;
        this.amount = principal.add(interest);
    }

    /**
     * Checks if this transaction is an expense.
     * 
     * @return true if this is an expense transaction, false if income
     */
    public boolean isExpense() {
        return !isIncome;
    }

    /**
     * Gets the signed amount for balance calculations.
     * Income transactions return positive amounts, expenses return negative amounts.
     * 
     * @return the signed amount
     */
    public BigDecimal getSignedAmount() {
        return isIncome ? amount : amount.negate();
    }

    /**
     * Checks if this transaction is a loan payment.
     * 
     * @return true if this transaction is linked to a loan plan, false otherwise
     */
    public boolean isLoanPayment() {
        return loanPlan != null;
    }

    /**
     * Checks if this transaction was created from a scheduled transaction.
     * 
     * @return true if this transaction is linked to a scheduled transaction, false otherwise
     */
    public boolean isScheduled() {
        return scheduledTransaction != null;
    }

    /**
     * Gets the total amount including principal and interest for loan payments.
     * 
     * @return the total amount, or the regular amount if not a loan payment
     */
    public BigDecimal getTotalAmount() {
        if (isLoanPayment() && principalAmount != null && interestAmount != null) {
            return principalAmount.add(interestAmount);
        }
        return amount;
    }

    // ==================== GETTERS ====================

    /**
     * Gets the transaction's unique identifier.
     * 
     * @return the transaction ID
     */
    public Long getId() { 
        return id; 
    }

    /**
     * Gets the transaction date.
     * 
     * @return the transaction date
     */
    public LocalDate getDate() { 
        return date; 
    }

    /**
     * Gets the transaction description.
     * 
     * @return the transaction description
     */
    public String getDescription() { 
        return description; 
    }

    /**
     * Gets the transaction amount.
     * 
     * @return the transaction amount
     */
    public BigDecimal getAmount() { 
        return amount; 
    }

    /**
     * Checks if this transaction is an income transaction.
     * 
     * @return true if this is an income transaction, false if expense
     */
    public boolean isIncome() { 
        return isIncome; 
    }

    /**
     * Gets the transaction tags.
     * 
     * @return an unmodifiable set of tags
     */
    public Set<Tag> getTags() { 
        return Collections.unmodifiableSet(tags); 
    }

    /**
     * Gets the associated scheduled transaction.
     * 
     * @return the scheduled transaction, or null if not linked
     */
    public ScheduledTransaction getScheduledTransaction() { 
        return scheduledTransaction; 
    }

    /**
     * Gets the associated loan amortization plan.
     * 
     * @return the loan plan, or null if not linked
     */
    public LoanAmortizationPlan getLoanPlan() { 
        return loanPlan; 
    }

    /**
     * Gets the user who owns this transaction.
     * 
     * @return the user, or null if not assigned
     */
    public User getUser() { 
        return user; 
    }

    /**
     * Gets the principal amount for loan payments.
     * 
     * @return the principal amount, or null if not a loan payment
     */
    public BigDecimal getPrincipalAmount() { 
        return principalAmount; 
    }

    /**
     * Gets the interest amount for loan payments.
     * 
     * @return the interest amount, or null if not a loan payment
     */
    public BigDecimal getInterestAmount() { 
        return interestAmount; 
    }

    /**
     * Gets the transaction currency.
     * 
     * @return the currency code
     */
    public String getCurrency() { 
        return currency; 
    }

    // ==================== SETTERS ====================

    /**
     * Sets the transaction date.
     * 
     * @param date the transaction date
     */
    public void setDate(LocalDate date) { 
        this.date = date; 
    }

    /**
     * Sets the transaction description.
     * 
     * @param description the transaction description
     */
    public void setDescription(String description) { 
        this.description = description; 
    }

    /**
     * Sets the transaction amount.
     * 
     * @param amount the transaction amount
     */
    public void setAmount(BigDecimal amount) { 
        this.amount = amount; 
    }

    /**
     * Sets whether this transaction is an income transaction.
     * 
     * @param income true if this is an income transaction, false if expense
     */
    public void setIncome(boolean income) { 
        isIncome = income; 
    }

    /**
     * Sets the transaction currency.
     * 
     * @param currency the currency code
     */
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }

    /**
     * Sets the user who owns this transaction.
     * 
     * @param user the user
     */
    public void setUser(User user) { 
        this.user = user; 
    }

    /**
     * Sets the associated scheduled transaction.
     * 
     * @param scheduledTransaction the scheduled transaction
     */
    public void setScheduledTransaction(ScheduledTransaction scheduledTransaction) {
        this.scheduledTransaction = scheduledTransaction;
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, date=%s, description='%s', amount=%s, isIncome=%s, currency='%s'}", 
                           id, date, description, amount, isIncome, currency);
    }
}
