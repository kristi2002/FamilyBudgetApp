package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyStatistic {
    private final YearMonth month;
    private final BigDecimal income;
    private final BigDecimal expenses;
    private final BigDecimal balance;
    private final double savingsRate;

    public MonthlyStatistic(YearMonth month, BigDecimal income, BigDecimal expenses) {
        this.month = month;
        this.income = income;
        this.expenses = expenses;
        this.balance = income.subtract(expenses);
        this.savingsRate = income.doubleValue() == 0 ? 0 :
            (balance.doubleValue() / income.doubleValue()) * 100;
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
        return savingsRate;
    }
} 