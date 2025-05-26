package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.util.Optional;

public class TransactionStatisticsImpl implements TransactionStatistics {
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int transactionCount = 0;
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expenses = BigDecimal.ZERO;
    private int incomeCount = 0;
    private int expenseCount = 0;
    private BigDecimal maxAmount = null;
    private BigDecimal minAmount = null;

    @Override
    public void addTransaction(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        totalAmount = totalAmount.add(amount);
        transactionCount++;

        if (transaction.isIncome()) {
            income = income.add(amount);
            incomeCount++;
        } else {
            expenses = expenses.add(amount);
            expenseCount++;
        }

        if (maxAmount == null || amount.compareTo(maxAmount) > 0) {
            maxAmount = amount;
        }
        if (minAmount == null || amount.compareTo(minAmount) < 0) {
            minAmount = amount;
        }
    }

    @Override
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Override
    public int getTransactionCount() {
        return transactionCount;
    }

    @Override
    public BigDecimal getIncome() {
        return income;
    }

    @Override
    public BigDecimal getExpenses() {
        return expenses;
    }

    @Override
    public BigDecimal getBalance() {
        return income.subtract(expenses);
    }

    @Override
    public int getIncomeCount() {
        return incomeCount;
    }

    @Override
    public int getExpenseCount() {
        return expenseCount;
    }

    @Override
    public int getTotalCount() {
        return transactionCount;
    }

    @Override
    public BigDecimal getMaxAmount() {
        return maxAmount != null ? maxAmount : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getMinAmount() {
        return minAmount != null ? minAmount : BigDecimal.ZERO;
    }

    @Override
    public Optional<BigDecimal> getAverageIncome() {
        if (incomeCount == 0) {
            return Optional.empty();
        }
        return Optional.of(income.divide(BigDecimal.valueOf(incomeCount), 2, java.math.RoundingMode.HALF_UP));
    }

    @Override
    public Optional<BigDecimal> getAverageExpense() {
        if (expenseCount == 0) {
            return Optional.empty();
        }
        return Optional.of(expenses.divide(BigDecimal.valueOf(expenseCount), 2, java.math.RoundingMode.HALF_UP));
    }
} 