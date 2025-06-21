package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the TransactionService interface for managing financial transactions.
 * 
 * <p>This class provides comprehensive transaction management functionality including
 * CRUD operations, search capabilities, financial calculations, and user-specific
 * transaction handling. It integrates with the repository layer and tag service to
 * provide a complete transaction management solution.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Create, read, update, and delete transactions</li>
 *   <li>Search and filter transactions by various criteria</li>
 *   <li>Calculate financial metrics and statistics</li>
 *   <li>Support tag-based categorization and filtering</li>
 *   <li>Handle user-specific transaction management</li>
 *   <li>Provide date range and period-based operations</li>
 *   <li>Manage transaction relationships with other entities</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a new transaction
 * Transaction transaction = transactionService.createTransaction(
 *     user, LocalDate.now(), "Groceries", new BigDecimal("50.00"), false, tagIds);
 * 
 * // Find transactions for a user with filters
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
public class TransactionServiceImpl extends BaseService implements TransactionService {
    
    /** The transaction repository for data access */
    private final TransactionRepository repository;
    
    /** The tag service for tag-related operations */
    private final TagService tagService;

    // ==================== CONSTRUCTORS ====================

    /**
     * Creates a new TransactionServiceImpl with the required dependencies.
     * 
     * @param entityManager the EntityManager for database operations
     * @param repository the transaction repository
     * @param tagService the tag service
     * @throws IllegalArgumentException if any parameter is null
     */
    public TransactionServiceImpl(EntityManager entityManager, TransactionRepository repository, TagService tagService) {
        super(entityManager);
        if (repository == null) {
            throw new IllegalArgumentException("TransactionRepository cannot be null");
        }
        if (tagService == null) {
            throw new IllegalArgumentException("TagService cannot be null");
        }
        this.repository = repository;
        this.tagService = tagService;
    }

    // ==================== CRUD OPERATIONS ====================

