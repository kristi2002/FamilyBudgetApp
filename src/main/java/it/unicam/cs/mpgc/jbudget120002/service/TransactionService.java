package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service interface for managing financial transactions in the Family Budget App.
 * 
 * <p>This interface defines the contract for transaction management operations,
 * including CRUD operations, search functionality, and financial calculations.
 * It supports both single-user and multi-user scenarios, with methods that can
 * work with or without user context.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Create, read, update, and delete transactions</li>
 *   <li>Search and filter transactions by various criteria</li>
 *   <li>Calculate financial metrics and statistics</li>
 *   <li>Support tag-based categorization</li>
 *   <li>Handle user-specific transaction management</li>
 *   <li>Provide date range and period-based operations</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a new transaction
 * Transaction transaction = transactionService.createTransaction(
 *     user, LocalDate.now(), "Groceries", new BigDecimal("50.00"), false, tagIds);
 * 
 * // Find transactions for a user
 * List<Transaction> transactions = transactionService.findTransactions(
 *     user, "food", startDate, endDate, foodTag, true);
 * 
 * // Calculate balance for a period
 * BigDecimal balance = transactionService.calculateBalanceForUser(user, startDate, endDate);
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public interface TransactionService {
    
    // ==================== CRUD OPERATIONS ====================

    /**
     * Creates a new transaction without user association.
     * 
     * @param date the transaction date
     * @param description the transaction description
     * @param amount the transaction amount
     * @param isIncome true if this is an income transaction, false if expense
     * @param tagIds the set of tag IDs to associate with the transaction
     * @return the created transaction
     * @throws IllegalArgumentException if any required parameter is null or invalid
     */
    Transaction createTransaction(LocalDate date, String description, BigDecimal amount, 
        boolean isIncome, Set<Long> tagIds);
    
    /**
     * Creates a new transaction associated with a specific user.
     * 
     * @param user the user who owns the transaction
     * @param date the transaction date
     * @param description the transaction description
     * @param amount the transaction amount
     * @param isIncome true if this is an income transaction, false if expense
     * @param tagIds the set of tag IDs to associate with the transaction
     * @return the created transaction
     * @throws IllegalArgumentException if any required parameter is null or invalid
     */
    Transaction createTransaction(User user, LocalDate date, String description, BigDecimal amount, 
        boolean isIncome, Set<Long> tagIds);
    
    /**
     * Updates an existing transaction.
     * 
     * @param id the transaction ID
     * @param date the new transaction date
     * @param description the new transaction description
     * @param amount the new transaction amount
     * @param isIncome true if this is an income transaction, false if expense
     * @param tagIds the new set of tag IDs to associate with the transaction
     * @throws IllegalArgumentException if any required parameter is null or invalid
     * @throws RuntimeException if the transaction is not found
     */
    void updateTransaction(Long id, LocalDate date, String description, 
        BigDecimal amount, boolean isIncome, Set<Long> tagIds);
    
    /**
     * Deletes a transaction by its ID.
     * 
     * @param id the transaction ID to delete
     * @throws RuntimeException if the transaction is not found
     */
    void deleteTransaction(Long id);

    /**
     * Finds a transaction by its ID.
     * 
     * @param id the transaction ID
     * @return the transaction, or null if not found
     */
    Transaction findById(Long id);

    // ==================== SEARCH AND QUERY OPERATIONS ====================

    /**
     * Finds all transactions in the system.
     * 
     * @return a list of all transactions
     */
    List<Transaction> findAll();
    
    /**
     * Finds all transactions for a specific user.
     * 
     * @param user the user whose transactions to find
     * @return a list of transactions for the user
     * @throws IllegalArgumentException if user is null
     */
    List<Transaction> findAllForUser(User user);
    
    /**
     * Finds transactions for a user with various filters.
     * 
     * @param user the user whose transactions to search
     * @param searchTerm the search term for description matching
     * @param startDate the start date for the search period
     * @param endDate the end date for the search period
     * @param category the category tag to filter by
     * @param includeSubcategories true to include subcategories, false otherwise
     * @return a list of matching transactions
     * @throws IllegalArgumentException if user is null
     */
    List<Transaction> findTransactions(User user, String searchTerm, LocalDate startDate, LocalDate endDate, Tag category, boolean includeSubcategories);
    
    /**
     * Finds transactions within a date range.
     * 
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return a list of transactions in the date range
     * @throws IllegalArgumentException if start or end date is null
     */
    List<Transaction> findByDateRange(LocalDate start, LocalDate end);
    
    /**
     * Finds transactions within a date range for a specific user.
     * 
     * @param user the user whose transactions to find
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return a list of transactions in the date range for the user
     * @throws IllegalArgumentException if any parameter is null
     */
    List<Transaction> findByDateRangeForUser(User user, LocalDate start, LocalDate end);
    
    /**
     * Finds transactions between two dates (inclusive).
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of transactions between the dates
     * @throws IllegalArgumentException if startDate or endDate is null
     */
    List<Transaction> findTransactionsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    /**
     * Finds transactions in a period with a limit on the number of results.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param limit the maximum number of transactions to return
     * @return a list of transactions in the period, limited to the specified count
     * @throws IllegalArgumentException if any parameter is null or limit is negative
     */
    List<Transaction> findTransactionsInPeriod(LocalDate startDate, LocalDate endDate, int limit);
    
    /**
     * Finds transactions in a period for a specific user with a limit on the number of results.
     * 
     * @param user the user whose transactions to find
     * @param startDate the start date
     * @param endDate the end date
     * @param limit the maximum number of transactions to return
     * @return a list of transactions in the period for the user, limited to the specified count
     * @throws IllegalArgumentException if any parameter is null or limit is negative
     */
    List<Transaction> findTransactionsInPeriodForUser(User user, LocalDate startDate, LocalDate endDate, int limit);

    // ==================== TAG-BASED OPERATIONS ====================

    /**
     * Finds transactions by tag ID.
     * 
     * @param tagId the tag ID to search for
     * @return a list of transactions with the specified tag
     * @throws IllegalArgumentException if tagId is null
     */
    List<Transaction> findByTag(Long tagId);
    
    /**
     * Finds transactions by tag within a date range.
     * 
     * @param tagId the tag ID to search for
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of transactions with the specified tag in the date range
     * @throws IllegalArgumentException if any parameter is null
     */
    List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Finds transactions by tag, optionally including child tags.
     * 
     * @param tag the tag to search for
     * @param includeChildren true to include transactions with child tags, false otherwise
     * @return a list of transactions with the specified tag
     * @throws IllegalArgumentException if tag is null
     */
    List<Transaction> findByTag(Tag tag, boolean includeChildren);
    
    /**
     * Finds transactions by multiple tags.
     * 
     * @param tags the collection of tags to search for
     * @param matchAll true if all tags must match, false if any tag can match
     * @return a list of transactions matching the tag criteria
     * @throws IllegalArgumentException if tags is null
     */
    List<Transaction> findByTags(Collection<Tag> tags, boolean matchAll);
    
    /**
     * Calculates the total amount for a tag in a specific period.
     * 
     * @param tag the tag to calculate totals for
     * @param startDate the start date
     * @param endDate the end date
     * @return the total amount (income minus expenses) for the tag in the period
     * @throws IllegalArgumentException if any parameter is null
     */
    BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate);

    // ==================== RELATED ENTITY OPERATIONS ====================

    /**
     * Finds transactions generated by a scheduled transaction.
     * 
     * @param scheduledTransactionId the scheduled transaction ID
     * @return a list of transactions generated by the scheduled transaction
     * @throws IllegalArgumentException if scheduledTransactionId is null
     */
    List<Transaction> findByScheduledTransaction(Long scheduledTransactionId);
    
    /**
     * Finds transactions related to a loan plan.
     * 
     * @param loanPlanId the loan plan ID
     * @return a list of transactions related to the loan plan
     * @throws IllegalArgumentException if loanPlanId is null
     */
    List<Transaction> findByLoanPlan(Long loanPlanId);

    // ==================== FINANCIAL CALCULATIONS ====================

    /**
     * Calculates the balance (income minus expenses) for a date range.
     * 
     * @param start the start date
     * @param end the end date
     * @return the balance for the period
     * @throws IllegalArgumentException if start or end date is null
     */
    BigDecimal calculateBalance(LocalDate start, LocalDate end);
    
    /**
     * Calculates the balance for a specific user in a date range.
     * 
     * @param user the user to calculate balance for
     * @param start the start date
     * @param end the end date
     * @return the balance for the user in the period
     * @throws IllegalArgumentException if any parameter is null
     */
    BigDecimal calculateBalanceForUser(User user, LocalDate start, LocalDate end);
    
    /**
     * Calculates the total income for a period.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return the total income for the period
     * @throws IllegalArgumentException if startDate or endDate is null
     */
    BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculates the total income for a specific user in a period.
     * 
     * @param user the user to calculate income for
     * @param startDate the start date
     * @param endDate the end date
     * @return the total income for the user in the period
     * @throws IllegalArgumentException if any parameter is null
     */
    BigDecimal calculateIncomeForPeriodForUser(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculates the total expenses for a period.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return the total expenses for the period
     * @throws IllegalArgumentException if startDate or endDate is null
     */
    BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculates the total expenses for a specific user in a period.
     * 
     * @param user the user to calculate expenses for
     * @param startDate the start date
     * @param endDate the end date
     * @return the total expenses for the user in the period
     * @throws IllegalArgumentException if any parameter is null
     */
    BigDecimal calculateExpensesForPeriodForUser(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculates the net worth as of a specific date.
     * 
     * @param asOfDate the date to calculate net worth for
     * @return the net worth (total income minus total expenses) up to the given date
     * @throws IllegalArgumentException if asOfDate is null
     */
    BigDecimal calculateNetWorth(LocalDate asOfDate);

    // ==================== STATISTICS AND ANALYTICS ====================

    /**
     * Calculates tag totals for a period.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param includeChildren true to include child tags, false otherwise
     * @return a map of tags to their total amounts
     * @throws IllegalArgumentException if startDate or endDate is null
     */
    Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate, boolean includeChildren);
    
    /**
     * Calculates tag statistics for a period.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param includeChildren true to include child tags, false otherwise
     * @return a map of tags to their transaction statistics
     * @throws IllegalArgumentException if startDate or endDate is null
     */
    Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate, boolean includeChildren);
}
