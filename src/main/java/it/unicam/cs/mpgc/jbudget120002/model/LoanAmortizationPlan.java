package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_amortization_plans")
public class LoanAmortizationPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @Column(nullable = false)
    private int termInMonths;

    @Column(nullable = false)
    private LocalDate startDate;

    @OneToMany(mappedBy = "loanPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> installments = new ArrayList<>();

    // Default constructor for JPA
    protected LoanAmortizationPlan() {}

    public LoanAmortizationPlan(String description, BigDecimal principalAmount, 
                               BigDecimal annualInterestRate, int termInMonths, 
                               LocalDate startDate) {
        this.description = description;
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.termInMonths = termInMonths;
        this.startDate = startDate;
        generateAmortizationSchedule();
    }

    private void generateAmortizationSchedule() {
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = calculateMonthlyPayment(monthlyRate);
        
        BigDecimal remainingBalance = principalAmount;
        LocalDate paymentDate = startDate;

        for (int month = 1; month <= termInMonths; month++) {
            BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = monthlyPayment.subtract(interest);
            
            if (month == termInMonths) {
                // Last payment - adjust for rounding
                principal = remainingBalance;
                monthlyPayment = principal.add(interest);
            }

            Transaction installment = new Transaction(
                paymentDate,
                String.format("%s - Payment %d/%d", description, month, termInMonths),
                monthlyPayment,
                false
            );
            installment.setLoanDetails(this, principal, interest);
            installments.add(installment);

            remainingBalance = remainingBalance.subtract(principal);
            paymentDate = paymentDate.plusMonths(1);
        }
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal monthlyRate) {
        // PMT formula: P * (r * (1 + r)^n) / ((1 + r)^n - 1)
        BigDecimal numerator = monthlyRate.multiply(
            BigDecimal.ONE.add(monthlyRate).pow(termInMonths)
        );
        BigDecimal denominator = BigDecimal.ONE.add(monthlyRate).pow(termInMonths).subtract(BigDecimal.ONE);
        
        return principalAmount.multiply(numerator).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    // Getters
    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public int getTermInMonths() { return termInMonths; }
    public LocalDate getStartDate() { return startDate; }
    public List<Transaction> getInstallments() { return installments; }
}