    @Override
    public Transaction createTransaction(LocalDate date, String description, BigDecimal amount,
                                         boolean isIncome, Set<Long> tagIds) {
        validateCreateTransactionParams(date, description, amount, tagIds);
        
        beginTransaction();
        try {
            Transaction transaction = new Transaction(date, description, amount, isIncome);
            associateTagsWithTransaction(transaction, tagIds);
            repository.save(transaction);
            commitTransaction();
            return transaction;
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public Transaction createTransaction(User user, LocalDate date, String description, BigDecimal amount,
                                         boolean isIncome, Set<Long> tagIds) {
        validateCreateTransactionParams(user, date, description, amount, tagIds);
        
        beginTransaction();
        try {
            Transaction transaction = new Transaction(date, description, amount, isIncome);
            transaction.setUser(user);
            associateTagsWithTransaction(transaction, tagIds);
            repository.save(transaction);
            commitTransaction();
            return transaction;
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public void updateTransaction(Long id, LocalDate date, String description,
                                  BigDecimal amount, boolean isIncome, Set<Long> tagIds) {
        validateUpdateTransactionParams(id, date, description, amount, tagIds);
        
        beginTransaction();
        try {
            Transaction transaction = findById(id);
            if (transaction == null) {
                throw new RuntimeException("Transaction not found with ID: " + id);
            }
            
            transaction.setDate(date);
            transaction.setDescription(description);
            transaction.setAmount(amount);
            transaction.setIncome(isIncome);
            
            Set<Tag> newTags = new HashSet<>();
            if (tagIds != null) {
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(newTags::add);
                }
            }
            transaction.setTags(newTags);
            
            repository.save(transaction);
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public void deleteTransaction(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }
        
        beginTransaction();
        try {
            Transaction transaction = findById(id);
            if (transaction == null) {
                throw new RuntimeException("Transaction not found with ID: " + id);
            }
            repository.deleteById(id);
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public Transaction findById(Long id) {
        if (id == null) {
            return null;
        }
        return repository.findById(id).orElse(null);
    }

    // ==================== SEARCH AND QUERY OPERATIONS ====================

    @Override
    public List<Transaction> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Transaction> findAllForUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return repository.findAllForUser(user);
    }

    @Override
    public List<Transaction> findTransactions(User user, String searchTerm, LocalDate startDate, LocalDate endDate, Tag category, boolean includeSubcategories) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        List<Long> groupIds = user.getGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toList());
        
        List<Long> tagIds = null;
        if (category != null) {
            tagIds = new ArrayList<>();
            if (includeSubcategories) {
                tagIds.addAll(tagService.getAllDescendants(category.getId()).stream()
                                     .map(Tag::getId)
                                     .collect(Collectors.toList()));
            }
            tagIds.add(category.getId());
        }
        
        return repository.findWithFilters(user, groupIds, searchTerm, startDate, endDate, tagIds);
    }

    @Override
    public List<Transaction> findByDateRange(LocalDate start, LocalDate end) {
        validateDateRange(start, end);
        return repository.findByDateBetween(start, end);
    }

    @Override
    public List<Transaction> findByDateRangeForUser(User user, LocalDate start, LocalDate end) {
        validateDateRangeForUser(user, start, end);
        return repository.findByDateBetweenForUser(user, start, end);
    }

    @Override
    public List<Transaction> findTransactionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return repository.findByDateBetween(startDate, endDate);
    }

    @Override
    public List<Transaction> findTransactionsInPeriod(LocalDate startDate, LocalDate endDate, int limit) {
        validateDateRangeAndLimit(startDate, endDate, limit);
        return findByDateRange(startDate, endDate).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findTransactionsInPeriodForUser(User user, LocalDate startDate, LocalDate endDate, int limit) {
        validateDateRangeAndLimitForUser(user, startDate, endDate, limit);
        return findByDateRangeForUser(user, startDate, endDate).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== TAG-BASED OPERATIONS ====================

    @Override
    public List<Transaction> findByTag(Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("Tag ID cannot be null");
        }
        return repository.findByTagId(tagId);
    }

    @Override
    public List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate) {
        if (tagId == null) {
            throw new IllegalArgumentException("Tag ID cannot be null");
        }
        validateDateRange(startDate, endDate);
        return repository.findByTagIdAndDateRange(tagId, startDate, endDate);
    }

    @Override
    public List<Transaction> findByTag(Tag tag, boolean includeChildren) {
        if (tag == null) {
            return Collections.emptyList();
        }
        
        if (!includeChildren) {
            return findByTag(tag.getId());
        }
        
        Set<Tag> allTags = new HashSet<>(tagService.getAllDescendants(tag.getId()));
        allTags.add(tag);
        Set<Long> tagIds = allTags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());
        
        return repository.findByTags(tagIds, false, tagIds.size());
    }

    @Override
    public List<Transaction> findByTags(Collection<Tag> tags, boolean matchAll) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<Long> tagIds = tags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());
        
