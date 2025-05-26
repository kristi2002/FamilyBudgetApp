package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyBalance {
    private final YearMonth month;
    private final BigDecimal income;
    private final BigDecimal expenses;
    private final BigDecimal balance;

    public MonthlyBalance(YearMonth month, BigDecimal income, BigDecimal expenses, BigDecimal balance) {
        this.month = month;
        this.income = income;
        this.expenses = expenses;
        this.balance = balance;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public double getSavingsRate() {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return balance.divide(income, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
} 