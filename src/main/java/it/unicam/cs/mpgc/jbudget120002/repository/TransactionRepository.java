package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TransactionRepository extends Repository<Transaction, Long> {
    List<Transaction> findByDateBetween(LocalDate from, LocalDate to);
    List<Transaction> findByDateBetweenForUser(User user, LocalDate from, LocalDate to);
    List<Transaction> findByTagName(String tagName);
    List<Transaction> findByTagId(Long tagId);
    List<Transaction> findByTagIdAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByScheduledTransaction(Long scheduledTransactionId);
    List<Transaction> findByLoanPlan(Long loanPlanId);
    List<Transaction> findByTags(Collection<Long> tagIds, boolean matchAll, int tagCount);
    List<Transaction> findAllForUser(User user);
    List<Transaction> findWithFilters(User user, List<Long> groupIds, String searchTerm, LocalDate startDate, LocalDate endDate, List<Long> tagIds);
    List<Transaction> findByUserAndGroups(User user, Set<Long> groupIds, LocalDate startDate, LocalDate endDate, String search, List<Tag> tags);
    List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate);
    boolean existsByScheduledTransactionAndDate(ScheduledTransaction scheduledTransaction, LocalDate date);
}
