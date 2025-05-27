package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public interface TransactionService {
    Transaction createTransaction(LocalDate date, String description, BigDecimal amount, 
        boolean isIncome, Set<Long> tagIds);
    
    Transaction createTransaction(LocalDate date, String description, BigDecimal amount, 
        boolean isIncome, Set<Long> tagIds, String currency);
    
    void deleteTransaction(Long id);
    
    List<Transaction> findAll();
    
    Transaction findById(Long id);
    
    List<Transaction> findByDateRange(LocalDate start, LocalDate end);
    
    void updateTransaction(Long id, LocalDate date, String description, 
        BigDecimal amount, boolean isIncome, Set<Long> tagIds);
    
    void updateTransaction(Long id, LocalDate date, String description, 
        BigDecimal amount, boolean isIncome, Set<Long> tagIds, String currency);
    
    BigDecimal calculateBalance(LocalDate start, LocalDate end);
    
    BigDecimal calculateBalance(LocalDate start, LocalDate end, String targetCurrency);

    List<Transaction> findByTag(Long tagId);

    List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByScheduledTransaction(Long scheduledTransactionId);

    List<Transaction> findByLoanPlan(Long loanPlanId);

    BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate);
    
    BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate, String targetCurrency);

    BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate);
    
    BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate, String targetCurrency);

    List<Transaction> findByTag(Tag tag, boolean includeChildren);

    List<Transaction> findByTags(Collection<Tag> tags, boolean matchAll);

    Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate, boolean includeChildren);
    
    Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate, 
        boolean includeChildren, String targetCurrency);

    Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate, boolean includeChildren);
    
    Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate, 
        boolean includeChildren, String targetCurrency);

    /**
     * Find all transactions between two dates (inclusive)
     */
    List<Transaction> findTransactionsBetweenDates(LocalDate startDate, LocalDate endDate);

    BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate);
    
    BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate, 
        String targetCurrency);

    /**
     * Calculate the net worth as of a specific date
     * @param asOfDate the date to calculate net worth for
     * @return the net worth (total income minus total expenses) up to the given date
     */
    BigDecimal calculateNetWorth(LocalDate asOfDate);
    
    BigDecimal calculateNetWorth(LocalDate asOfDate, String targetCurrency);
}
