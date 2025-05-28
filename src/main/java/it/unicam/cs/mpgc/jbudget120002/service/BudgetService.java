package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BudgetService {
    /**
     * Create a new budget for a specific period and category
     */
    Budget createBudget(String name, LocalDate startDate, LocalDate endDate, 
                       BigDecimal amount, Long categoryId);
    
    /**
     * Update an existing budget
     */
    void updateBudget(Long id, String name, LocalDate startDate, LocalDate endDate, 
                     BigDecimal amount, Long categoryId);
    
    /**
     * Delete a budget
     */
    void deleteBudget(Long id);
    
    /**
     * Get all budgets
     */
    List<Budget> findAll();
    
    /**
     * Find a budget by its ID
     */
    Optional<Budget> findById(Long id);
    
    /**
     * Find budgets within a date range
     */
    List<Budget> findByDateRange(LocalDate start, LocalDate end);
    
    /**
     * Find budgets for a specific category
     */
    List<Budget> findByCategory(Long categoryId);
    
    /**
     * Calculate budget status for a given period
     */
    Map<Long, BudgetStatus> calculateBudgetStatus(LocalDate start, LocalDate end);
    
    /**
     * Get budget alerts for active budgets
     */
    List<BudgetAlert> getBudgetAlerts();
    
    /**
     * Create a new budget template
     */
    BudgetTemplate createTemplate(String name, Map<Long, BigDecimal> categoryAmounts);
    
    /**
     * Apply a budget template to create new budgets
     */
    List<Budget> applyTemplate(Long templateId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get budget forecast for future months
     */
    Map<LocalDate, BigDecimal> getBudgetForecast(LocalDate startDate, int months);
    
    /**
     * Get budget comparison for a given period
     */
    BudgetComparison getBudgetComparison(LocalDate start, LocalDate end);
    
    /**
     * Calculate the amount spent for a budget
     */
    BigDecimal calculateSpentAmount(Long budgetId);
    
    /**
     * Calculate the remaining amount for a budget
     */
    BigDecimal calculateRemainingAmount(Long budgetId);
} 