package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Installment {
    private int number;
    private LocalDate dueDate;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal totalPayment;

    public Installment(int number, LocalDate dueDate, BigDecimal principal, BigDecimal interest, BigDecimal totalPayment) {
        this.number = number;
        this.dueDate = dueDate;
        this.principal = principal;
        this.interest = interest;
        this.totalPayment = totalPayment;
    }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }
    public BigDecimal getInterest() { return interest; }
    public void setInterest(BigDecimal interest) { this.interest = interest; }
    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }
} 