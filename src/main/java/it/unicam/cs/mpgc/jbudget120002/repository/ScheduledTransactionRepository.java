// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/ScheduledTransactionRepository.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ScheduledTransactionRepository
        extends Repository<ScheduledTransaction, Long> {

    List<ScheduledTransaction> findByDateRange(LocalDate start, LocalDate end);

    List<ScheduledTransaction> findActive(LocalDate asOfDate);

    List<ScheduledTransaction> findByTag(Tag tag);

    List<ScheduledTransaction> findByTags(List<Tag> tags);

    BigDecimal calculateSumForPeriod(LocalDate startDate, LocalDate endDate, boolean isIncome);

    List<ScheduledTransaction> findByUser(User user);
}
