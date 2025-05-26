package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "scheduled_transactions")
public class ScheduledTransaction {
    public enum ProcessingState {
        PENDING,
        COMPLETED,
        CANCELLED
    }

    public enum RecurrencePattern {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean isIncome;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrencePattern recurrencePattern;

    @Column(nullable = false)
    private int recurrenceValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingState processingState = ProcessingState.PENDING;

    @ManyToMany
    @JoinTable(
        name = "scheduled_transaction_tags",
        joinColumns = @JoinColumn(name = "scheduled_transaction_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "scheduledTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> generatedTransactions = new ArrayList<>();

    // Default constructor for JPA
    protected ScheduledTransaction() {}

    public ScheduledTransaction(String description, BigDecimal amount, boolean isIncome,
                              LocalDate startDate, LocalDate endDate,
                              RecurrencePattern pattern, int value) {
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recurrencePattern = pattern;
        this.recurrenceValue = value;
    }

    public void generateTransactions(LocalDate until) {
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
            currentDate = switch (recurrencePattern) {
                case DAILY -> currentDate.plusDays(recurrenceValue);
                case WEEKLY -> currentDate.plusWeeks(recurrenceValue);
                case MONTHLY -> currentDate.plusMonths(recurrenceValue);
                case YEARLY -> currentDate.plusYears(recurrenceValue);
            };
        }
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    // Getters
    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }
    public int getRecurrenceValue() { return recurrenceValue; }
    public ProcessingState getProcessingState() { return processingState; }
    public Set<Tag> getTags() { return tags; }
    public List<Transaction> getGeneratedTransactions() { return generatedTransactions; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setIncome(boolean income) { isIncome = income; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setRecurrencePattern(RecurrencePattern pattern) { this.recurrencePattern = pattern; }
    public void setRecurrenceValue(int value) { this.recurrenceValue = value; }
    public void setProcessingState(ProcessingState state) { this.processingState = state; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }
}
