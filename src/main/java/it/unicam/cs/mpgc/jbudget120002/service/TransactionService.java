package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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

    List<Transaction> findByTag(Tag tag, boolean includeChildren);

    List<Transaction> findByTags(Collection<Tag> tags, boolean matchAll);

    Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate, boolean includeChildren);

    Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate, boolean includeChildren);

    /**
     * Find all transactions between two dates (inclusive)
     */
    List<Transaction> findTransactionsBetweenDates(LocalDate startDate, LocalDate endDate);

    BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate);
}
