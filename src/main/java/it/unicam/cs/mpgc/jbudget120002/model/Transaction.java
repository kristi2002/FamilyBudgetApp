package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    // Default constructor for JPA
    protected Transaction() {}

    public Transaction(LocalDate date, String description, BigDecimal amount, boolean isIncome) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    // Getters
    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
    public Set<Tag> getTags() { return tags; }
    public ScheduledTransaction getScheduledTransaction() { return scheduledTransaction; }
    public LoanAmortizationPlan getLoanPlan() { return loanPlan; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public BigDecimal getInterestAmount() { return interestAmount; }

    // Setters
    public void setDate(LocalDate date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setIncome(boolean income) { isIncome = income; }
    
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getTransactions().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getTransactions().remove(this);
    }

    public void setScheduledTransaction(ScheduledTransaction scheduledTransaction) {
        this.scheduledTransaction = scheduledTransaction;
    }

    public void setLoanDetails(LoanAmortizationPlan plan, BigDecimal principal, BigDecimal interest) {
        this.loanPlan = plan;
        this.principalAmount = principal;
        this.interestAmount = interest;
        this.amount = principal.add(interest);
    }
}
