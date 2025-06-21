package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public interface TransactionService {
    Transaction createTransaction(LocalDate date, String description, BigDecimal amount, 
        boolean isIncome, Set<Long> tagIds);
    
    // User-aware version for multi-user support
    Transaction createTransaction(User user, LocalDate date, String description, BigDecimal amount, 
        boolean isIncome, Set<Long> tagIds);
    
    void deleteTransaction(Long id);

    List<Transaction> findAllForUser(User user);
    
    List<Transaction> findAll();
    
    List<Transaction> findTransactions(User user, String searchTerm, LocalDate startDate, LocalDate endDate, Tag category, boolean includeSubcategories);
    
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
    
    /**
     * Calculate the net worth as of a specific date
     * @param asOfDate the date to calculate net worth for
     * @return the net worth (total income minus total expenses) up to the given date
     */
    BigDecimal calculateNetWorth(LocalDate asOfDate);

    List<Transaction> findTransactionsInPeriod(LocalDate startDate, LocalDate endDate, int limit);

    // User-aware versions for multi-user support
    List<Transaction> findTransactionsInPeriodForUser(User user, LocalDate startDate, LocalDate endDate, int limit);
    
    BigDecimal calculateBalanceForUser(User user, LocalDate start, LocalDate end);
    
    BigDecimal calculateIncomeForPeriodForUser(User user, LocalDate startDate, LocalDate endDate);
    
    BigDecimal calculateExpensesForPeriodForUser(User user, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByDateRangeForUser(User user, LocalDate start, LocalDate end);
}
