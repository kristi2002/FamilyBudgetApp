package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

/**
 * Entity class representing a scheduled or recurring transaction in the Family Budget App.
 * 
 * <p>This class manages transactions that are planned for future execution or occur
 * on a regular basis, such as monthly bills or recurring income. It supports various
 * recurrence patterns and can automatically generate actual transactions based on
 * the defined schedule.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Define transaction schedule and frequency</li>
 *   <li>Track next execution date</li>
 *   <li>Manage recurring transaction patterns</li>
 *   <li>Link to generated transactions</li>
 *   <li>Support schedule modifications and cancellations</li>
 *   <li>Automate transaction generation</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a monthly rent payment
 * ScheduledTransaction rent = new ScheduledTransaction("Rent Payment", 
 *     new BigDecimal("1200.00"), false, LocalDate.now(), null, 
 *     RecurrencePattern.MONTHLY, 1);
 * 
 * // Generate transactions for the next 6 months
 * rent.generateTransactions(LocalDate.now().plusMonths(6));
 * 
 * // Check if scheduled transaction is active
 * boolean isActive = rent.isActive(LocalDate.now());
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "scheduled_transactions")
public class ScheduledTransaction {
    
    /**
     * Represents the processing state of a scheduled transaction.
     */
    public enum ProcessingState {
        /** Transaction is pending and will be processed */
        PENDING,
        /** Transaction has been completed */
        COMPLETED,
        /** Transaction has been cancelled */
        CANCELLED
    }

    /**
     * Represents the recurrence pattern for scheduled transactions.
     */
    public enum RecurrencePattern {
        /** Transaction occurs daily */
        DAILY,
        /** Transaction occurs weekly */
        WEEKLY,
        /** Transaction occurs monthly */
        MONTHLY,
        /** Transaction occurs yearly */
        YEARLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean isIncome;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(name = "RECURRENCEVALUE", nullable = false)
    private Integer recurrenceValue = 1;

    @Column(name = "RECURRENCEPATTERN", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecurrencePattern pattern = RecurrencePattern.MONTHLY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingState processingState = ProcessingState.PENDING;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "scheduled_transaction_tags",
        joinColumns = @JoinColumn(name = "scheduled_transaction_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "scheduledTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> generatedTransactions = new ArrayList<>();

    @Column(length = 100)
    private String category;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for JPA.
     */
    protected ScheduledTransaction() {}

    /**
     * Creates a new scheduled transaction with basic information.
     * 
     * @param description the transaction description
     * @param amount the transaction amount
     * @param isIncome true if this is an income transaction, false if expense
     * @param startDate the start date for the schedule
     * @param endDate the end date for the schedule (can be null for indefinite)
     * @param pattern the recurrence pattern
     * @param value the recurrence value (e.g., every 2 months)
     * @throws IllegalArgumentException if any required parameter is null or invalid
     */
    public ScheduledTransaction(String description, BigDecimal amount, boolean isIncome,
                              LocalDate startDate, LocalDate endDate,
                              RecurrencePattern pattern, Integer value) {
        validateConstructorParams(description, amount, startDate, endDate, pattern, value);
        
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pattern = (pattern != null) ? pattern : RecurrencePattern.MONTHLY;
        this.recurrenceValue = (value != null) ? value : 1;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates constructor parameters.
     * 
     * @param description the description to validate
     * @param amount the amount to validate
     * @param startDate the start date to validate
     * @param endDate the end date to validate
     * @param pattern the pattern to validate
     * @param value the value to validate
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateConstructorParams(String description, BigDecimal amount, 
                                         LocalDate startDate, LocalDate endDate,
                                         RecurrencePattern pattern, Integer value) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Recurrence value must be positive");
        }
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Generates transactions for this scheduled transaction up to the specified date.
     * 
     * @param until the date until which to generate transactions
     */
    public void generateTransactions(LocalDate until) {
        if (until == null || processingState != ProcessingState.PENDING) {
            return;
        }
        
        LocalDate currentDate = startDate;
        LocalDate effectiveEndDate = endDate != null ? endDate : until;

        while (!currentDate.isAfter(effectiveEndDate)) {
            Transaction transaction = new Transaction(
                currentDate,
                description,
                amount,
                isIncome
            );
            transaction.setScheduledTransaction(this);
            
            // Copy tags from scheduled transaction to actual transaction
            for (Tag tag : tags) {
                transaction.addTag(tag);
            }

            generatedTransactions.add(transaction);
            
            // Calculate next occurrence based on pattern
            currentDate = calculateNextOccurrence(currentDate);
        }
    }

    /**
     * Calculates the next occurrence date based on the recurrence pattern.
     * 
     * @param currentDate the current date
     * @return the next occurrence date
     */
    private LocalDate calculateNextOccurrence(LocalDate currentDate) {
        return switch (pattern) {
            case DAILY -> currentDate.plusDays(recurrenceValue);
            case WEEKLY -> currentDate.plusWeeks(recurrenceValue);
            case MONTHLY -> currentDate.plusMonths(recurrenceValue);
            case YEARLY -> currentDate.plusYears(recurrenceValue);
        };
    }

    /**
     * Adds a tag to this scheduled transaction.
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
     * Removes a tag from this scheduled transaction.
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
     * Checks if this scheduled transaction has a specific tag.
     * 
     * @param tag the tag to check
     * @return true if the scheduled transaction has the tag, false otherwise
     */
    public boolean hasTag(Tag tag) {
        return tag != null && tags.contains(tag);
    }

    /**
     * Checks if this scheduled transaction is active for the given date.
     * 
     * @param date the date to check
     * @return true if the scheduled transaction is active on the given date, false otherwise
     */
    public boolean isActive(LocalDate date) {
        if (date == null || processingState != ProcessingState.PENDING) {
            return false;
        }
        
        if (date.isBefore(startDate)) {
            return false;
        }
        
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks if this scheduled transaction is currently active.
     * 
     * @return true if the scheduled transaction is currently active, false otherwise
     */
    public boolean isCurrentlyActive() {
        return isActive(LocalDate.now());
    }

    /**
     * Checks if this scheduled transaction has expired.
     * 
     * @return true if the scheduled transaction has expired, false otherwise
     */
    public boolean isExpired() {
        return endDate != null && LocalDate.now().isAfter(endDate);
    }

    /**
     * Gets the next occurrence date.
     * 
     * @return the next occurrence date, or null if expired or cancelled
     */
    public LocalDate getNextOccurrence() {
        if (processingState != ProcessingState.PENDING || isExpired()) {
            return null;
        }
        
        LocalDate lastGenerated = generatedTransactions.stream()
                .map(Transaction::getDate)
                .max(LocalDate::compareTo)
                .orElse(startDate.minusDays(1));
        
        return calculateNextOccurrence(lastGenerated);
    }

    /**
     * Gets the number of occurrences that should have been generated by a given date.
     * 
     * @param date the date to check
     * @return the number of occurrences
     */
    public int getOccurrenceCount(LocalDate date) {
        if (date == null || date.isBefore(startDate)) {
            return 0;
        }
        
        LocalDate effectiveEndDate = endDate != null ? endDate : date;
        if (date.isAfter(effectiveEndDate)) {
            date = effectiveEndDate;
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, date);
        
        return switch (pattern) {
            case DAILY -> (int) (daysBetween / recurrenceValue) + 1;
            case WEEKLY -> (int) (daysBetween / (7 * recurrenceValue)) + 1;
            case MONTHLY -> (int) (ChronoUnit.MONTHS.between(startDate, date) / recurrenceValue) + 1;
            case YEARLY -> (int) (ChronoUnit.YEARS.between(startDate, date) / recurrenceValue) + 1;
        };
    }

    /**
     * Cancels this scheduled transaction.
     */
    public void cancel() {
        this.processingState = ProcessingState.CANCELLED;
    }

    /**
     * Completes this scheduled transaction.
     */
    public void complete() {
        this.processingState = ProcessingState.COMPLETED;
    }

    /**
     * Reactivates a cancelled scheduled transaction.
     */
    public void reactivate() {
        if (this.processingState == ProcessingState.CANCELLED) {
            this.processingState = ProcessingState.PENDING;
        }
    }

    /**
     * Checks if this scheduled transaction is cancelled.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return processingState == ProcessingState.CANCELLED;
    }

    /**
     * Checks if this scheduled transaction is completed.
     * 
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return processingState == ProcessingState.COMPLETED;
    }

    /**
     * Checks if this scheduled transaction is pending.
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return processingState == ProcessingState.PENDING;
    }

    // ==================== GETTERS AND SETTERS ====================

    /**
     * Gets the scheduled transaction's unique identifier.
     * 
     * @return the scheduled transaction ID
     */
    public Long getId() { 
        return id; 
    }

    /**
     * Sets the scheduled transaction's unique identifier.
     * 
     * @param id the scheduled transaction ID
     */
    public void setId(Long id) { 
        this.id = id; 
    }

    /**
     * Gets the user who owns this scheduled transaction.
     * 
     * @return the user, or null if not assigned
     */
    public User getUser() { 
        return user; 
    }

    /**
     * Sets the user who owns this scheduled transaction.
     * 
     * @param user the user
     */
    public void setUser(User user) { 
        this.user = user; 
    }

    /**
     * Gets the transaction description.
     * 
     * @return the description
     */
    public String getDescription() { 
        return description; 
    }

    /**
     * Sets the transaction description.
     * 
     * @param description the description
     */
    public void setDescription(String description) { 
        this.description = description; 
    }

    /**
     * Gets the transaction amount.
     * 
     * @return the amount
     */
    public BigDecimal getAmount() { 
        return amount; 
    }

    /**
     * Sets the transaction amount.
     * 
     * @param amount the amount
     */
    public void setAmount(BigDecimal amount) { 
        this.amount = amount; 
    }

    /**
     * Checks if this is an income transaction.
     * 
     * @return true if this is an income transaction, false if expense
     */
    public boolean isIncome() { 
        return isIncome; 
    }

    /**
     * Sets whether this is an income transaction.
     * 
     * @param income true if this is an income transaction, false if expense
     */
    public void setIncome(boolean income) { 
        isIncome = income; 
    }

    /**
     * Gets the start date.
     * 
     * @return the start date
     */
    public LocalDate getStartDate() { 
        return startDate; 
    }

    /**
     * Sets the start date.
     * 
     * @param startDate the start date
     */
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    /**
     * Gets the end date.
     * 
     * @return the end date, or null if indefinite
     */
    public LocalDate getEndDate() { 
        return endDate; 
    }

    /**
     * Sets the end date.
     * 
     * @param endDate the end date
     */
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

    /**
     * Gets the recurrence pattern.
     * 
     * @return the recurrence pattern
     */
    public RecurrencePattern getPattern() { 
        return pattern; 
    }

    /**
     * Sets the recurrence pattern.
     * 
     * @param pattern the recurrence pattern
     */
    public void setPattern(RecurrencePattern pattern) { 
        this.pattern = (pattern != null) ? pattern : RecurrencePattern.MONTHLY; 
    }

    /**
     * Gets the recurrence value.
     * 
     * @return the recurrence value
     */
    public Integer getRecurrenceValue() { 
        return recurrenceValue; 
    }

    /**
     * Sets the recurrence value.
     * 
     * @param value the recurrence value
     */
    public void setRecurrenceValue(Integer value) { 
        this.recurrenceValue = (value != null) ? value : 1; 
    }

    /**
     * Gets the processing state.
     * 
     * @return the processing state
     */
    public ProcessingState getProcessingState() { 
        return processingState; 
    }

    /**
     * Sets the processing state.
     * 
     * @param state the processing state
     */
    public void setProcessingState(ProcessingState state) { 
        this.processingState = state; 
    }

    /**
     * Gets the scheduled transaction tags.
     * 
     * @return the set of tags
     */
    public Set<Tag> getTags() { 
        return tags; 
    }

    /**
     * Sets the scheduled transaction tags.
     * 
     * @param tags the set of tags
     */
    public void setTags(Set<Tag> tags) { 
        this.tags = tags != null ? tags : new HashSet<>(); 
    }

    /**
     * Gets the generated transactions.
     * 
     * @return the list of generated transactions
     */
    public List<Transaction> getGeneratedTransactions() { 
        return generatedTransactions; 
    }

    /**
     * Gets the category.
     * 
     * @return the category
     */
    public String getCategory() { 
        return category; 
    }

    /**
     * Sets the category.
     * 
     * @param category the category
     */
    public void setCategory(String category) { 
        this.category = category; 
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledTransaction)) return false;
        ScheduledTransaction that = (ScheduledTransaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ScheduledTransaction{id=%d, description='%s', amount=%s, pattern=%s, state=%s}", 
                           id, description, amount, pattern, processingState);
    }
} 