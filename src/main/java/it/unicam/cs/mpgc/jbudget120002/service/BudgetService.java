package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Budget;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BudgetService {
    Budget createBudget(String name, BigDecimal amount, LocalDate startDate, LocalDate endDate, Set<Long> tagIds);
    
    void deleteBudget(Long id);
    
    List<Budget> findAll();
    
    Optional<Budget> findById(Long id);
    
    List<Budget> findByDateRange(LocalDate start, LocalDate end);
    
    void updateBudget(Long id, String name, BigDecimal amount, LocalDate startDate, LocalDate endDate, Set<Long> tagIds);
    
    BigDecimal calculateSpentAmount(Long budgetId);
    
    BigDecimal calculateRemainingAmount(Long budgetId);
    
    List<Budget> findActiveBudgets(LocalDate asOfDate);
    
    void addTagToBudget(Long budgetId, Long tagId);
    
    void removeTagFromBudget(Long budgetId, Long tagId);
} 