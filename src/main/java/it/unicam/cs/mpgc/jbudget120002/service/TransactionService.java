package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransactionService {
    Transaction createTransaction(LocalDate date, String description, BigDecimal amount, 
        boolean isIncome, Set<Long> tagIds);
    
    void deleteTransaction(Long id);
    
    List<Transaction> findAll();
    
    Transaction findById(Long id);
    
    List<Transaction> findByDateRange(LocalDate start, LocalDate end);
    
    void updateTransaction(Long id, LocalDate date, String description, 
        BigDecimal amount, boolean isIncome, Set<Long> tagIds);
    
    BigDecimal calculateBalance(LocalDate start, LocalDate end);

    List<Transaction> findByTag(Long tagId);

    List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByScheduledTransaction(Long scheduledTransactionId);

    List<Transaction> findByLoanPlan(Long loanPlanId);

    BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate);

    BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate);
}
