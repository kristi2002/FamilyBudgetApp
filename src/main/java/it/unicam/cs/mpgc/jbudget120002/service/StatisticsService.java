package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Statistic;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public interface StatisticsService {
    List<Statistic.CategoryExpense> getTopExpenseCategories(LocalDate startDate, LocalDate endDate, int limit);
    
    List<Statistic.MonthlyBalance> getMonthlyBalances(LocalDate startDate, LocalDate endDate);
    
    List<Statistic.CategoryComparison> comparePeriods(LocalDate previousStart, LocalDate previousEnd,
                                                     LocalDate currentStart, LocalDate currentEnd);
    
    BigDecimal getNetWorth(LocalDate asOfDate);
    
    Map<Tag, Double> getCategoryPercentages(LocalDate startDate, LocalDate endDate);
    
    Map<Integer, Map<Tag, BigDecimal>> getYearlyComparison(int startYear, int endYear);
} 