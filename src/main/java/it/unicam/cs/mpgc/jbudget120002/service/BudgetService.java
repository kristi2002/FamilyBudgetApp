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

/**
 * Service interface for managing budget plans and financial planning in the Family Budget App.
 * 
 * <p>This interface defines the contract for budget management operations, including
 * CRUD operations, budget status tracking, financial comparisons, and forecasting.
 * It provides comprehensive budget planning and monitoring capabilities for users
 * and groups.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Create, read, update, and delete budget plans</li>
 *   <li>Track budget utilization and status</li>
 *   <li>Calculate budget comparisons and forecasts</li>
 *   <li>Monitor spending against budget limits</li>
 *   <li>Support user-specific and group-based budgets</li>
 *   <li>Provide budget recommendations and alerts</li>
 *   <li>Generate budget reports and analytics</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a new budget
 * Budget budget = new Budget("Monthly Groceries", new BigDecimal("500.00"), 
 *                           startDate, endDate);
 * budgetService.save(budget);
 * 
 * // Check budget status
 * Map<Long, BudgetStatus> status = budgetService.calculateBudgetStatus(startDate, endDate);
 * 
 * // Get budget comparison
 * BudgetComparison comparison = budgetService.getBudgetComparison(startDate, endDate);
 * 
 * // Calculate forecast
 * Map<LocalDate, BigDecimal> forecast = budgetService.getBudgetForecast(startDate, 6);
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public interface BudgetService {
    
    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Finds a budget by its unique identifier.
     * 
     * @param id the budget ID
     * @return an Optional containing the budget if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<Budget> findById(Long id);

    /**
     * Retrieves all budgets in the system.
     * 
     * @return a list of all budgets
     */
    List<Budget> findAll();

    /**
     * Retrieves all budgets associated with a specific user.
     * 
     * @param user the user whose budgets to retrieve
     * @return a list of budgets for the user
     * @throws IllegalArgumentException if user is null
     */
    List<Budget> findAllByUser(User user);

    /**
     * Saves a budget to the system.
     * 
     * @param budget the budget to save
     * @throws IllegalArgumentException if budget is null
     * @throws RuntimeException if budget validation fails
     */
    void save(Budget budget);

    /**
     * Deletes a budget by its ID.
     * 
     * @param id the ID of the budget to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if budget is not found
     */
    void delete(Long id);

    // ==================== SEARCH AND QUERY OPERATIONS ====================
    
    /**
     * Finds budgets within a specific date range.
     * 
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return a list of budgets active in the date range
     * @throws IllegalArgumentException if start or end date is null, or if start is after end
     */
    List<Budget> findByDateRange(LocalDate start, LocalDate end);

    /**
     * Finds budgets associated with a specific category.
     * 
     * @param categoryId the category ID
     * @return a list of budgets for the category
     * @throws IllegalArgumentException if categoryId is null
     */
    List<Budget> findByCategory(Long categoryId);

    // ==================== BUDGET STATUS AND MONITORING ====================
    
    /**
     * Calculates the status of all budgets within a date range.
     * 
     * @param start the start date
     * @param end the end date
     * @return a map of budget ID to budget status
     * @throws IllegalArgumentException if start or end date is null, or if start is after end
     */
    Map<Long, BudgetStatus> calculateBudgetStatus(LocalDate start, LocalDate end);

    /**
     * Generates a comprehensive budget comparison for a date range.
     * 
     * @param start the start date
     * @param end the end date
     * @return a budget comparison object containing category-wise analysis
     * @throws IllegalArgumentException if start or end date is null, or if start is after end
     */
    BudgetComparison getBudgetComparison(LocalDate start, LocalDate end);

    /**
     * Calculates the total amount spent against a specific budget.
     * 
     * @param budgetId the budget ID
     * @return the total amount spent
     * @throws IllegalArgumentException if budgetId is null
     * @throws RuntimeException if budget is not found
     */
    BigDecimal calculateSpentAmount(Long budgetId);

    // ==================== FORECASTING AND PLANNING ====================
    
    /**
     * Generates a budget forecast for future months.
     * 
     * @param startDate the starting date for the forecast
     * @param months the number of months to forecast
     * @return a map of dates to forecasted budget amounts
     * @throws IllegalArgumentException if startDate is null or months is negative
     */
    Map<LocalDate, BigDecimal> getBudgetForecast(LocalDate startDate, int months);
} 