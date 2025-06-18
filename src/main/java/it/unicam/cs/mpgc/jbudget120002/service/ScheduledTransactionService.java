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

public interface ScheduledTransactionService {
    ScheduledTransaction createScheduledTransaction(String description, BigDecimal amount, 
        boolean isIncome, LocalDate startDate, LocalDate endDate, 
        ScheduledTransaction.RecurrencePattern pattern, int recurrenceValue, Set<Long> tagIds);
    
    void deleteScheduledTransaction(Long id);
    
    List<ScheduledTransaction> findAll();
    
    Optional<ScheduledTransaction> findById(Long id);
    
    void updateProcessingState(Long id, ScheduledTransaction.ProcessingState state);
    
    List<ScheduledTransaction> findByDateRange(LocalDate start, LocalDate end);

    LoanAmortizationPlan createLoanPlan(
            String description, BigDecimal principalAmount,
            BigDecimal annualInterestRate, int termInMonths,
            LocalDate startDate);

    Optional<ScheduledTransaction> findScheduledById(Long id);

    Optional<LoanAmortizationPlan> findLoanPlanById(Long id);

    List<ScheduledTransaction> findActiveScheduledTransactions(LocalDate asOfDate);

    List<LoanAmortizationPlan> findActiveLoanPlans(LocalDate asOfDate);

    void generateTransactionsUntil(Long scheduledId, LocalDate until);

    void updateScheduledTransaction(
            Long id, String description, BigDecimal amount,
            boolean isIncome, LocalDate startDate, LocalDate endDate,
            ScheduledTransaction.RecurrencePattern pattern, int interval,
            Set<Long> tagIds);

    void deleteLoanPlan(Long id);

    void generateTransactions(Long scheduledTransactionId, LocalDate asOfDate);

    List<ScheduledTransaction> findDeadlinesForMonth(YearMonth month);

    List<ScheduledTransaction> findByTag(Tag tag, boolean includeSubcategories);

    BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate);

    BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate);
}