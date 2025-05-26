// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/BudgetPeriodRepository.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.BudgetPeriod;
import java.time.LocalDate;
import java.util.Optional;

public interface BudgetPeriodRepository
        extends Repository<BudgetPeriod, Long> {
    Optional<BudgetPeriod> findByDate(LocalDate date);
}
