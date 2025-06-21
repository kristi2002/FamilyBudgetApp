package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.repository.ScheduledTransactionRepository;
import it.unicam.cs.mpgc.jbudget120002.repository.TransactionRepository;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ScheduledTransactionServiceImpl extends BaseService implements ScheduledTransactionService {
    private final ScheduledTransactionRepository repository;
    private final TransactionRepository transactionRepository;
    private final TagService tagService;

    public ScheduledTransactionServiceImpl(EntityManager entityManager, ScheduledTransactionRepository repository,
                                         TransactionRepository transactionRepository, TagService tagService) {
        super(entityManager);
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.tagService = tagService;
    }

    @Override
    public ScheduledTransaction createScheduledTransaction(String description, BigDecimal amount,
            boolean isIncome, LocalDate startDate, LocalDate endDate,
            ScheduledTransaction.RecurrencePattern pattern, int recurrenceValue, Set<Long> tagIds, User user) {
        
        ScheduledTransaction scheduled = new ScheduledTransaction(
            description, amount, isIncome, startDate, endDate, pattern, recurrenceValue);
        scheduled.setUser(user);

        for (Long tagId : tagIds) {
            tagService.findById(tagId).ifPresent(scheduled::addTag);
        }
        
        executeInTransaction(() -> repository.save(scheduled));
        return scheduled;
    }

    @Override
    public void deleteScheduledTransaction(Long id) {
        executeInTransaction(() -> repository.deleteById(id));
    }

    @Override
    public List<ScheduledTransaction> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ScheduledTransaction> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void updateProcessingState(Long id, ScheduledTransaction.ProcessingState state) {
        executeInTransaction(() -> {
            repository.findById(id).ifPresent(scheduled -> {
                scheduled.setProcessingState(state);
                repository.save(scheduled);
            });
        });
    }

    @Override
    public List<ScheduledTransaction> findByDateRange(LocalDate start, LocalDate end) {
        return repository.findByDateRange(start, end);
    }

    @Override
    public LoanAmortizationPlan createLoanPlan(String description, BigDecimal principalAmount,
            BigDecimal annualInterestRate, int termInMonths, LocalDate startDate) {
        throw new UnsupportedOperationException("Loan plan creation not implemented yet");
    }

    @Override
    public Optional<ScheduledTransaction> findScheduledById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<LoanAmortizationPlan> findLoanPlanById(Long id) {
        // This should probably be in its own repository, but leaving as is for now
        return Optional.ofNullable(em.find(LoanAmortizationPlan.class, id));
    }

    @Override
    public List<ScheduledTransaction> findActiveScheduledTransactions(LocalDate asOfDate) {
        return repository.findActive(asOfDate);
    }

    @Override
    public List<LoanAmortizationPlan> findActiveLoanPlans(LocalDate asOfDate) {
        throw new UnsupportedOperationException("Finding active loan plans not implemented yet");
    }

    @Override
    public void generateTransactionsUntil(Long scheduledId, LocalDate until) {
        executeInTransaction(() -> {
            repository.findById(scheduledId).ifPresent(scheduled -> {
                LocalDate currentDate = scheduled.getStartDate();
                LocalDate effectiveEndDate = scheduled.getEndDate() != null ? scheduled.getEndDate() : until;
                
                while (!currentDate.isAfter(effectiveEndDate) && !currentDate.isAfter(until)) {
                    boolean exists = transactionRepository.existsByScheduledTransactionAndDate(scheduled, currentDate);
                    if (!exists) {
                        Transaction transaction = new Transaction(
                            currentDate,
                            scheduled.getDescription(),
                            scheduled.getAmount(),
                            scheduled.isIncome()
                        );
                        transaction.setCurrency("EUR");
                        transaction.setScheduledTransaction(scheduled);
                        transaction.setUser(scheduled.getUser()); // Set the user on the transaction
                        for (Tag tag : scheduled.getTags()) {
                            transaction.addTag(tag);
                        }
                        transactionRepository.save(transaction);
                    }
                    
                    switch (scheduled.getPattern()) {
                        case DAILY -> currentDate = currentDate.plusDays(scheduled.getRecurrenceValue());
                        case WEEKLY -> currentDate = currentDate.plusWeeks(scheduled.getRecurrenceValue());
                        case MONTHLY -> currentDate = currentDate.plusMonths(scheduled.getRecurrenceValue());
                        case YEARLY -> currentDate = currentDate.plusYears(scheduled.getRecurrenceValue());
                    }
                }
            });
        });
    }

    @Override
    public void updateScheduledTransaction(Long id, String description, BigDecimal amount,
            boolean isIncome, LocalDate startDate, LocalDate endDate,
            ScheduledTransaction.RecurrencePattern pattern, int recurrenceValue, Set<Long> tagIds) {
        
        executeInTransaction(() -> {
            repository.findById(id).ifPresent(scheduled -> {
                scheduled.setDescription(description);
                scheduled.setAmount(amount);
                scheduled.setIncome(isIncome);
                scheduled.setStartDate(startDate);
                scheduled.setEndDate(endDate);
                scheduled.setPattern(pattern);
                scheduled.setRecurrenceValue(recurrenceValue);
                
                scheduled.getTags().clear();
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(scheduled::addTag);
                }
                repository.save(scheduled);
            });
        });
    }

    @Override
    public void deleteLoanPlan(Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void generateTransactions(Long id, LocalDate upToDate) {
        generateTransactionsUntil(id, upToDate);
    }

    @Override
    public List<ScheduledTransaction> findDeadlinesForMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return repository.findByDateRange(start, end);
    }

    @Override
    public List<ScheduledTransaction> findByTag(Tag tag, boolean includeSubcategories) {
        if (includeSubcategories) {
            List<Tag> tags = tagService.findTagAndDescendants(tag);
            return repository.findByTags(tags);
        } else {
            return repository.findByTag(tag);
        }
    }

    @Override
    public BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        return repository.calculateSumForPeriod(startDate, endDate, true);
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        return repository.calculateSumForPeriod(startDate, endDate, false);
    }

    @Override
    public void generateTransactionsForUser(User user, LocalDate untilDate) {
        List<ScheduledTransaction> scheduledTransactions = repository.findByUser(user);
        for (ScheduledTransaction scheduled : scheduledTransactions) {
            generateTransactionsUntil(scheduled.getId(), untilDate);
        }
    }

    @Override
    public List<ScheduledTransaction> findByUser(User user) {
        return repository.findByUser(user);
    }
}
