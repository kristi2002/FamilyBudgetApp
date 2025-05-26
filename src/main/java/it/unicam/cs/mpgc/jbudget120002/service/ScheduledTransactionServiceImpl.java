package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {
    private final EntityManager entityManager;
    private final TagService tagService;

    public ScheduledTransactionServiceImpl(EntityManager entityManager, TagService tagService) {
        this.entityManager = entityManager;
        this.tagService = tagService;
    }

    @Override
    public ScheduledTransaction createScheduledTransaction(String description, BigDecimal amount,
            boolean isIncome, LocalDate startDate, LocalDate endDate,
            ScheduledTransaction.RecurrencePattern pattern, int recurrenceValue, Set<Long> tagIds) {
        entityManager.getTransaction().begin();
        try {
            ScheduledTransaction scheduled = new ScheduledTransaction(
                description, amount, isIncome, startDate, endDate, pattern, recurrenceValue);

            // Add tags
            for (Long tagId : tagIds) {
                tagService.findById(tagId).ifPresent(scheduled::addTag);
            }

            entityManager.persist(scheduled);
            entityManager.getTransaction().commit();
            return scheduled;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void deleteScheduledTransaction(Long id) {
        entityManager.getTransaction().begin();
        try {
            ScheduledTransaction scheduled = entityManager.find(ScheduledTransaction.class, id);
            if (scheduled != null) {
                entityManager.remove(scheduled);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public List<ScheduledTransaction> findAll() {
        TypedQuery<ScheduledTransaction> query = entityManager.createQuery(
            "SELECT s FROM ScheduledTransaction s", ScheduledTransaction.class);
        return query.getResultList();
    }

    @Override
    public Optional<ScheduledTransaction> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ScheduledTransaction.class, id));
    }

    @Override
    public void updateProcessingState(Long id, ScheduledTransaction.ProcessingState state) {
        entityManager.getTransaction().begin();
        try {
            ScheduledTransaction scheduled = entityManager.find(ScheduledTransaction.class, id);
            if (scheduled != null) {
                scheduled.setProcessingState(state);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public List<ScheduledTransaction> findByDateRange(LocalDate start, LocalDate end) {
        TypedQuery<ScheduledTransaction> query = entityManager.createQuery(
            "SELECT s FROM ScheduledTransaction s WHERE " +
            "s.startDate <= :end AND (s.endDate IS NULL OR s.endDate >= :start)",
            ScheduledTransaction.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public LoanAmortizationPlan createLoanPlan(String description, BigDecimal principalAmount,
            BigDecimal annualInterestRate, int termInMonths, LocalDate startDate) {
        // Implementation for loan plan creation
        throw new UnsupportedOperationException("Loan plan creation not implemented yet");
    }

    @Override
    public Optional<ScheduledTransaction> findScheduledById(Long id) {
        return Optional.ofNullable(entityManager.find(ScheduledTransaction.class, id));
    }

    @Override
    public Optional<LoanAmortizationPlan> findLoanPlanById(Long id) {
        return Optional.ofNullable(entityManager.find(LoanAmortizationPlan.class, id));
    }

    @Override
    public List<ScheduledTransaction> findActiveScheduledTransactions(LocalDate asOfDate) {
        TypedQuery<ScheduledTransaction> query = entityManager.createQuery(
            "SELECT s FROM ScheduledTransaction s WHERE " +
            "s.startDate <= :date AND (s.endDate IS NULL OR s.endDate >= :date) " +
            "AND s.processingState = :state",
            ScheduledTransaction.class);
        query.setParameter("date", asOfDate);
        query.setParameter("state", ScheduledTransaction.ProcessingState.PENDING);
        return query.getResultList();
    }

    @Override
    public List<LoanAmortizationPlan> findActiveLoanPlans(LocalDate asOfDate) {
        // Implementation for finding active loan plans
        throw new UnsupportedOperationException("Finding active loan plans not implemented yet");
    }

    @Override
    public void generateTransactionsUntil(Long scheduledId, LocalDate until) {
        entityManager.getTransaction().begin();
        try {
            ScheduledTransaction scheduled = findById(scheduledId).orElse(null);
            if (scheduled != null) {
                scheduled.generateTransactions(until);
                entityManager.merge(scheduled);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void updateScheduledTransaction(Long id, String description, BigDecimal amount,
            boolean isIncome, LocalDate startDate, LocalDate endDate,
            ScheduledTransaction.RecurrencePattern pattern, int interval, Set<Long> tagIds) {
        entityManager.getTransaction().begin();
        try {
            ScheduledTransaction scheduled = findById(id).orElse(null);
            if (scheduled != null) {
                scheduled.setDescription(description);
                scheduled.setAmount(amount);
                scheduled.setIncome(isIncome);
                scheduled.setStartDate(startDate);
                scheduled.setEndDate(endDate);
                scheduled.setRecurrencePattern(pattern);
                scheduled.setRecurrenceValue(interval);

                // Update tags
                scheduled.getTags().clear();
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(scheduled::addTag);
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void deleteLoanPlan(Long id) {
        entityManager.getTransaction().begin();
        try {
            LoanAmortizationPlan plan = entityManager.find(LoanAmortizationPlan.class, id);
            if (plan != null) {
                entityManager.remove(plan);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void generateTransactions(Long scheduledTransactionId, LocalDate asOfDate) {
        generateTransactionsUntil(scheduledTransactionId, asOfDate);
    }

    @Override
    public List<ScheduledTransaction> findDeadlinesForMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return findByDateRange(start, end);
    }
}
