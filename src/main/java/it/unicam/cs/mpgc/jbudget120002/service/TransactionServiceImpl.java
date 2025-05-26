package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
    public BigDecimal calculateBalance(LocalDate start, LocalDate end) {
        List<Transaction> transactions = findByDateRange(start, end);
        return transactions.stream()
            .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
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
        TypedQuery<BigDecimal> query = entityManager.createQuery(
            "SELECT SUM(t.amount) FROM Transaction t WHERE t.isIncome = true " +
            "AND t.date BETWEEN :start AND :end", BigDecimal.class);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        return query.getSingleResult() != null ? query.getSingleResult() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
            "SELECT SUM(t.amount) FROM Transaction t WHERE t.isIncome = false " +
            "AND t.date BETWEEN :start AND :end", BigDecimal.class);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        return query.getSingleResult() != null ? query.getSingleResult() : BigDecimal.ZERO;
    }
}
