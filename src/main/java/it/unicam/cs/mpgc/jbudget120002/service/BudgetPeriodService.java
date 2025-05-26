package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.BudgetPeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetPeriodService {
    BudgetPeriod create(BudgetPeriod b);
    Optional<BudgetPeriod> findById(Long id);
    Optional<BudgetPeriod> findByDate(LocalDate date);
    List<BudgetPeriod> findAll();
    BudgetPeriod update(BudgetPeriod b);
    void delete(Long id);
}
