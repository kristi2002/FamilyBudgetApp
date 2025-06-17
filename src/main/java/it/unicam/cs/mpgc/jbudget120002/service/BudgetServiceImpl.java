package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the BudgetService interface that manages budget planning and tracking
 * in the Family Budget App. This class handles budget creation, monitoring, and alerts
 * for different spending categories.
 *
 * Responsibilities:
 * - Budget creation and management
 * - Budget tracking and utilization monitoring
 * - Budget alerts and notifications
 * - Category-based budget allocation
 * - Budget recommendations and adjustments
 *
 * Usage:
 * Used by controllers to manage budget-related operations and by the StatisticsService
 * to provide budget utilization insights and recommendations.
 */
public class BudgetServiceImpl implements BudgetService {
    private final EntityManager entityManager;
    private final TransactionService transactionService;
    private final TagService tagService;

    public BudgetServiceImpl(EntityManager entityManager, TransactionService transactionService,
                           TagService tagService) {
        this.entityManager = entityManager;
        this.transactionService = transactionService;
        this.tagService = tagService;
    }

    @Override
    public Budget createBudget(String name, LocalDate startDate, LocalDate endDate,
                             BigDecimal amount, Long categoryId) {
        entityManager.getTransaction().begin();
        try {
            Budget budget = new Budget(name, amount, startDate, endDate);
            if (categoryId != null) {
                tagService.findById(categoryId).ifPresent(budget::addTag);
            }
            entityManager.persist(budget);
            entityManager.getTransaction().commit();
            return budget;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void updateBudget(Long id, String name, LocalDate startDate, LocalDate endDate,
                           BigDecimal amount, Long categoryId) {
        entityManager.getTransaction().begin();
        try {
            Budget budget = entityManager.find(Budget.class, id);
            if (budget != null) {
                budget.setName(name);
                budget.setAmount(amount);
                budget.setStartDate(startDate);
                budget.setEndDate(endDate);
                
                // Update category
                budget.getTags().clear();
                if (categoryId != null) {
                    tagService.findById(categoryId).ifPresent(budget::addTag);
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void deleteBudget(Long id) {
        entityManager.getTransaction().begin();
        try {
            Budget budget = entityManager.find(Budget.class, id);
            if (budget != null) {
                entityManager.remove(budget);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public List<Budget> findAll() {
        TypedQuery<Budget> query = entityManager.createQuery(
            "SELECT b FROM Budget b", Budget.class);
        return query.getResultList();
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Budget.class, id));
    }

    @Override
    public List<Budget> findByDateRange(LocalDate start, LocalDate end) {
        TypedQuery<Budget> query = entityManager.createQuery(
            "SELECT b FROM Budget b WHERE b.startDate <= :end AND b.endDate >= :start",
            Budget.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public List<Budget> findByCategory(Long categoryId) {
        TypedQuery<Budget> query = entityManager.createQuery(
            "SELECT DISTINCT b FROM Budget b JOIN b.tags t WHERE t.id = :categoryId",
            Budget.class);
        query.setParameter("categoryId", categoryId);
        return query.getResultList();
    }

    @Override
    public Map<Long, BudgetStatus> calculateBudgetStatus(LocalDate start, LocalDate end) {
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
    public List<BudgetAlert> getBudgetAlerts() {
        List<BudgetAlert> alerts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        // Check all budgets that are active today
        List<Budget> activeBudgets = findByDateRange(now, now);
        for (Budget budget : activeBudgets) {
            // Calculate the actual amount spent for this budget up to now
            BigDecimal actualAmount = calculateActualAmount(budget, budget.getStartDate(), now);
            
            // If actual spending exceeds the budgeted amount, trigger an OVER_BUDGET alert
            if (actualAmount.compareTo(budget.getAmount()) > 0) {
                alerts.add(new BudgetAlert(
                    budget.getId(),
                    budget.getName(),
                    budget.getAmount(),
                    actualAmount,
                    budget.getAmount(),
                    now,
                    BudgetAlert.AlertType.OVER_BUDGET,
                    BudgetAlert.AlertSeverity.CRITICAL
                ));
            }
            
            // If actual spending is at least 80% of the budget, trigger an APPROACHING_LIMIT alert
            BigDecimal threshold = budget.getAmount().multiply(new BigDecimal("0.8"));
            if (actualAmount.compareTo(threshold) >= 0 && 
                actualAmount.compareTo(budget.getAmount()) < 0) {
                alerts.add(new BudgetAlert(
                    budget.getId(),
                    budget.getName(),
                    budget.getAmount(),
                    actualAmount,
                    threshold,
                    now,
                    BudgetAlert.AlertType.APPROACHING_LIMIT,
                    BudgetAlert.AlertSeverity.WARNING
                ));
            }
            
            // If the budget is expiring within 7 days, trigger a BUDGET_EXPIRING alert
            if (budget.getEndDate().minusDays(7).isBefore(now)) {
                alerts.add(new BudgetAlert(
                    budget.getId(),
                    budget.getName(),
                    budget.getAmount(),
                    actualAmount,
                    null,
                    budget.getEndDate(),
                    BudgetAlert.AlertType.BUDGET_EXPIRING,
                    BudgetAlert.AlertSeverity.INFO
                ));
            }
        }
        return alerts;
    }

    @Override
    public BudgetTemplate createTemplate(String name, Map<Long, BigDecimal> categoryAmounts) {
        entityManager.getTransaction().begin();
        try {
            BudgetTemplate template = new BudgetTemplate(name, categoryAmounts);
            entityManager.persist(template);
            entityManager.getTransaction().commit();
            return template;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public List<Budget> applyTemplate(Long templateId, LocalDate startDate, LocalDate endDate) {
        entityManager.getTransaction().begin();
        try {
            BudgetTemplate template = entityManager.find(BudgetTemplate.class, templateId);
            if (template == null) {
                throw new IllegalArgumentException("Template not found");
            }
            
            List<Budget> createdBudgets = new ArrayList<>();
            for (Map.Entry<Long, BigDecimal> entry : template.getCategoryAmounts().entrySet()) {
                Budget budget = createBudget(
                    template.getName() + " - " + tagService.findById(entry.getKey())
                        .map(Tag::getName)
                        .orElse("Unknown Category"),
                    startDate,
                    endDate,
                    entry.getValue(),
                    entry.getKey()
                );
                createdBudgets.add(budget);
            }
            
            entityManager.getTransaction().commit();
            return createdBudgets;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Map<LocalDate, BigDecimal> getBudgetForecast(LocalDate startDate, int months) {
        Map<LocalDate, BigDecimal> forecast = new TreeMap<>();
        LocalDate endDate = startDate.plusMonths(months);
        
        // Get historical spending patterns
        Map<Long, BigDecimal> categoryAverages = calculateCategoryAverages(startDate.minusMonths(3), startDate);
        
        // Project future spending
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            BigDecimal monthlyTotal = categoryAverages.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            forecast.put(currentDate, monthlyTotal);
            currentDate = currentDate.plusMonths(1);
        }
        
        return forecast;
    }

    @Override
    public BudgetComparison getBudgetComparison(LocalDate start, LocalDate end) {
        Map<Long, BudgetComparison.CategoryComparison> comparisons = new HashMap<>();
        
        // Get all categories (tags) in the system
        List<Tag> categories = tagService.findAll();
        
        for (Tag category : categories) {
            // Calculate the total budgeted amount for this category in the given period
            List<Budget> categoryBudgets = findByCategory(category.getId());
            BigDecimal budgetedAmount = categoryBudgets.stream()
                .filter(b -> !b.getStartDate().isAfter(end) && !b.getEndDate().isBefore(start))
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate the actual amount spent for this category in the given period
            List<Transaction> transactions = transactionService.findByTag(category.getId());
            BigDecimal actualAmount = transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Store the comparison for this category
            comparisons.put(category.getId(), new BudgetComparison.CategoryComparison(
                category.getId(),
                category.getName(),
                budgetedAmount,
                actualAmount
            ));
        }
        
        // Return the overall budget comparison for the period
        return new BudgetComparison(start, end, comparisons);
    }

    /**
     * Calculates the actual amount spent for a budget between two dates.
     * This sums all transactions for the budget's tags in the given period.
     * Income transactions are counted as positive, expenses as negative.
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
            .filter(t -> !t.isIncome()) // Only expenses
            .map(t -> t.getAmount().abs()) // Use absolute value for expenses
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Long, BigDecimal> calculateCategoryAverages(LocalDate start, LocalDate end) {
        Map<Long, BigDecimal> averages = new HashMap<>();
        List<Tag> categories = tagService.findAll();
        
        for (Tag category : categories) {
            List<Transaction> transactions = transactionService.findByTag(category.getId());
            BigDecimal total = transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long months = java.time.temporal.ChronoUnit.MONTHS.between(start, end);
            if (months > 0) {
                averages.put(category.getId(), total.divide(new BigDecimal(months), 2, BigDecimal.ROUND_HALF_UP));
            }
        }
        
        return averages;
    }

    @Override
    public BigDecimal calculateSpentAmount(Long budgetId) {
        Budget budget = entityManager.find(Budget.class, budgetId);
        if (budget == null) {
            return BigDecimal.ZERO;
        }
        return calculateActualAmount(budget, budget.getStartDate(), budget.getEndDate());
    }

    @Override
    public BigDecimal calculateRemainingAmount(Long budgetId) {
        Budget budget = entityManager.find(Budget.class, budgetId);
        if (budget == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal spent = calculateSpentAmount(budgetId);
        return budget.getAmount().subtract(spent);
    }
} 