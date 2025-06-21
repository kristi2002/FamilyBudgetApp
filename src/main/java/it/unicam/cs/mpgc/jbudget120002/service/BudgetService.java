package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Budget;
import it.unicam.cs.mpgc.jbudget120002.model.BudgetComparison;
import it.unicam.cs.mpgc.jbudget120002.model.BudgetStatus;
import it.unicam.cs.mpgc.jbudget120002.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BudgetService {
    Optional<Budget> findById(Long id);

    List<Budget> findAll();

    List<Budget> findAllByUser(User user);

    void save(Budget budget);

    void delete(Long id);

    List<Budget> findByDateRange(LocalDate start, LocalDate end);

    List<Budget> findByCategory(Long categoryId);

    Map<Long, BudgetStatus> calculateBudgetStatus(LocalDate start, LocalDate end);

    BudgetComparison getBudgetComparison(LocalDate start, LocalDate end);

    BigDecimal calculateSpentAmount(Long budgetId);

    Map<LocalDate, BigDecimal> getBudgetForecast(LocalDate startDate, int months);
} 