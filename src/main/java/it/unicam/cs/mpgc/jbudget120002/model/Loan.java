package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Loan {
    private BigDecimal amount;
    private double interestRate; // annual, as percent
    private int termMonths;
    private LocalDate startDate;
    private List<Installment> installments;

    public Loan(BigDecimal amount, double interestRate, int termMonths, LocalDate startDate, List<Installment> installments) {
        this.amount = amount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.startDate = startDate;
        this.installments = installments;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public List<Installment> getInstallments() { return installments; }
    public void setInstallments(List<Installment> installments) { this.installments = installments; }
} 