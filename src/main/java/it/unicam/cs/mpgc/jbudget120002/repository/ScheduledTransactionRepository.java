// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/ScheduledTransactionRepository.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import java.time.LocalDate;
import java.util.List;

public interface ScheduledTransactionRepository
        extends Repository<ScheduledTransaction, Long> {
    List<ScheduledTransaction> findByStartDateBetween(LocalDate from, LocalDate to);
}
