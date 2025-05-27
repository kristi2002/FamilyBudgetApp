package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Installment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanService {
    /**
     * Generates an amortization schedule for a fixed-rate loan.
     */
    public List<Installment> generateAmortizationSchedule(BigDecimal amount, double annualRate, int termMonths, LocalDate startDate) {
        List<Installment> schedule = new ArrayList<>();
        if (amount == null || annualRate <= 0 || termMonths <= 0 || startDate == null) return schedule;

        double monthlyRate = annualRate / 12.0 / 100.0;
        BigDecimal monthlyPayment = amount.multiply(BigDecimal.valueOf(monthlyRate))
            .divide(BigDecimal.ONE.subtract(BigDecimal.valueOf(Math.pow(1 + monthlyRate, -termMonths))), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal remaining = amount;
        LocalDate dueDate = startDate;

        for (int i = 1; i <= termMonths; i++) {
            BigDecimal interest = remaining.multiply(BigDecimal.valueOf(monthlyRate)).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal principal = monthlyPayment.subtract(interest).setScale(2, BigDecimal.ROUND_HALF_UP);
            if (i == termMonths) {
                principal = remaining;
                monthlyPayment = principal.add(interest);
            }
            schedule.add(new Installment(i, dueDate, principal, interest, monthlyPayment));
            remaining = remaining.subtract(principal).setScale(2, BigDecimal.ROUND_HALF_UP);
            dueDate = dueDate.plusMonths(1);
        }
        return schedule;
    }
} 