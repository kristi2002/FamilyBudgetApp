package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Budget;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BudgetServiceImpl implements BudgetService {
    private final EntityManager entityManager;
    private final TagService tagService;
    private final TransactionService transactionService;

    public BudgetServiceImpl(EntityManager entityManager,
                           TagService tagService,
                           TransactionService transactionService) {
        this.entityManager = entityManager;
        this.tagService = tagService;
        this.transactionService = transactionService;
    }

    @Override
    public Budget createBudget(String name, BigDecimal amount, LocalDate startDate,
                             LocalDate endDate, Set<Long> tagIds) {
        entityManager.getTransaction().begin();
        try {
            Budget budget = new Budget(name, amount, startDate, endDate);
            
            // Add tags
            for (Long tagId : tagIds) {
                tagService.findById(tagId).ifPresent(budget::addTag);
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
            "SELECT b FROM Budget b ORDER BY b.startDate DESC", Budget.class);
        return query.getResultList();
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Budget.class, id));
    }

    @Override
    public List<Budget> findByDateRange(LocalDate start, LocalDate end) {
        TypedQuery<Budget> query = entityManager.createQuery(
            "SELECT b FROM Budget b WHERE " +
            "(b.startDate BETWEEN :start AND :end) OR " +
            "(b.endDate BETWEEN :start AND :end) OR " +
            "(b.startDate <= :start AND b.endDate >= :end) " +
            "ORDER BY b.startDate DESC", Budget.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public void updateBudget(Long id, String name, BigDecimal amount,
                           LocalDate startDate, LocalDate endDate, Set<Long> tagIds) {
        entityManager.getTransaction().begin();
        try {
            Budget budget = entityManager.find(Budget.class, id);
            if (budget != null) {
                budget.setName(name);
                budget.setAmount(amount);
                budget.setStartDate(startDate);
                budget.setEndDate(endDate);

                // Update tags
                budget.getTags().clear();
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(budget::addTag);
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public BigDecimal calculateSpentAmount(Long budgetId) {
        Budget budget = entityManager.find(Budget.class, budgetId);
        if (budget != null) {
            Set<Long> tagIds = budget.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());
            
            BigDecimal total = BigDecimal.ZERO;
            for (Long tagId : tagIds) {
                List<Transaction> transactions = transactionService.findByTagAndDateRange(
                    tagId, budget.getStartDate(), budget.getEndDate());
                
                total = total.add(transactions.stream()
                    .filter(t -> !t.isIncome())  // Only include expenses (non-income transactions)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            }
            return total;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateRemainingAmount(Long budgetId) {
        Budget budget = entityManager.find(Budget.class, budgetId);
        if (budget != null) {
            return budget.getAmount().subtract(calculateSpentAmount(budgetId));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Budget> findActiveBudgets(LocalDate asOfDate) {
        TypedQuery<Budget> query = entityManager.createQuery(
            "SELECT b FROM Budget b WHERE " +
            "b.startDate <= :date AND b.endDate >= :date " +
            "ORDER BY b.startDate DESC", Budget.class);
        query.setParameter("date", asOfDate);
        return query.getResultList();
    }

    @Override
    public void addTagToBudget(Long budgetId, Long tagId) {
        entityManager.getTransaction().begin();
        try {
            Budget budget = entityManager.find(Budget.class, budgetId);
            if (budget != null) {
                tagService.findById(tagId).ifPresent(budget::addTag);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void removeTagFromBudget(Long budgetId, Long tagId) {
        entityManager.getTransaction().begin();
        try {
            Budget budget = entityManager.find(Budget.class, budgetId);
            if (budget != null) {
                tagService.findById(tagId).ifPresent(budget::removeTag);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
} 