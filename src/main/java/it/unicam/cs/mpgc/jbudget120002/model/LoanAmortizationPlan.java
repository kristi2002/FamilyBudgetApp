package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a loan amortization plan in the Family Budget App.
 * This class manages loan details, payment schedules, and amortization calculations
 * for tracking and managing loans and their payments.
 *
 * Responsibilities:
 * - Store loan details (principal, interest rate, term)
 * - Calculate payment schedules
 * - Track payment progress
 * - Manage loan status and balances
 * - Link to related transactions
 *
 * Usage:
 * Used by the application to manage loans and their payments,
 * providing detailed tracking and amortization calculations.
 */
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

    /**
     * Generates the amortization schedule for the loan.
     * Uses the standard PMT formula to calculate monthly payments and splits each payment into principal and interest.
     */
    private void generateAmortizationSchedule() {
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = calculateMonthlyPayment(monthlyRate);
        
        BigDecimal remainingBalance = principalAmount;
        LocalDate paymentDate = startDate;

        for (int month = 1; month <= termInMonths; month++) {
            // Calculate interest for the current month
            BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            // Calculate principal for the current month
            BigDecimal principal = monthlyPayment.subtract(interest);
            
            if (month == termInMonths) {
                // Last payment - adjust for rounding to ensure the loan is fully paid off
                principal = remainingBalance;
                monthlyPayment = principal.add(interest);
            }

            // Create a transaction for this installment
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

    /**
     * Calculates the fixed monthly payment using the PMT formula:
     * PMT = P * (r * (1 + r)^n) / ((1 + r)^n - 1)
     * where:
     *   P = principal amount
     *   r = monthly interest rate
     *   n = total number of payments (months)
     */
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
