package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionStatistics {
    void addTransaction(Transaction transaction);
    BigDecimal getTotalAmount();
    int getTransactionCount();
    BigDecimal getIncome();
    BigDecimal getExpenses();
    BigDecimal getBalance();
    int getIncomeCount();
    int getExpenseCount();
    int getTotalCount();
    BigDecimal getMaxAmount();
    BigDecimal getMinAmount();
    Optional<BigDecimal> getAverageIncome();
    Optional<BigDecimal> getAverageExpense();
} 