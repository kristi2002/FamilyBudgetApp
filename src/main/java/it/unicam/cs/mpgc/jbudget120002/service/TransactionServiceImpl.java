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

public class TransactionServiceImpl implements TransactionService {
    private final EntityManager entityManager;
    private final TagService tagService;
    private final ExchangeRateService exchangeRateService;

    public TransactionServiceImpl(EntityManager entityManager, TagService tagService, ExchangeRateService exchangeRateService) {
        this.entityManager = entityManager;
        this.tagService = tagService;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public Transaction createTransaction(LocalDate date, String description, BigDecimal amount,
            boolean isIncome, Set<Long> tagIds, String currency) {
        entityManager.getTransaction().begin();
        try {
            Transaction transaction = new Transaction(date, description, amount, isIncome, currency);
            
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
            BigDecimal amount, boolean isIncome, Set<Long> tagIds, String currency) {
        entityManager.getTransaction().begin();
        try {
            Transaction transaction = findById(id);
            if (transaction != null) {
                transaction.setDate(date);
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setIncome(isIncome);
                transaction.setCurrency(currency);

                // Update tags
                transaction.getTags().clear();
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(transaction::addTag);
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public BigDecimal calculateBalance(LocalDate start, LocalDate end, String targetCurrency) {
        List<Transaction> transactions = findByDateRange(start, end);
        return transactions.stream()
            .map(t -> {
                BigDecimal amount = t.isIncome() ? t.getAmount() : t.getAmount().negate();
                if (!t.getCurrency().equals(targetCurrency)) {
                    return exchangeRateService.convertAmount(amount, t.getCurrency(), targetCurrency);
                }
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
    public BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate, String targetCurrency) {
        List<Transaction> transactions = findByDateRange(startDate, endDate);
        return transactions.stream()
            .filter(Transaction::isIncome)
            .map(t -> {
                if (!t.getCurrency().equals(targetCurrency)) {
                    return exchangeRateService.convertAmount(t.getAmount(), t.getCurrency(), targetCurrency);
                }
                return t.getAmount();
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate, String targetCurrency) {
        List<Transaction> transactions = findByDateRange(startDate, endDate);
        return transactions.stream()
            .filter(t -> !t.isIncome())
            .map(t -> {
                if (!t.getCurrency().equals(targetCurrency)) {
                    return exchangeRateService.convertAmount(t.getAmount(), t.getCurrency(), targetCurrency);
                }
                return t.getAmount();
            })
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
            boolean includeChildren, String targetCurrency) {
        Map<Tag, BigDecimal> totals = new HashMap<>();
        List<Tag> tags = tagService.findAll();
        
        for (Tag tag : tags) {
            List<Transaction> transactions = findByTag(tag, includeChildren);
            BigDecimal total = transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .map(t -> {
                    BigDecimal amount = t.isIncome() ? t.getAmount() : t.getAmount().negate();
                    if (!t.getCurrency().equals(targetCurrency)) {
                        return exchangeRateService.convertAmount(amount, t.getCurrency(), targetCurrency);
                    }
                    return amount;
                })
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
            boolean includeChildren, String targetCurrency) {
        Map<Tag, TransactionStatistics> statistics = new HashMap<>();
        List<Tag> tags = tagService.findAll();
        
        for (Tag tag : tags) {
            TransactionStatisticsImpl stats = new TransactionStatisticsImpl();
            List<Transaction> transactions = findByTag(tag, includeChildren);
            
            transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .forEach(t -> {
                    if (!t.getCurrency().equals(targetCurrency)) {
                        BigDecimal convertedAmount = exchangeRateService.convertAmount(
                            t.getAmount(), t.getCurrency(), targetCurrency);
                        Transaction convertedTransaction = new Transaction(
                            t.getDate(), t.getDescription(), convertedAmount, t.isIncome(), targetCurrency);
                        stats.addTransaction(convertedTransaction);
                    } else {
                        stats.addTransaction(t);
                    }
                });
                
            statistics.put(tag, stats);
        }
        
        return statistics;
    }

    @Override
    public BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate, 
            String targetCurrency) {
        List<Transaction> transactions = findByTag(tag, true);
        return transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .map(t -> {
                BigDecimal amount = t.isIncome() ? t.getAmount() : t.getAmount().negate();
                if (!t.getCurrency().equals(targetCurrency)) {
                    return exchangeRateService.convertAmount(amount, t.getCurrency(), targetCurrency);
                }
                return amount;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateNetWorth(LocalDate asOfDate, String targetCurrency) {
        List<Transaction> transactions = findByDateRange(LocalDate.MIN, asOfDate);
        return transactions.stream()
            .map(t -> {
                BigDecimal amount = t.isIncome() ? t.getAmount() : t.getAmount().negate();
                if (!t.getCurrency().equals(targetCurrency)) {
                    return exchangeRateService.convertAmount(amount, t.getCurrency(), targetCurrency);
                }
                return amount;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Transaction createTransaction(LocalDate date, String description, BigDecimal amount,
            boolean isIncome, Set<Long> tagIds) {
        return createTransaction(date, description, amount, isIncome, tagIds, "EUR");
    }

    @Override
    public void updateTransaction(Long id, LocalDate date, String description,
            BigDecimal amount, boolean isIncome, Set<Long> tagIds) {
        updateTransaction(id, date, description, amount, isIncome, tagIds, "EUR");
    }

    @Override
    public BigDecimal calculateBalance(LocalDate start, LocalDate end) {
        return calculateBalance(start, end, "EUR");
    }

    @Override
    public BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        return calculateIncomeForPeriod(startDate, endDate, "EUR");
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        return calculateExpensesForPeriod(startDate, endDate, "EUR");
    }

    @Override
    public Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate, boolean includeChildren) {
        return calculateTagTotals(startDate, endDate, includeChildren, "EUR");
    }

    @Override
    public Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate, boolean includeChildren) {
        return calculateTagStatistics(startDate, endDate, includeChildren, "EUR");
    }

    @Override
    public BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate) {
        return calculateAmountForTagInPeriod(tag, startDate, endDate, "EUR");
    }

    @Override
    public BigDecimal calculateNetWorth(LocalDate asOfDate) {
        return calculateNetWorth(asOfDate, "EUR");
    }
}