        return repository.findByTags(tagIds, matchAll, tagIds.size());
    }

    @Override
    public BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate) {
        if (tag == null) {
            return BigDecimal.ZERO;
        }
        validateDateRange(startDate, endDate);
        
        return findByTagAndDateRange(tag.getId(), startDate, endDate).stream()
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== RELATED ENTITY OPERATIONS ====================

    @Override
    public List<Transaction> findByScheduledTransaction(Long scheduledTransactionId) {
        if (scheduledTransactionId == null) {
            throw new IllegalArgumentException("Scheduled transaction ID cannot be null");
        }
        return repository.findByScheduledTransaction(scheduledTransactionId);
    }

    @Override
    public List<Transaction> findByLoanPlan(Long loanPlanId) {
        if (loanPlanId == null) {
            throw new IllegalArgumentException("Loan plan ID cannot be null");
        }
        return repository.findByLoanPlan(loanPlanId);
    }

    // ==================== FINANCIAL CALCULATIONS ====================

    @Override
    public BigDecimal calculateBalance(LocalDate start, LocalDate end) {
        validateDateRange(start, end);
        List<Transaction> transactions = findByDateRange(start, end);
        return calculateBalanceFromTransactions(transactions);
    }

    @Override
    public BigDecimal calculateBalanceForUser(User user, LocalDate start, LocalDate end) {
        validateDateRangeForUser(user, start, end);
        List<Transaction> transactions = findByDateRangeForUser(user, start, end);
        return calculateBalanceFromTransactions(transactions);
    }

    @Override
    public BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return findByDateRange(startDate, endDate).stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateIncomeForPeriodForUser(User user, LocalDate startDate, LocalDate endDate) {
        validateDateRangeForUser(user, startDate, endDate);
        return findByDateRangeForUser(user, startDate, endDate).stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return findByDateRange(startDate, endDate).stream()
                .filter(t -> !t.isIncome())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateExpensesForPeriodForUser(User user, LocalDate startDate, LocalDate endDate) {
        validateDateRangeForUser(user, startDate, endDate);
        return findByDateRangeForUser(user, startDate, endDate).stream()
                .filter(t -> !t.isIncome())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateNetWorth(LocalDate asOfDate) {
        if (asOfDate == null) {
            throw new IllegalArgumentException("As of date cannot be null");
        }
        
        return repository.findAll().stream()
                .filter(t -> !t.getDate().isAfter(asOfDate))
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== STATISTICS AND ANALYTICS ====================

    @Override
    public Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate, boolean includeChildren) {
        // This logic is complex and might be better suited for a dedicated statistics service
        // For now, returning an empty map as placeholder
        return new HashMap<>();
    }

    @Override
    public Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate, boolean includeChildren) {
        // This logic is complex and might be better suited for a dedicated statistics service
        // For now, returning an empty map as placeholder
        return new HashMap<>();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates parameters for creating a transaction.
     * 
     * @param date the transaction date
     * @param description the transaction description
     * @param amount the transaction amount
     * @param tagIds the tag IDs
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateCreateTransactionParams(LocalDate date, String description, BigDecimal amount, Set<Long> tagIds) {
        if (date == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction description cannot be null or empty");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    /**
     * Validates parameters for creating a transaction with user.
     * 
     * @param user the user
     * @param date the transaction date
     * @param description the transaction description
     * @param amount the transaction amount
     * @param tagIds the tag IDs
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateCreateTransactionParams(User user, LocalDate date, String description, BigDecimal amount, Set<Long> tagIds) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        validateCreateTransactionParams(date, description, amount, tagIds);
    }

    /**
     * Validates parameters for updating a transaction.
     * 
     * @param id the transaction ID
     * @param date the transaction date
     * @param description the transaction description
     * @param amount the transaction amount
     * @param tagIds the tag IDs
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateUpdateTransactionParams(Long id, LocalDate date, String description, BigDecimal amount, Set<Long> tagIds) {
        if (id == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }
        validateCreateTransactionParams(date, description, amount, tagIds);
    }

    /**
     * Validates date range parameters.
     * 
     * @param start the start date
     * @param end the end date
     * @throws IllegalArgumentException if dates are invalid
     */
    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (end == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    /**
     * Validates date range parameters for user operations.
     * 
     * @param user the user
     * @param start the start date
     * @param end the end date
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateDateRangeForUser(User user, LocalDate start, LocalDate end) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        validateDateRange(start, end);
    }

    /**
     * Validates date range and limit parameters.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param limit the limit
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateDateRangeAndLimit(LocalDate startDate, LocalDate endDate, int limit) {
        validateDateRange(startDate, endDate);
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
    }

    /**
     * Validates date range and limit parameters for user operations.
     * 
     * @param user the user
     * @param startDate the start date
     * @param endDate the end date
     * @param limit the limit
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateDateRangeAndLimitForUser(User user, LocalDate startDate, LocalDate endDate, int limit) {
        validateDateRangeForUser(user, startDate, endDate);
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
    }

    /**
     * Associates tags with a transaction.
     * 
     * @param transaction the transaction
     * @param tagIds the tag IDs
     */
    private void associateTagsWithTransaction(Transaction transaction, Set<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                tagService.findById(tagId).ifPresent(transaction::addTag);
            }
        }
    }

    /**
     * Calculates balance from a list of transactions.
     * 
     * @param transactions the list of transactions
     * @return the calculated balance
     */
    private BigDecimal calculateBalanceFromTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
