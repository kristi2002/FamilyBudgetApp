package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<MonthlyStatistic> getMonthlyStatistics(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        Map<YearMonth, List<Transaction>> transactionsByMonth = transactions.stream()
            .collect(Collectors.groupingBy(t -> YearMonth.from(t.getDate())));
            
        return transactionsByMonth.entrySet().stream()
            .map(entry -> {
                BigDecimal income = entry.getValue().stream()
                    .filter(Transaction::isIncome)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                BigDecimal expenses = entry.getValue().stream()
                    .filter(t -> !t.isIncome())
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                return new MonthlyStatistic(entry.getKey(), income, expenses);
            })
            .sorted(Comparator.comparing(MonthlyStatistic::getMonth))
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryStatistic> getCategoryStatistics(
        LocalDate startDate,
        LocalDate endDate,
        Tag category,
        boolean includeSubcategories
    ) {
        List<CategoryStatistic> stats = new ArrayList<>();
        
        // Get all relevant categories
        List<Tag> categories = includeSubcategories ? 
            tagService.findTagAndDescendants(category) :
            Collections.singletonList(category);
        
        // Calculate statistics for each category
        for (Tag tag : categories) {
            BigDecimal currentAmount = transactionService.calculateAmountForTagInPeriod(tag, startDate, endDate);
            BigDecimal previousAmount = transactionService.calculateAmountForTagInPeriod(
                tag,
                startDate.minusMonths(1),
                endDate.minusMonths(1)
            );
            stats.add(new CategoryStatistic(tag, currentAmount, previousAmount));
        }
        
        // Sort by absolute percentage change
        stats.sort((a, b) -> Double.compare(
            Math.abs(b.getPercentageChange()),
            Math.abs(a.getPercentageChange())
        ));
        
        return stats;
    }

    @Override
    public List<BudgetStatistic> getBudgetStatistics(
        LocalDate startDate,
        LocalDate endDate,
        Tag category
    ) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        Map<Tag, BigDecimal> actualSpending = aggregateTransactionsByTag(
            transactions,
            category,
            true
        );
        
        return actualSpending.entrySet().stream()
            .map(entry -> {
                Tag tag = entry.getKey();
                BigDecimal actual = entry.getValue();
                BigDecimal budget = getBudgetAmount(tag, startDate, endDate);
                return new BudgetStatistic(tag, budget, actual);
            })
            .sorted(Comparator.comparing(stat -> stat.getCategory().getName()))
            .collect(Collectors.toList());
    }

    private BigDecimal getBudgetAmount(Tag tag, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement budget lookup logic once Budget entity is available
        return BigDecimal.ZERO;
    }

    private Map<Tag, BigDecimal> aggregateTransactionsByTag(
        List<Transaction> transactions,
        Tag rootCategory,
        boolean includeSubcategories
    ) {
        return transactions.stream()
            .filter(t -> isRelevantTransaction(t, rootCategory, includeSubcategories))
            .collect(Collectors.groupingBy(
                this::getPrimaryTag,
                Collectors.mapping(
                    Transaction::getAmount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
    }

    private boolean isRelevantTransaction(
        Transaction transaction,
        Tag rootCategory,
        boolean includeSubcategories
    ) {
        if (rootCategory == null) {
            return true;
        }
        
        Tag primaryTag = getPrimaryTag(transaction);
        if (primaryTag == null) {
            return false;
        }
        
        if (includeSubcategories) {
            return isTagOrDescendant(primaryTag, rootCategory);
        } else {
            return primaryTag.equals(rootCategory);
        }
    }

    private Tag getPrimaryTag(Transaction transaction) {
        return transaction.getTags().stream()
            .filter(tag -> tag.getParent() == null || 
                !transaction.getTags().contains(tag.getParent()))
            .findFirst()
            .orElse(null);
    }

    private boolean isTagOrDescendant(Tag tag, Tag ancestor) {
        while (tag != null) {
            if (tag.equals(ancestor)) {
                return true;
            }
            tag = tag.getParent();
        }
        return false;
    }

    @Override
    public BigDecimal calculateTotalIncome(LocalDate startDate, LocalDate endDate) {
        return transactionService.calculateIncomeForPeriod(startDate, endDate);
    }

    @Override
    public BigDecimal calculateTotalExpenses(LocalDate startDate, LocalDate endDate) {
        return transactionService.calculateExpensesForPeriod(startDate, endDate);
    }

    @Override
    public double calculateSavingsRate(LocalDate startDate, LocalDate endDate) {
        BigDecimal income = calculateTotalIncome(startDate, endDate);
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        BigDecimal expenses = calculateTotalExpenses(startDate, endDate);
        BigDecimal savings = income.subtract(expenses);
        
        return savings.divide(income, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    @Override
    public List<CategoryExpense> getTopExpenseCategories(
            LocalDate startDate, LocalDate endDate, int limit) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        Map<Tag, BigDecimal> expensesByCategory = aggregateTransactionsByTag(
            transactions.stream()
                .filter(t -> !t.isIncome())
                .collect(Collectors.toList()),
            null,
            true
        );
        
        return expensesByCategory.entrySet().stream()
            .map(entry -> new CategoryExpense(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(CategoryExpense::getAmount).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyBalance> getMonthlyBalances(LocalDate startDate, LocalDate endDate) {
        List<MonthlyStatistic> monthlyStats = getMonthlyStatistics(startDate, endDate);
        return monthlyStats.stream()
            .map(stat -> new MonthlyBalance(
                stat.getMonth(),
                stat.getIncome(),
                stat.getExpenses(),
                stat.getBalance()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryComparison> comparePeriods(
            LocalDate previousStart, LocalDate previousEnd,
            LocalDate currentStart, LocalDate currentEnd) {
        List<CategoryStatistic> stats = getCategoryStatistics(
            currentStart, currentEnd, null, true);
            
        return stats.stream()
            .map(stat -> new CategoryComparison(
                stat.getCategory(),
                stat.getPreviousAmount(),
                stat.getCurrentAmount(),
                stat.getDifference(),
                stat.getPercentageChange()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public Map<Tag, Double> getCategoryPercentages(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        BigDecimal totalExpenses = transactions.stream()
            .filter(t -> !t.isIncome())
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyMap();
        }
        
        Map<Tag, BigDecimal> expensesByCategory = aggregateTransactionsByTag(
            transactions.stream()
                .filter(t -> !t.isIncome())
                .collect(Collectors.toList()),
            null,
            true
        );
        
        return expensesByCategory.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue()
                    .divide(totalExpenses, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
            ));
    }

    @Override
    public BigDecimal getNetWorth(LocalDate asOfDate) {
        List<Transaction> transactions = transactionService.findByDateRange(
            LocalDate.of(1900, 1, 1), asOfDate);
            
        return transactions.stream()
            .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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