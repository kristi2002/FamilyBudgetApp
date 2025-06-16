package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.model.StatisticsModels.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import jakarta.persistence.TypedQuery;

/**
 * Implements the StatisticsService interface to provide comprehensive statistical analysis
 * and reporting for the Family Budget App. This class aggregates, analyzes, and compares
 * financial data such as transactions, budgets, and categories to generate insights for users.
 *
 * Responsibilities:
 * - Calculate monthly, category-based, and budget statistics
 * - Detect spending anomalies and generate forecasts
 * - Provide time-based and trend analyses
 * - Support for savings progress and net worth calculations
 *
 * Usage:
 * Used by controllers to retrieve and display statistical data, trends, and reports
 * for the user interface. Relies on TransactionService and TagService for data access.
 */
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
        List<MonthlyStatistic> stats = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        while (!current.isAfter(end)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();

            BigDecimal income = transactions.stream()
                .filter(t -> t.isIncome() && !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expenses = transactions.stream()
                .filter(t -> !t.isIncome() && !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.add(new MonthlyStatistic(current, income, expenses));
            current = current.plusMonths(1);
        }

        return stats;
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
            .map(t -> new Object[]{getPrimaryTag(t), t})
            .filter(arr -> arr[0] != null) // Skip transactions with null primary tag
            .collect(Collectors.groupingBy(
                arr -> (Tag) arr[0],
                Collectors.mapping(arr -> ((Transaction) arr[1]).getAmount(), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
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
            // Check if the transaction's tag is a descendant of the root category
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
    public List<CategoryExpense> getTopExpenseCategories(LocalDate startDate, LocalDate endDate, int limit) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        // Only keep expenses
        transactions = transactions.stream()
            .filter(t -> !t.isIncome())
            .collect(Collectors.toList());
        // Group by primary tag
        Map<Tag, BigDecimal> expensesByTag = transactions.stream()
            .collect(Collectors.groupingBy(
                this::getPrimaryTag,
                Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
        // Sort and limit
        return expensesByTag.entrySet().stream()
            .sorted(Map.Entry.<Tag, BigDecimal>comparingByValue().reversed())
            .limit(limit)
            .map(e -> new CategoryExpense(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyBalance> getMonthlyBalances(LocalDate startDate, LocalDate endDate) {
        // Instead of loading all transactions and processing them in memory,
        // use a more efficient SQL query to calculate monthly balances
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date), " +
            "SUM(CASE WHEN t.isIncome = true THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.isIncome = false THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.isIncome = true THEN t.amount ELSE -t.amount END) " +
            "FROM Transaction t " +
            "WHERE t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date) " +
            "ORDER BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)",
            Object[].class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Object[]> results = query.getResultList();
        System.out.println("Raw monthly balance query results:");
        for (Object[] result : results) {
            System.out.println(java.util.Arrays.toString(result));
        }
        List<MonthlyBalance> balances = new ArrayList<>();

        for (Object[] result : results) {
            int year = (Integer) result[0];
            int month = (Integer) result[1];
            BigDecimal income = (BigDecimal) result[2];
            BigDecimal expenses = (BigDecimal) result[3];
            BigDecimal balance = (BigDecimal) result[4];
            
            YearMonth yearMonth = YearMonth.of(year, month);
            balances.add(new MonthlyBalance(yearMonth, income, expenses, balance));
        }

        return balances;
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
        return transactionService.calculateNetWorth(asOfDate);
    }

    @Override
    public Map<Integer, Map<Tag, BigDecimal>> getYearlyComparison(int startYear, int endYear) {
        Map<Integer, Map<Tag, BigDecimal>> yearlyComparison = new HashMap<>();
        
        for (int year = startYear; year <= endYear; year++) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            
            List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
            Map<Tag, BigDecimal> yearlyStats = aggregateTransactionsByTag(
                transactions,
                null,
                true
            );
            
            yearlyComparison.put(year, yearlyStats);
        }
        
        return yearlyComparison;
    }

    @Override
    public List<SpendingAnomaly> detectSpendingAnomalies(LocalDate startDate, LocalDate endDate, Tag category) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        List<SpendingAnomaly> anomalies = new ArrayList<>();
        
        // Group transactions by category
        Map<Tag, List<Transaction>> transactionsByTag = transactions.stream()
            .collect(Collectors.groupingBy(this::getPrimaryTag));
            
        // Calculate statistics for each category
        for (Map.Entry<Tag, List<Transaction>> entry : transactionsByTag.entrySet()) {
            Tag tag = entry.getKey();
            if (category != null && !tag.equals(category)) {
                continue;
            }
            
            List<Transaction> tagTransactions = entry.getValue();
            if (tagTransactions.size() < 3) {
                continue; // Need at least 3 transactions for meaningful analysis
            }
            
            // Calculate average and standard deviation for anomaly detection
            BigDecimal total = tagTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal average = total.divide(
                BigDecimal.valueOf(tagTransactions.size()),
                2,
                java.math.RoundingMode.HALF_UP
            );
            
            // Calculate standard deviation
            double variance = tagTransactions.stream()
                .map(t -> t.getAmount().subtract(average).pow(2))
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
            double stdDev = Math.sqrt(variance);
            
            // Detect anomalies (transactions more than 2 standard deviations from mean)
            for (Transaction transaction : tagTransactions) {
                BigDecimal amount = transaction.getAmount();
                double deviation = amount.subtract(average).abs().doubleValue();
                double deviationPercentage = (deviation / average.doubleValue()) * 100;
                
                if (deviation > 2 * stdDev) {
                    anomalies.add(new SpendingAnomaly(
                        transaction.getDate().atStartOfDay(),
                        tag,
                        amount,
                        average,
                        deviationPercentage,
                        amount.compareTo(average) > 0 ? "Spike" : "Drop"
                    ));
                }
            }
        }
        
        return anomalies;
    }

    @Override
    public Map<Tag, TimeBasedPattern> getTimeBasedPatterns(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        Map<Tag, List<Transaction>> transactionsByTag = transactions.stream()
            .collect(Collectors.groupingBy(this::getPrimaryTag));

        Map<Tag, TimeBasedPattern> patterns = new HashMap<>();
        
        for (Map.Entry<Tag, List<Transaction>> entry : transactionsByTag.entrySet()) {
            Tag tag = entry.getKey();
            List<Transaction> tagTransactions = entry.getValue();
            
            // Calculate hourly distribution (0-23)
            Map<Integer, BigDecimal> hourlyDistribution = new HashMap<>();
            for (int hour = 0; hour < 24; hour++) {
                final int currentHour = hour;
                BigDecimal total = tagTransactions.stream()
                    .filter(t -> t.getDate().atStartOfDay().getHour() == currentHour)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                hourlyDistribution.put(hour, total);
            }
            
            // Calculate daily distribution (1-7)
            Map<Integer, BigDecimal> dailyDistribution = new HashMap<>();
            for (int day = 1; day <= 7; day++) {
                final int currentDay = day;
                BigDecimal total = tagTransactions.stream()
                    .filter(t -> t.getDate().getDayOfWeek().getValue() == currentDay)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                dailyDistribution.put(day, total);
            }
            
            // Calculate monthly distribution (1-12)
            Map<Integer, BigDecimal> monthlyDistribution = new HashMap<>();
            for (int month = 1; month <= 12; month++) {
                final int currentMonth = month;
                BigDecimal total = tagTransactions.stream()
                    .filter(t -> t.getDate().getMonthValue() == currentMonth)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                monthlyDistribution.put(month, total);
            }
            
            patterns.put(tag, new TimeBasedPattern(
                tag,
                hourlyDistribution,
                dailyDistribution,
                monthlyDistribution
            ));
        }
        
        return patterns;
    }

    @Override
    public Map<Tag, BudgetRecommendation> getBudgetRecommendations(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        Map<Tag, List<Transaction>> transactionsByTag = transactions.stream()
            .collect(Collectors.groupingBy(this::getPrimaryTag));
            
        Map<Tag, BudgetRecommendation> recommendations = new HashMap<>();
        
        for (Map.Entry<Tag, List<Transaction>> entry : transactionsByTag.entrySet()) {
            Tag tag = entry.getKey();
            List<Transaction> tagTransactions = entry.getValue();
            
            if (tagTransactions.size() < 3) {
                continue; // Need at least 3 transactions for meaningful analysis
            }
            
            // Calculate current average spending
            BigDecimal totalSpending = tagTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal currentAverage = totalSpending.divide(
                BigDecimal.valueOf(tagTransactions.size()),
                2,
                java.math.RoundingMode.HALF_UP
            );
            
            // Calculate standard deviation
            double variance = tagTransactions.stream()
                .map(t -> t.getAmount().subtract(currentAverage).pow(2))
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
            double stdDev = Math.sqrt(variance);
            
            // Calculate recommended budget (average + 1 standard deviation)
            BigDecimal recommendedBudget = currentAverage.add(BigDecimal.valueOf(stdDev));
            
            // Generate reasoning based on spending patterns
            String reasoning;
            double confidenceScore;
            
            if (stdDev / currentAverage.doubleValue() < 0.1) {
                reasoning = "Stable spending pattern detected. Current average is a good baseline.";
                confidenceScore = 0.9;
            } else if (stdDev / currentAverage.doubleValue() < 0.3) {
                reasoning = "Moderate spending variation. Adding one standard deviation for safety.";
                confidenceScore = 0.7;
            } else {
                reasoning = "High spending variation. Consider reviewing spending habits.";
                confidenceScore = 0.5;
            }
            
            recommendations.put(tag, new BudgetRecommendation(
                tag,
                currentAverage,
                recommendedBudget,
                reasoning,
                confidenceScore
            ));
        }
        
        return recommendations;
    }

    @Override
    public SavingsProgress getSavingsProgress(LocalDate startDate, LocalDate endDate) {
        // Calculate total income and expenses for the period
        BigDecimal totalIncome = calculateTotalIncome(startDate, endDate);
        BigDecimal totalExpenses = calculateTotalExpenses(startDate, endDate);
        BigDecimal currentAmount = totalIncome.subtract(totalExpenses);
        
        // Calculate monthly contribution (average savings per month)
        long monthsBetween = ChronoUnit.MONTHS.between(startDate, endDate) + 1;
        BigDecimal monthlyContribution = currentAmount.divide(
            BigDecimal.valueOf(monthsBetween),
            2,
            java.math.RoundingMode.HALF_UP
        );
        
        // Set a target amount (e.g., 3 months of expenses)
        BigDecimal targetAmount = totalExpenses.multiply(BigDecimal.valueOf(3));
        
        // Calculate progress percentage
        double progressPercentage = targetAmount.compareTo(BigDecimal.ZERO) == 0 ? 0 :
            (currentAmount.doubleValue() / targetAmount.doubleValue()) * 100;
        
        // Calculate projected completion date
        LocalDate projectedCompletionDate;
        if (monthlyContribution.compareTo(BigDecimal.ZERO) <= 0) {
            projectedCompletionDate = null; // No progress if monthly contribution is zero or negative
        } else {
            BigDecimal remainingAmount = targetAmount.subtract(currentAmount);
            long monthsToComplete = remainingAmount.divide(
                monthlyContribution,
                0,
                java.math.RoundingMode.CEILING
            ).longValue();
            projectedCompletionDate = endDate.plusMonths(monthsToComplete);
        }
        
        return new SavingsProgress(
            endDate,
            targetAmount,
            currentAmount,
            monthlyContribution,
            progressPercentage,
            projectedCompletionDate
        );
    }

    @Override
    public Map<Tag, SpendingForecast> getSpendingForecast(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        System.out.println("[DEBUG] getSpendingForecast: transactions found = " + transactions.size());
        Map<Tag, List<Transaction>> transactionsByTag = transactions.stream()
            .collect(Collectors.groupingBy(this::getPrimaryTag));
        
        Map<Tag, SpendingForecast> forecasts = new HashMap<>();
        
        // --- Support for 'All Categories' ---
        if (!transactions.isEmpty()) {
            Tag allCategoriesTag = new Tag("All Categories");
            allCategoriesTag.setId(null); // No ID for dummy tag
            List<Transaction> allTransactions = new ArrayList<>(transactions);
            if (allTransactions.size() >= 1) {
                BigDecimal totalSpending = allTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal currentAverage = totalSpending.divide(
                    BigDecimal.valueOf(allTransactions.size()),
                    2,
                    java.math.RoundingMode.HALF_UP
                );
                double confidenceLevel = 0.7 + (allTransactions.size() / 100.0);
                confidenceLevel = Math.min(0.95, Math.max(0.5, confidenceLevel));
                List<BigDecimal> historicalData = allTransactions.stream()
                    .map(Transaction::getAmount)
                    .collect(Collectors.toList());
                forecasts.put(allCategoriesTag, new SpendingForecast(
                    allCategoriesTag,
                    currentAverage,
                    currentAverage,
                    confidenceLevel,
                    historicalData
                ));
            }
        }
        // --- End support for 'All Categories' ---
        
        for (Map.Entry<Tag, List<Transaction>> entry : transactionsByTag.entrySet()) {
            Tag tag = entry.getKey();
            List<Transaction> tagTransactions = entry.getValue();
            if (tagTransactions.size() < 1) {
                continue; // Need at least 1 transaction for meaningful analysis
            }
            // Calculate current average spending
            BigDecimal totalSpending = tagTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal currentAverage = totalSpending.divide(
                BigDecimal.valueOf(tagTransactions.size()),
                2,
                java.math.RoundingMode.HALF_UP
            );
            // Calculate standard deviation
            double variance = tagTransactions.stream()
                .map(t -> t.getAmount().subtract(currentAverage).pow(2))
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
            double stdDev = Math.sqrt(variance);
            // Calculate confidence level based on number of transactions and variance
            double confidenceLevel = Math.min(0.95, 0.7 + (tagTransactions.size() / 100.0));
            confidenceLevel = Math.max(0.5, confidenceLevel - (stdDev / currentAverage.doubleValue()));
            // Project next month's spending (average + trend)
            BigDecimal projectedAmount = currentAverage;
            // Add trend analysis if we have enough data
            if (tagTransactions.size() >= 6) {
                tagTransactions.sort(Comparator.comparing(Transaction::getDate));
                double[] x = new double[tagTransactions.size()];
                double[] y = new double[tagTransactions.size()];
                for (int i = 0; i < tagTransactions.size(); i++) {
                    x[i] = i;
                    y[i] = tagTransactions.get(i).getAmount().doubleValue();
                }
                double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
                for (int i = 0; i < x.length; i++) {
                    sumX += x[i];
                    sumY += y[i];
                    sumXY += x[i] * y[i];
                    sumX2 += x[i] * x[i];
                }
                double slope = (x.length * sumXY - sumX * sumY) / (x.length * sumX2 - sumX * sumX);
                projectedAmount = currentAverage.add(BigDecimal.valueOf(slope));
            }
            List<BigDecimal> historicalData = tagTransactions.stream()
                .map(Transaction::getAmount)
                .collect(Collectors.toList());
            forecasts.put(tag, new SpendingForecast(
                tag,
                currentAverage,
                projectedAmount,
                confidenceLevel,
                historicalData
            ));
        }
        // Debug output: print forecast map keys
        System.out.println("[DEBUG] getSpendingForecast: forecast map keys:");
        for (Tag t : forecasts.keySet()) {
            System.out.println("  - " + t.getName() + " (ID=" + t.getId() + ")");
        }
        return forecasts;
    }

    @Override
    public List<BudgetUtilization> getBudgetUtilization(LocalDate startDate, LocalDate endDate, Tag category) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        Map<Tag, List<Transaction>> transactionsByTag = transactions.stream()
            .collect(Collectors.groupingBy(this::getPrimaryTag));
            
        List<BudgetUtilization> utilization = new ArrayList<>();
        
        for (Map.Entry<Tag, List<Transaction>> entry : transactionsByTag.entrySet()) {
            Tag tag = entry.getKey();
            if (category != null && !tag.equals(category)) {
                continue;
            }
            
            List<Transaction> tagTransactions = entry.getValue();
            
            // Calculate total spending for the period
            BigDecimal totalSpending = tagTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            // Get budget amount for the period
            BigDecimal budgetAmount = getBudgetAmount(tag, startDate, endDate);
            
            // Calculate utilization percentage
            double utilizationPercentage = budgetAmount.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                (totalSpending.doubleValue() / budgetAmount.doubleValue()) * 100;
                
            // Determine if budget is exceeded
            boolean isExceeded = totalSpending.compareTo(budgetAmount) > 0;
            
            utilization.add(new BudgetUtilization(
                endDate,
                tag,
                budgetAmount,
                totalSpending,
                utilizationPercentage,
                isExceeded
            ));
        }
        
        return utilization;
    }

    @Override
    public SpendingPattern getSpendingPatterns(LocalDate startDate, LocalDate endDate, Tag category) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        List<Transaction> categoryTransactions = transactions.stream()
            .filter(t -> category == null || getPrimaryTag(t).equals(category))
            .collect(Collectors.toList());
            
        if (categoryTransactions.size() < 1) {
            return null; // Need at least 1 transaction for meaningful analysis
        }
        
        // Calculate average transaction amount
        BigDecimal totalAmount = categoryTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageAmount = totalAmount.divide(
            BigDecimal.valueOf(categoryTransactions.size()),
            2,
            java.math.RoundingMode.HALF_UP
        );
        
        // Calculate frequency (transactions per day)
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal frequency = BigDecimal.valueOf(categoryTransactions.size())
            .divide(BigDecimal.valueOf(daysBetween), 4, java.math.RoundingMode.HALF_UP);
        
        // Calculate day of week distribution
        List<LocalDate> dayOfWeekDistribution = categoryTransactions.stream()
            .map(Transaction::getDate)
            .collect(Collectors.toList());
        
        // Calculate hour of day distribution
        List<LocalDate> hourOfDayDistribution = categoryTransactions.stream()
            .map(t -> t.getDate().atStartOfDay().toLocalDate())
            .collect(Collectors.toList());
        
        // Find most common day and hour
        int mostCommonDay = dayOfWeekDistribution.stream()
            .collect(Collectors.groupingBy(
                d -> d.getDayOfWeek().getValue(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(1);
            
        int mostCommonHour = hourOfDayDistribution.stream()
            .collect(Collectors.groupingBy(
                d -> d.atStartOfDay().getHour(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(12);
        
        return new SpendingPattern(
            category,
            averageAmount,
            frequency,
            BigDecimal.valueOf(mostCommonDay),
            BigDecimal.valueOf(mostCommonHour),
            dayOfWeekDistribution,
            hourOfDayDistribution
        );
    }

    @Override
    public List<CategoryTrend> getCategoryTrends(LocalDate startDate, LocalDate endDate, Tag category, String interval) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        List<Transaction> categoryTransactions = transactions.stream()
            .filter(t -> category == null || getPrimaryTag(t).equals(category))
            .collect(Collectors.toList());
        
        if (categoryTransactions.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Group transactions by interval
        Map<LocalDate, List<Transaction>> transactionsByInterval = new HashMap<>();
        for (Transaction t : categoryTransactions) {
            LocalDate intervalDate;
            switch (interval.toLowerCase()) {
                case "daily":
                    intervalDate = t.getDate();
                    break;
                case "weekly":
                    intervalDate = t.getDate().with(t.getDate().getDayOfWeek().getValue() == 1 ? 
                        t.getDate() : t.getDate().minusDays(t.getDate().getDayOfWeek().getValue() - 1));
                    break;
                case "monthly":
                    intervalDate = t.getDate().withDayOfMonth(1);
                    break;
                default:
                    intervalDate = t.getDate();
            }
            transactionsByInterval.computeIfAbsent(intervalDate, k -> new ArrayList<>()).add(t);
        }
        
        // Calculate trends
        List<CategoryTrend> trends = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Transaction>> entry : transactionsByInterval.entrySet()) {
            LocalDate intervalDate = entry.getKey();
            List<Transaction> intervalTransactions = entry.getValue();
            
            // Calculate total amount for the interval
            BigDecimal totalAmount = intervalTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            // Calculate transaction count
            BigDecimal transactionCount = BigDecimal.valueOf(intervalTransactions.size());
            
            // Calculate average amount
            BigDecimal averageAmount = totalAmount.divide(
                transactionCount,
                2,
                java.math.RoundingMode.HALF_UP
            );
            
            trends.add(new CategoryTrend(
                intervalDate,
                category,
                totalAmount,
                averageAmount,
                averageAmount // For now, use averageAmount as the trend
            ));
        }
        
        // Sort by date
        trends.sort(Comparator.comparing(CategoryTrend::date));
        
        return trends;
    }
} 