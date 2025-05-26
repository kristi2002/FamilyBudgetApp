package it.unicam.cs.mpgc.jbudget120002.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class Statistic {
    public record CategoryExpense(
        String category,
        BigDecimal amount,
        double percentage
    ) {
        public String getCategory() { return category; }
        public BigDecimal getAmount() { return amount; }
        public double getPercentage() { return percentage; }
    }
    
    public record MonthlyBalance(
        LocalDate month,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
    ) {
        public LocalDate getMonth() { return month; }
        public BigDecimal getIncome() { return income; }
        public BigDecimal getExpenses() { return expenses; }
        public BigDecimal getBalance() { return balance; }
    }
    
    public record CategoryComparison(
        Tag category,
        BigDecimal previousPeriodAmount,
        BigDecimal currentPeriodAmount,
        double percentageChange
    ) {}

    public static List<CategoryExpense> getTopExpenseCategories(
            Set<Transaction> transactions,
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        Map<String, BigDecimal> categoryTotals = transactions.stream()
            .filter(t -> !t.isIncome() && !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .flatMap(t -> t.getTags().stream())
            .collect(Collectors.groupingBy(
                Tag::getName,
                Collectors.mapping(
                    tag -> tag.getTransactions().stream()
                        .filter(t -> !t.isIncome() && !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));

        BigDecimal totalExpenses = categoryTotals.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return categoryTotals.entrySet().stream()
            .map(e -> new CategoryExpense(
                e.getKey(),
                e.getValue(),
                totalExpenses.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    e.getValue().multiply(BigDecimal.valueOf(100))
                        .divide(totalExpenses, 2, java.math.RoundingMode.HALF_UP)
                        .doubleValue()
            ))
            .sorted((a, b) -> b.amount.compareTo(a.amount))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public static List<MonthlyBalance> getMonthlyBalances(
            Set<Transaction> transactions,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<MonthlyBalance> balances = new ArrayList<>();
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

            balances.add(new MonthlyBalance(monthStart, income, expenses, income.subtract(expenses)));
            current = current.plusMonths(1);
        }

        return balances;
    }

    public static List<CategoryComparison> comparePeriods(
            Set<Transaction> transactions,
            LocalDate previousStart,
            LocalDate previousEnd,
            LocalDate currentStart,
            LocalDate currentEnd
    ) {
        Map<Tag, BigDecimal> previousPeriod = calculatePeriodExpenses(
            transactions, previousStart, previousEnd);
        Map<Tag, BigDecimal> currentPeriod = calculatePeriodExpenses(
            transactions, currentStart, currentEnd);

        Set<Tag> allCategories = new HashSet<>();
        allCategories.addAll(previousPeriod.keySet());
        allCategories.addAll(currentPeriod.keySet());

        return allCategories.stream()
            .map(category -> {
                BigDecimal previous = previousPeriod.getOrDefault(category, BigDecimal.ZERO);
                BigDecimal current = currentPeriod.getOrDefault(category, BigDecimal.ZERO);
                
                double percentageChange = previous.equals(BigDecimal.ZERO) ? 100.0 :
                    current.subtract(previous)
                          .multiply(BigDecimal.valueOf(100))
                          .divide(previous, 2, java.math.RoundingMode.HALF_UP)
                          .doubleValue();

                return new CategoryComparison(category, previous, current, percentageChange);
            })
            .sorted((a, b) -> b.currentPeriodAmount().compareTo(a.currentPeriodAmount()))
            .collect(Collectors.toList());
    }

    private static Map<Tag, BigDecimal> calculatePeriodExpenses(
            Set<Transaction> transactions,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<Tag, BigDecimal> expensesByCategory = new HashMap<>();

        transactions.stream()
            .filter(t -> !t.isIncome())
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .forEach(transaction -> {
                for (Tag tag : transaction.getTags()) {
                    expensesByCategory.merge(tag, transaction.getAmount(), BigDecimal::add);
                }
            });

        return expensesByCategory;
    }

    public static BigDecimal calculateNetWorth(Set<Transaction> transactions, LocalDate date) {
        return transactions.stream()
            .filter(t -> !t.getDate().isAfter(date))
            .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Map<Tag, Double> calculateCategoryPercentages(
            Set<Transaction> transactions,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<Tag, BigDecimal> expensesByCategory = calculatePeriodExpenses(
            transactions, startDate, endDate);
        
        BigDecimal totalExpenses = expensesByCategory.values()
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalExpenses.equals(BigDecimal.ZERO)) {
            return new HashMap<>();
        }

        return expensesByCategory.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalExpenses, 2, java.math.RoundingMode.HALF_UP)
                    .doubleValue()
            ));
    }
}
