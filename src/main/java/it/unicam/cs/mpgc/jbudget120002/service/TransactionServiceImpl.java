package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.*;
import it.unicam.cs.mpgc.jbudget120002.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionServiceImpl extends BaseService implements TransactionService {
    private final TransactionRepository repository;
    private final TagService tagService;

    public TransactionServiceImpl(EntityManager entityManager, TransactionRepository repository, TagService tagService) {
        super(entityManager);
        this.repository = repository;
        this.tagService = tagService;
    }

    @Override
    public List<Transaction> findTransactions(User user, String searchTerm, LocalDate startDate, LocalDate endDate, Tag category, boolean includeSubcategories) {
        List<Long> groupIds = user.getGroups().stream().map(Group::getId).collect(Collectors.toList());
        List<Long> tagIds = null;
        if (category != null) {
            tagIds = new ArrayList<>();
            if (includeSubcategories) {
                tagIds.addAll(tagService.getAllDescendants(category.getId()).stream()
                                 .map(Tag::getId)
                                 .collect(Collectors.toList()));
            }
            tagIds.add(category.getId());
        }
        return repository.findWithFilters(user, groupIds, searchTerm, startDate, endDate, tagIds);
    }

    @Override
    public List<Transaction> findAllForUser(User user) {
        return repository.findAllForUser(user);
    }

    @Override
    public Transaction createTransaction(LocalDate date, String description, BigDecimal amount,
                                         boolean isIncome, Set<Long> tagIds) {
        beginTransaction();
        try {
            Transaction transaction = new Transaction(date, description, amount, isIncome);
            if (!tagIds.isEmpty()) {
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(transaction::addTag);
                }
            }
            repository.save(transaction);
            commitTransaction();
            return transaction;
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public Transaction createTransaction(User user, LocalDate date, String description, BigDecimal amount,
                                         boolean isIncome, Set<Long> tagIds) {
        beginTransaction();
        try {
            Transaction transaction = new Transaction(date, description, amount, isIncome);
            transaction.setUser(user);
            if (!tagIds.isEmpty()) {
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(transaction::addTag);
                }
            }
            repository.save(transaction);
            commitTransaction();
            return transaction;
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public void deleteTransaction(Long id) {
        beginTransaction();
        try {
            repository.deleteById(id);
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public List<Transaction> findAll() {
        return repository.findAll();
    }

    @Override
    public Transaction findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<Transaction> findByDateRange(LocalDate start, LocalDate end) {
        return repository.findByDateBetween(start, end);
    }

    @Override
    public void updateTransaction(Long id, LocalDate date, String description,
                                  BigDecimal amount, boolean isIncome, Set<Long> tagIds) {
        beginTransaction();
        try {
            Transaction transaction = findById(id);
            if (transaction != null) {
                transaction.setDate(date);
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setIncome(isIncome);
                Set<Tag> newTags = new HashSet<>();
                for (Long tagId : tagIds) {
                    tagService.findById(tagId).ifPresent(newTags::add);
                }
                transaction.setTags(newTags);
                repository.save(transaction);
            }
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public BigDecimal calculateBalance(LocalDate start, LocalDate end) {
        List<Transaction> transactions = findByDateRange(start, end);
        return transactions.stream()
            .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> findByTag(Long tagId) {
        return repository.findByTagId(tagId);
    }

    @Override
    public List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate) {
        return repository.findByTagIdAndDateRange(tagId, startDate, endDate);
    }

    @Override
    public List<Transaction> findByScheduledTransaction(Long scheduledTransactionId) {
        return repository.findByScheduledTransaction(scheduledTransactionId);
    }

    @Override
    public List<Transaction> findByLoanPlan(Long loanPlanId) {
        return repository.findByLoanPlan(loanPlanId);
    }

    @Override
    public BigDecimal calculateIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        return findByDateRange(startDate, endDate).stream()
            .filter(Transaction::isIncome)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        return findByDateRange(startDate, endDate).stream()
            .filter(t -> !t.isIncome())
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> findByTag(Tag tag, boolean includeChildren) {
        if (tag == null) {
            return Collections.emptyList();
        }
        if (!includeChildren) {
            return findByTag(tag.getId());
        }
        Set<Tag> allTags = new HashSet<>(tagService.getAllDescendants(tag.getId()));
        allTags.add(tag);
        Set<Long> tagIds = allTags.stream()
            .map(Tag::getId)
            .collect(Collectors.toSet());
        return repository.findByTags(tagIds, false, tagIds.size());
    }

    @Override
    public List<Transaction> findByTags(Collection<Tag> tags, boolean matchAll) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> tagIds = tags.stream()
            .map(Tag::getId)
            .collect(Collectors.toSet());
        return repository.findByTags(tagIds, matchAll, tagIds.size());
    }

    @Override
    public Map<Tag, BigDecimal> calculateTagTotals(LocalDate startDate, LocalDate endDate,
                                                     boolean includeChildren) {
        // This logic is complex and might be better suited for a dedicated statistics service
        // For now, keeping it simple
        return new HashMap<>();
    }

    @Override
    public List<Transaction> findTransactionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetween(startDate, endDate);
    }

    @Override
    public Map<Tag, TransactionStatistics> calculateTagStatistics(LocalDate startDate, LocalDate endDate,
                                                                    boolean includeChildren) {
        // This logic is complex and might be better suited for a dedicated statistics service
        return new HashMap<>();
    }

    @Override
    public BigDecimal calculateAmountForTagInPeriod(Tag tag, LocalDate startDate, LocalDate endDate) {
        if (tag == null) {
            return BigDecimal.ZERO;
        }
        return findByTagAndDateRange(tag.getId(), startDate, endDate).stream()
            .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateNetWorth(LocalDate asOfDate) {
        return repository.findAll().stream()
            .filter(t -> !t.getDate().isAfter(asOfDate))
            .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> findTransactionsInPeriod(LocalDate startDate, LocalDate endDate, int limit) {
        return findByDateRange(startDate, endDate).stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findTransactionsInPeriodForUser(User user, LocalDate startDate, LocalDate endDate, int limit) {
        return findByDateRangeForUser(user, startDate, endDate).stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateBalanceForUser(User user, LocalDate start, LocalDate end) {
        List<Transaction> transactions = findByDateRangeForUser(user, start, end);
        return transactions.stream()
            .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateIncomeForPeriodForUser(User user, LocalDate startDate, LocalDate endDate) {
        return findByDateRangeForUser(user, startDate, endDate).stream()
            .filter(Transaction::isIncome)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateExpensesForPeriodForUser(User user, LocalDate startDate, LocalDate endDate) {
        return findByDateRangeForUser(user, startDate, endDate).stream()
            .filter(t -> !t.isIncome())
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> findByDateRangeForUser(User user, LocalDate start, LocalDate end) {
        return repository.findByDateBetweenForUser(user, start, end);
    }
}
