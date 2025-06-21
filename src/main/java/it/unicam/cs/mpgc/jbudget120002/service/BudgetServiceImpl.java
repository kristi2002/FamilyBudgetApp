package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.repository.BudgetRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class BudgetServiceImpl extends BaseService implements BudgetService {
    private final BudgetRepository repository;
    private final TransactionService transactionService;
    private final TagService tagService;

    public BudgetServiceImpl(EntityManager entityManager, BudgetRepository repository, TransactionService transactionService, TagService tagService) {
        super(entityManager);
        this.repository = repository;
        this.transactionService = transactionService;
        this.tagService = tagService;
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Budget> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(Budget budget) {
        executeInTransaction(() -> repository.save(budget));
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(() -> repository.deleteById(id));
    }

    @Override
    public List<Budget> findAllByUser(User user) {
        if (user == null || user.getGroups().isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByGroups(user.getGroups());
    }

    @Override
    public List<Budget> findByDateRange(LocalDate start, LocalDate end) {
        return repository.findByDateRange(start, end);
    }

    @Override
    public List<Budget> findByCategory(Long categoryId) {
        return repository.findByCategory(categoryId);
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
    public BudgetComparison getBudgetComparison(LocalDate start, LocalDate end) {
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
        Budget budget = findById(budgetId).orElse(null);
        if (budget == null) {
            return BigDecimal.ZERO;
        }
        return calculateActualAmount(budget, budget.getStartDate(), budget.getEndDate());
    }

    @Override
    public Map<LocalDate, BigDecimal> getBudgetForecast(LocalDate startDate, int months) {
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