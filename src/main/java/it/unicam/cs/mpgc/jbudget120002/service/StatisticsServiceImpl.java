package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Statistic;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class StatisticsServiceImpl implements StatisticsService {
    private final EntityManager entityManager;
    private final TransactionService transactionService;
    private final TagService tagService;

    public StatisticsServiceImpl(EntityManager entityManager,
                               TransactionService transactionService,
                               TagService tagService) {
        this.entityManager = entityManager;
        this.transactionService = transactionService;
        this.tagService = tagService;
    }

    @Override
    public List<Statistic.CategoryExpense> getTopExpenseCategories(
            LocalDate startDate, LocalDate endDate, int limit) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        return Statistic.getTopExpenseCategories(
            new HashSet<>(transactions), startDate, endDate, limit);
    }

    @Override
    public List<Statistic.MonthlyBalance> getMonthlyBalances(
            LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        return Statistic.getMonthlyBalances(
            new HashSet<>(transactions), startDate, endDate);
    }

    @Override
    public List<Statistic.CategoryComparison> comparePeriods(
            LocalDate previousStart, LocalDate previousEnd,
            LocalDate currentStart, LocalDate currentEnd) {
        List<Transaction> allTransactions = transactionService.findByDateRange(
            previousStart, currentEnd);
        return Statistic.comparePeriods(
            new HashSet<>(allTransactions),
            previousStart, previousEnd,
            currentStart, currentEnd);
    }

    @Override
    public Map<Tag, Double> getCategoryPercentages(
            LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        return Statistic.calculateCategoryPercentages(
            new HashSet<>(transactions), startDate, endDate);
    }

    @Override
    public BigDecimal getNetWorth(LocalDate asOfDate) {
        return Statistic.calculateNetWorth(
            new HashSet<>(transactionService.findByDateRange(
                LocalDate.of(1900, 1, 1), asOfDate)), 
            asOfDate);
    }

    @Override
    public Map<Integer, Map<Tag, BigDecimal>> getYearlyComparison(int startYear, int endYear) {
        Map<Integer, Map<Tag, BigDecimal>> yearlyData = new TreeMap<>();
        
        for (int year = startYear; year <= endYear; year++) {
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);
            
            List<Transaction> yearTransactions = transactionService.findByDateRange(
                yearStart, yearEnd);
            
            Map<Tag, BigDecimal> yearExpenses = new HashMap<>();
            for (Transaction transaction : yearTransactions) {
                if (!transaction.isIncome()) {
                    for (Tag tag : transaction.getTags()) {
                        yearExpenses.merge(tag, transaction.getAmount(), BigDecimal::add);
                    }
                }
            }
            
            yearlyData.put(year, yearExpenses);
        }
        
        return yearlyData;
    }
} 