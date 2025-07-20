package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.repository.BudgetRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of the BudgetService interface for managing budget plans and financial planning.
 * 
 * <p>This class provides comprehensive budget management functionality including
 * CRUD operations, budget status tracking, financial comparisons, and forecasting.
 * It integrates with the repository layer, transaction service, and tag service to
 * provide a complete budget management solution.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Create, read, update, and delete budget plans</li>
 *   <li>Track budget utilization and status</li>
 *   <li>Calculate budget comparisons and forecasts</li>
 *   <li>Monitor spending against budget limits</li>
 *   <li>Support user-specific and group-based budgets</li>
 *   <li>Provide budget recommendations and alerts</li>
 *   <li>Generate budget reports and analytics</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a new budget
 * Budget budget = new Budget("Monthly Groceries", new BigDecimal("500.00"), 
 *                           startDate, endDate);
 * budgetService.save(budget);
 * 
 * // Check budget status
 * Map<Long, BudgetStatus> status = budgetService.calculateBudgetStatus(startDate, endDate);
 * 
 * // Get budget comparison
 * BudgetComparison comparison = budgetService.getBudgetComparison(startDate, endDate);
 * 
 * // Calculate forecast
 * Map<LocalDate, BigDecimal> forecast = budgetService.getBudgetForecast(startDate, 6);
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public class BudgetServiceImpl extends BaseService implements BudgetService {
    
    /** The budget repository for data access */
    private final BudgetRepository repository;
    
    /** The transaction service for financial calculations */
    private final TransactionService transactionService;
    
    /** The tag service for category management */
    private final TagService tagService;

    // ==================== CONSTRUCTORS ====================

    /**
     * Creates a new BudgetServiceImpl with the required dependencies.
     * 
     * @param entityManager the EntityManager for database operations
     * @param repository the budget repository
     * @param transactionService the transaction service
     * @param tagService the tag service
     * @throws IllegalArgumentException if any parameter is null
     */
    public BudgetServiceImpl(EntityManager entityManager, BudgetRepository repository, 
                           TransactionService transactionService, TagService tagService) {
        super(entityManager);
        if (repository == null) {
            throw new IllegalArgumentException("BudgetRepository cannot be null");
        }
        if (transactionService == null) {
            throw new IllegalArgumentException("TransactionService cannot be null");
        }
        if (tagService == null) {
            throw new IllegalArgumentException("TagService cannot be null");
        }
        this.repository = repository;
        this.transactionService = transactionService;
        this.tagService = tagService;
    }

    // ==================== CRUD OPERATIONS ====================

    @Override
    public Optional<Budget> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    @Override
    public List<Budget> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        executeInTransaction(() -> repository.save(budget));
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Budget ID cannot be null");
        }
        executeInTransaction(() -> repository.deleteById(id));
    }

    @Override
    public List<Budget> findAllByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        // Return budgets for user's groups, or all budgets if user has no groups
        if (user.getGroups().isEmpty()) {
            return repository.findAll(); // Return all budgets if user has no groups
        }
        return repository.findByGroups(user.getGroups());
    }

    // ==================== SEARCH AND QUERY OPERATIONS ====================

    @Override
    public List<Budget> findByDateRange(LocalDate start, LocalDate end) {
        validateDateRange(start, end);
        return repository.findByDateRange(start, end);
    }

    @Override
    public List<Budget> findByCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        return repository.findByCategory(categoryId);
    }

    // ==================== BUDGET STATUS AND MONITORING ====================

    @Override
    public Map<Long, BudgetStatus> calculateBudgetStatus(LocalDate start, LocalDate end) {
        validateDateRange(start, end);
        
        Map<Long, BudgetStatus> statusMap = new HashMap<>();
        List<Budget> budgets = findByDateRange(start, end);
        
        for (Budget budget : budgets) {
            BigDecimal actualAmount = calculateActualAmount(budget, start, end);
            statusMap.put(budget.getId(), new BudgetStatus(
                budget.getId(),
                budget.getName(),
                budget.getAmount(),
                actualAmount,
                budget.getStartDate(),
                budget.getEndDate()
            ));
        }
        
        return statusMap;
    }

    @Override
    public BudgetComparison getBudgetComparison(LocalDate start, LocalDate end) {
        validateDateRange(start, end);
        
        Map<Long, BudgetComparison.CategoryComparison> comparisons = new HashMap<>();
        List<Tag> categories = tagService.findAll();
        
        for (Tag category : categories) {
            List<Budget> categoryBudgets = findByCategory(category.getId());
            BigDecimal budgetedAmount = categoryBudgets.stream()
                .filter(b -> !b.getStartDate().isAfter(end) && !b.getEndDate().isBefore(start))
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            List<Transaction> transactions = transactionService.findByTag(category.getId());
            BigDecimal actualAmount = transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            comparisons.put(category.getId(), new BudgetComparison.CategoryComparison(
                category.getId(),
                category.getName(),
                budgetedAmount,
                actualAmount
            ));
        }
        
        return new BudgetComparison(start, end, comparisons);
    }

    @Override
    public BigDecimal calculateSpentAmount(Long budgetId) {
        if (budgetId == null) {
            throw new IllegalArgumentException("Budget ID cannot be null");
        }
        
        Budget budget = findById(budgetId).orElse(null);
        if (budget == null) {
            return BigDecimal.ZERO;
        }
        return calculateActualAmount(budget, budget.getStartDate(), budget.getEndDate());
    }

    // ==================== FORECASTING AND PLANNING ====================

    @Override
    public Map<LocalDate, BigDecimal> getBudgetForecast(LocalDate startDate, int months) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (months < 0) {
            throw new IllegalArgumentException("Months cannot be negative");
        }
        
        Map<LocalDate, BigDecimal> forecast = new TreeMap<>();
        LocalDate endDate = startDate.plusMonths(months);
        
        Map<Long, BigDecimal> categoryAverages = calculateCategoryAverages(startDate.minusMonths(3), startDate);
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            BigDecimal monthlyTotal = categoryAverages.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            forecast.put(currentDate, monthlyTotal);
            currentDate = currentDate.plusMonths(1);
        }
        
        return forecast;
    }

    // ==================== PRIVATE HELPER METHODS ====================

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
     * Calculates the actual amount spent for a budget within a date range.
     * 
     * @param budget the budget to calculate for
     * @param start the start date
     * @param end the end date
     * @return the actual amount spent
     */
    private BigDecimal calculateActualAmount(Budget budget, LocalDate start, LocalDate end) {
        Set<Long> tagIds = new HashSet<>();
        for (Tag tag : budget.getTags()) {
            tagIds.add(tag.getId());
            tagService.findTagAndDescendants(tag).forEach(child -> tagIds.add(child.getId()));
        }
        return tagIds.stream()
            .flatMap(tagId -> transactionService.findByTag(tagId).stream())
            .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
            .filter(t -> !t.isIncome())
            .map(t -> t.getAmount().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates average spending by category for a date range.
     * 
     * @param start the start date
     * @param end the end date
     * @return a map of category ID to average spending
     */
    private Map<Long, BigDecimal> calculateCategoryAverages(LocalDate start, LocalDate end) {
        Map<Long, BigDecimal> averages = new HashMap<>();
        List<Tag> categories = tagService.findAll();
        
        for (Tag category : categories) {
            List<Transaction> transactions = transactionService.findByTag(category.getId());
            BigDecimal total = transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(start, end);
            if (monthsBetween > 0) {
                averages.put(category.getId(), total.divide(new BigDecimal(monthsBetween), 2, BigDecimal.ROUND_HALF_UP));
            }
        }
        
        return averages;
    }
} 