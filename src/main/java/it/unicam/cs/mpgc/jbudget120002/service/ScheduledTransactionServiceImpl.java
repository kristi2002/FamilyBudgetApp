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
            if (pattern == null) {
                pattern = ScheduledTransaction.RecurrencePattern.MONTHLY;
            }
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
                LocalDate currentDate = scheduled.getStartDate();
                LocalDate effectiveEndDate = scheduled.getEndDate() != null ? scheduled.getEndDate() : until;
                while (!currentDate.isAfter(effectiveEndDate) && !currentDate.isAfter(until)) {
                    // Check if a transaction for this date already exists
                    TypedQuery<Transaction> query = entityManager.createQuery(
                        "SELECT t FROM Transaction t WHERE t.scheduledTransaction.id = :scheduledId AND t.date = :date",
                        Transaction.class);
                    query.setParameter("scheduledId", scheduled.getId());
                    query.setParameter("date", currentDate);
                    boolean exists = !query.getResultList().isEmpty();
                    if (!exists) {
                        Transaction transaction = new Transaction(
                            currentDate,
                            scheduled.getDescription(),
                            scheduled.getAmount(),
                            scheduled.isIncome()
                        );
                        transaction.setCurrency("EUR");
                        transaction.setScheduledTransaction(scheduled);
                        for (Tag tag : scheduled.getTags()) {
                            transaction.addTag(tag);
                        }
                        entityManager.persist(transaction);
                    }
                    // Calculate next occurrence
                    switch (scheduled.getPattern()) {
                        case DAILY -> currentDate = currentDate.plusDays(scheduled.getRecurrenceValue());
                        case WEEKLY -> currentDate = currentDate.plusWeeks(scheduled.getRecurrenceValue());
                        case MONTHLY -> currentDate = currentDate.plusMonths(scheduled.getRecurrenceValue());
                        case YEARLY -> currentDate = currentDate.plusYears(scheduled.getRecurrenceValue());
                    }
                }
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
            ScheduledTransaction.RecurrencePattern pattern, int recurrenceValue, Set<Long> tagIds) {
        entityManager.getTransaction().begin();
        try {
            ScheduledTransaction scheduled = entityManager.find(ScheduledTransaction.class, id);
            if (scheduled != null) {
                scheduled.setDescription(description);
                scheduled.setAmount(amount);
                scheduled.setIncome(isIncome);
                scheduled.setStartDate(startDate);
                scheduled.setEndDate(endDate);
                scheduled.setPattern(pattern);
                scheduled.setRecurrenceValue(recurrenceValue);
                
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
    public void generateTransactions(Long id, LocalDate upToDate) {
        entityManager.getTransaction().begin();
        try {
            ScheduledTransaction scheduled = entityManager.find(ScheduledTransaction.class, id);
            if (scheduled != null) {
                LocalDate currentDate = scheduled.getStartDate();
                while (!currentDate.isAfter(upToDate) && 
                       (scheduled.getEndDate() == null || !currentDate.isAfter(scheduled.getEndDate()))) {
                    // Create transaction for this occurrence
                    Transaction transaction = new Transaction(
                        currentDate,
                        scheduled.getDescription(),
                        scheduled.getAmount(),
                        scheduled.isIncome()
                    );
                    transaction.setCurrency("EUR");
                    transaction.setScheduledTransaction(scheduled);
                    scheduled.getTags().forEach(transaction::addTag);
                    entityManager.persist(transaction);
                    
                    // Calculate next occurrence based on pattern
                    currentDate = switch (scheduled.getPattern()) {
                        case DAILY -> currentDate.plusDays(scheduled.getRecurrenceValue());
                        case WEEKLY -> currentDate.plusWeeks(scheduled.getRecurrenceValue());
                        case MONTHLY -> currentDate.plusMonths(scheduled.getRecurrenceValue());
                        case YEARLY -> currentDate.plusYears(scheduled.getRecurrenceValue());
                    };
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public List<ScheduledTransaction> findDeadlinesForMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return findByDateRange(start, end);
    }

    @Override
    public List<ScheduledTransaction> findByTag(Tag tag, boolean includeSubcategories) {
        TypedQuery<ScheduledTransaction> query;
        if (includeSubcategories) {
            query = entityManager.createQuery(
                "SELECT DISTINCT st FROM ScheduledTransaction st " +
                "JOIN st.tags t " +
                "WHERE t.id = :tagId OR t.parent.id = :tagId " +
                "ORDER BY st.startDate DESC", ScheduledTransaction.class);
        } else {
            query = entityManager.createQuery(
                "SELECT DISTINCT st FROM ScheduledTransaction st " +
                "JOIN st.tags t " +
                "WHERE t.id = :tagId " +
                "ORDER BY st.startDate DESC", ScheduledTransaction.class);
        }
        query.setParameter("tagId", tag.getId());
        return query.getResultList();
    }

    @Override
    public BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
            "SELECT COALESCE(SUM(st.amount), 0) FROM ScheduledTransaction st " +
            "WHERE st.isIncome = true " +
            "AND st.startDate <= :endDate " +
            "AND (st.endDate IS NULL OR st.endDate >= :startDate)", BigDecimal.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getSingleResult();
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
            "SELECT COALESCE(SUM(st.amount), 0) FROM ScheduledTransaction st " +
            "WHERE st.isIncome = false " +
            "AND st.startDate <= :endDate " +
            "AND (st.endDate IS NULL OR st.endDate >= :startDate)", BigDecimal.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getSingleResult();
    }
}
