package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.model.TransactionStatistics;
import it.unicam.cs.mpgc.jbudget120002.model.TransactionStatisticsImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core implementation of the TransactionService interface that manages all financial transactions
 * in the Family Budget App. This class handles the creation, modification, and retrieval of
 * transactions, including recurring and scheduled transactions.
 *
 * Responsibilities:
 * - CRUD operations for transactions
 * - Transaction categorization and tagging
 * - Recurring transaction management
 * - Transaction search and filtering
 * - Financial calculations (totals, balances, etc.)
 *
 * Usage:
 * Used by controllers to manage all transaction-related operations and by other services
 * (like StatisticsService) to access transaction data for analysis and reporting.
 */
public class TransactionServiceImpl implements TransactionService {
    private final EntityManager entityManager;
    private final TagService tagService;

    public TransactionServiceImpl(EntityManager entityManager, TagService tagService) {
        this.entityManager = entityManager;
        this.tagService = tagService;
    }

    @Override
    public Transaction createTransaction(LocalDate date, String description, BigDecimal amount,
            boolean isIncome, Set<Long> tagIds) {
        entityManager.getTransaction().begin();
        try {
            Transaction transaction = new Transaction(date, description, amount, isIncome);
            
            // Add tags
            for (Long tagId : tagIds) {
                tagService.findById(tagId).ifPresent(transaction::addTag);
            }

            entityManager.persist(transaction);
            entityManager.getTransaction().commit();
            return transaction;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void deleteTransaction(Long id) {
        entityManager.getTransaction().begin();
        try {
            Transaction transaction = entityManager.find(Transaction.class, id);
            if (transaction != null) {
                entityManager.remove(transaction);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public List<Transaction> findAll() {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT t FROM Transaction t ORDER BY t.date DESC", Transaction.class);
        return query.getResultList();
    }

    @Override
    public Transaction findById(Long id) {
        return entityManager.find(Transaction.class, id);
    }

    @Override
    public List<Transaction> findByDateRange(LocalDate start, LocalDate end) {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT t FROM Transaction t WHERE t.date BETWEEN :start AND :end ORDER BY t.date DESC",
            Transaction.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public void updateTransaction(Long id, LocalDate date, String description,
            BigDecimal amount, boolean isIncome, Set<Long> tagIds) {
        entityManager.getTransaction().begin();
        try {
            Transaction transaction = findById(id);
            if (transaction != null) {
                transaction.setDate(date);
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setIncome(isIncome);

                // Update tags using setTags
                Set<Tag> newTags = new HashSet<>();
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(newTags::add);
                }
                transaction.setTags(newTags);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public BigDecimal calculateBalance(LocalDate start, LocalDate end) {
        List<Transaction> transactions = findByDateRange(start, end);
        return transactions.stream()
            .map(t -> {
                BigDecimal amount = t.isIncome() ? t.getAmount() : t.getAmount().negate();
                return amount;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> findByTag(Long tagId) {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT DISTINCT t FROM Transaction t JOIN t.tags tag WHERE tag.id = :tagId",
            Transaction.class);
        query.setParameter("tagId", tagId);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate) {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT DISTINCT t FROM Transaction t JOIN t.tags tag " +
            "WHERE tag.id = :tagId AND t.date BETWEEN :start AND :end",
            Transaction.class);
        query.setParameter("tagId", tagId);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByScheduledTransaction(Long scheduledTransactionId) {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT t FROM Transaction t WHERE t.scheduledTransaction.id = :scheduledId",
            Transaction.class);
        query.setParameter("scheduledId", scheduledTransactionId);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByLoanPlan(Long loanPlanId) {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT t FROM Transaction t WHERE t.loanPlan.id = :loanPlanId",
            Transaction.class);
        query.setParameter("loanPlanId", loanPlanId);
        return query.getResultList();
    }

    @Override
    public BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = findByDateRange(startDate, endDate);
        return transactions.stream()
            .filter(Transaction::isIncome)
            .map(t -> t.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = findByDateRange(startDate, endDate);
        return transactions.stream()
            .filter(t -> !t.isIncome())
            .map(t -> t.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> findByTag(Tag tag, boolean includeChildren) {
        if (tag == null) {
            return Collections.emptyList();
        }
        if (!includeChildren) {
            return findByTag(tag.getId());
        }
        Set<Tag> allTags = new HashSet<>();
        allTags.add(tag);
        if (includeChildren) {
            allTags.addAll(tagService.getAllDescendants(tag.getId()));
        }
        Set<Long> tagIds = allTags.stream()
            .filter(java.util.Objects::nonNull)
            .map(Tag::getId)
            .collect(Collectors.toSet());
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT DISTINCT t FROM Transaction t JOIN t.tags tag WHERE tag.id IN :tagIds",
            Transaction.class);
        query.setParameter("tagIds", tagIds);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByTags(Collection<Tag> tags, boolean matchAll) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> tagIds = tags.stream()
            .map(Tag::getId)
            .collect(Collectors.toSet());

        String jpql = matchAll ?
            "SELECT t FROM Transaction t WHERE " +
            "SIZE(t.tags) >= :tagCount AND " +
            "NOT EXISTS (SELECT tag FROM Tag tag WHERE tag.id IN :tagIds AND tag NOT MEMBER OF t.tags)" :
            "SELECT DISTINCT t FROM Transaction t JOIN t.tags tag WHERE tag.id IN :tagIds";

        TypedQuery<Transaction> query = entityManager.createQuery(jpql, Transaction.class);
        query.setParameter("tagIds", tagIds);
        if (matchAll) {
            query.setParameter("tagCount", (long) tagIds.size());
        }
        return query.getResultList();
    }

    @Override
    public Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate, 
            boolean includeChildren) {
        Map<Tag, BigDecimal> totals = new HashMap<>();
        List<Tag> tags = tagService.findAll();
        
        for (Tag tag : tags) {
            List<Transaction> transactions = findByTag(tag, includeChildren);
            BigDecimal total = transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (total.compareTo(BigDecimal.ZERO) != 0) {
                totals.put(tag, total);
            }
        }
        
        return totals;
    }

    @Override
    public List<Transaction> findTransactionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT t FROM Transaction t WHERE t.date BETWEEN :startDate AND :endDate ORDER BY t.date",
            Transaction.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    @Override
    public Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate, 
            boolean includeChildren) {
        Map<Tag, TransactionStatistics> statistics = new HashMap<>();
        List<Tag> tags = tagService.findAll();
        
        for (Tag tag : tags) {
            TransactionStatisticsImpl stats = new TransactionStatisticsImpl();
            List<Transaction> transactions = findByTag(tag, includeChildren);
            
            transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .forEach(t -> {
                    stats.addTransaction(t);
                });
                
            statistics.put(tag, stats);
        }
        
        return statistics;
    }

    @Override
    public BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = findByTag(tag, true);
        return transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .map(t -> t.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateNetWorth(LocalDate asOfDate) {
        List<Transaction> transactions = findByDateRange(LocalDate.MIN, asOfDate);
        return transactions.stream()
            .map(t -> t.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> findTransactionsInPeriod(LocalDate startDate, LocalDate endDate, int limit) {
        TypedQuery<Transaction> query = entityManager.createQuery(
            "SELECT t FROM Transaction t WHERE t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC",
            Transaction.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
